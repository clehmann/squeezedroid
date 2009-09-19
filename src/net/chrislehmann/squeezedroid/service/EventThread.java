package net.chrislehmann.squeezedroid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.chrislehmann.squeezedroid.exception.ApplicationException;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.util.SerializationUtils;
import android.util.Log;

public class EventThread extends Thread
{
   private static final String LOGTAG = "EventThread";
   private Socket _eventSocket;
   private Writer _eventWriter;
   private BufferedReader _eventReader;

   private Object _handlersMutex = new Object();

   private Map<String, List<PlayerStatusHandler>> _handlers = new HashMap<String, List<PlayerStatusHandler>>();
   
   private BlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<Runnable>();
   
   private Thread workerThread = new Thread()
   {
      public void run()
      {
         try
         {
            while ( !interrupted() )
            {
               Runnable r = commandQueue.take();
               r.run();
            }
         }
         catch ( InterruptedException e )
         {
            Log.d( LOGTAG, "Worker thread interrupted, stopping" );
         }
      }
   };

   private String host = "localhost";
   private int cliPort = 9090;

   private Pattern eventPattern = Pattern.compile( "([^ ]*) ([^ ]*) (.*)" );

   protected PlayerStatus _status;


   private SqueezeService _service;

   public EventThread(String host, int cliPort)
   {
      super();
      this.host = host;
      this.cliPort = cliPort;
   }

   @Override
   public void run()
   {
      connect();
      try
      {
         while ( !isInterrupted() && _eventSocket.isConnected() )
         {
            String line = _eventReader.readLine();
            if ( line != null )
            {
               Matcher matcher = eventPattern.matcher( line );
               if ( matcher.find() )
               {
                  String playerId = SerializationUtils.decode( matcher.group( 1 ) );
                  String eventType = SerializationUtils.decode( matcher.group( 2 ) );
                  String data = matcher.group( 3 );
                  notify( eventType, playerId, data );

                  //TODO - find a better way of doing this.  Squeezeserver only sends one sync command, 
                  // and it might be associated to a player we are not subscribed too.
                  //
                  //As a hack, just 'fake' a second sync command, swapping the playerid and data.
                  if( "sync".equals( eventType ) && !data.equals(  "-" ) )
                  {
                     notify( eventType, SerializationUtils.decode( data ), playerId );
                  }
               }
            }

         }
         if ( _eventSocket.isClosed() )
         {
            _eventSocket.close();
         }

      }
      catch ( IOException e )
      {
         // error reading, just end thread
      }

      _eventSocket = null;
      _eventReader = null;
      _eventWriter = null;

      //TODO - Handle server notifications
      // notify( Event.DISCONNECT.toString(), null, null );

   }

   private interface CommandHandler
   {
      void handleCommand(String playerId, String data, PlayerStatusHandler handler);
   }

   private CommandHandler playListHandler = new CommandHandler()
   {
      public void handleCommand(String playerId, String data, PlayerStatusHandler handler)
      {
         String[] splitData = data.split( " " );
         if ( splitData.length == 0 )
         {
            return;
         }
         String action = splitData[0];

         if ( "newsong".equalsIgnoreCase( action ) )
         {
            handler.onSongChanged( _status );
         }

         if ( "loadtracks".equalsIgnoreCase( action ) || "delete".equalsIgnoreCase( action ) || "deletetracks".equalsIgnoreCase( action ) )
         {
            handler.onPlaylistChanged( _status );
         }
      }
   };

   private CommandHandler timeChangeHandler = new CommandHandler()
   {
      public void handleCommand(String playerId, String data, PlayerStatusHandler handler)
      {
         try
         {
            Integer newTime = Integer.parseInt( data );
            handler.onTimeChanged( newTime );

         }
         catch ( NumberFormatException e )
         {/* Invalid time, do not notify */
         }
      }
   };

   private CommandHandler playerSyncStatusHandler = new CommandHandler()
   {
      public void handleCommand(String playerId, String data, PlayerStatusHandler handler)
      {
         if ( "-".equals( data ) )
         {
            handler.onPlayerUnsynchronized();
         }
         else
         {
            Player updatedPlayer = _service.getPlayer( playerId );
            handler.onPlayerSynchronized( updatedPlayer, SerializationUtils.decode( data ) );
         }
      }
   };

   private CommandHandler mixerChangeHandler = new CommandHandler()
   {
      public void handleCommand(String playerId, String data, PlayerStatusHandler handler)
      {
         String[] parts = data.split( " " );
         if ( parts.length >= 2 && "volume".equals( parts[0] ) )
         {

            try
            {
               Integer newVolume = Integer.parseInt( parts[1] );
               handler.onVolumeChanged( newVolume );

            }
            catch ( NumberFormatException e )
            {/* Invalid time, do not notify */
            }
         }
      }
   };


   private Map<String, CommandHandler> _commandHandlers = new HashMap<String, CommandHandler>();
   {
      _commandHandlers.put( "playlist", playListHandler );
      _commandHandlers.put( "time", timeChangeHandler );
      _commandHandlers.put( "mixer", mixerChangeHandler );
      _commandHandlers.put( "sync", playerSyncStatusHandler );

   }

   private void notify(String event, String playerId, String data)
   {
      synchronized ( _handlersMutex )
      {
         Log.v( LOGTAG, "Got event '" + playerId + ":" + event + "', notifying any handlers that care" );
         updateStatus( playerId );
         if ( _handlers.containsKey( playerId ) )
         {
            List<PlayerStatusHandler> handlers = _handlers.get( playerId );
            for ( PlayerStatusHandler handler : handlers )
            {
               Log.v( LOGTAG, "Handler '" + handler + " cares about this player" );
               if ( handler != null && _commandHandlers.containsKey( event ) )
               {
                  Log.v( LOGTAG, "Using CommandHandler " + _commandHandlers.get( event ) + "' to handle event" );
                  _commandHandlers.get( event ).handleCommand( playerId, data, handler );
               }
            }
         }
         Log.v( LOGTAG, "Done notifying event '" + playerId + ":" + event + "'" );
      }

      //_commandHandlers.get( "Done notifying handlers" );
   }

   protected void updateStatus(String playerId)
   {
      _status = _service.getPlayerStatus( new Player( playerId ) );
   }

   private void connect()
   {
      Log.d( LOGTAG, "Connecting to server at " + host + ":" + cliPort );
      try
      {
         _eventSocket = new Socket( host, cliPort );
         _eventWriter = new OutputStreamWriter( _eventSocket.getOutputStream() );
         _eventReader = new BufferedReader( new InputStreamReader( _eventSocket.getInputStream() ) );
         Log.v( LOGTAG, "connected, sending subscribe commands" );


         _eventWriter.write( "listen 1\n" );
         _eventWriter.flush();
         _eventWriter.write( "subscribe playlist,time,mixer,sync\n" );
         _eventWriter.flush();
         Log.v( LOGTAG, "subscribed to events" );

         Log.v( LOGTAG, "Starting worker thread" );
         workerThread.start();
         Log.v( LOGTAG, "Worker thread started" );

      }
      catch ( Exception e )
      {
         Log.e( LOGTAG, "Error connecting Squeezeserver CLI at " + host + ":" + cliPort, e );
         throw new ApplicationException( "Cannot connect to squeezeserver", e );
      }
   }

   public void subscribe(final Player player, final PlayerStatusHandler handler)
   {
      Runnable action = new Runnable()
      {
         public void run()
         {
            synchronized ( _handlersMutex )
            {
               Log.v( LOGTAG, "Suscribing to notifications for player " + player.getId() + " with handler " + handler );
               if ( !_handlers.containsKey( player.getId() ) )
               {
                  _handlers.put( player.getId(), Collections.synchronizedList( new ArrayList<PlayerStatusHandler>() ) );
               }
               List<PlayerStatusHandler> handlers = _handlers.get( player.getId() );
               handlers.add( handler );
               Log.v( LOGTAG, "Done subscribing to notifications for player " + player.getId() + " with handler " + handler );
            }
         }
      };
      addToQueue( action );
   }

   public void addToQueue( Runnable runnable )
   {
      try
      {
         commandQueue.put( runnable );
      }
      catch ( InterruptedException e )
      {
         //should only be called on the main thread anyway, we can just ignore.
      }
      
   }
   
   public void unsubscribe(final Player player, final PlayerStatusHandler handler)
   {
      Runnable action = new Runnable()
      {
         public void run()
         {
            synchronized ( _handlersMutex )
            {
               Log.v( LOGTAG, "Unsubscribing from all notifactions handled by " + handler + " with player " + player.getId() );
               if ( _handlers.containsKey( player.getId() ) )
               {
                  List<PlayerStatusHandler> handlers = _handlers.get( player.getId() );
                  synchronized ( handlers )
                  {
                     handlers.remove( handler );
                  }
               }
               Log.v( LOGTAG, "Done unsubscribing from all notifactions handled by " + handler + " with player " + player.getId() );
      
            }
         }
      };
      addToQueue( action );
   }


   public void unsubscribe(final PlayerStatusHandler handler)
   {
      Runnable action = new Runnable()
      {
         public void run()
         {

            synchronized ( _handlersMutex )
            {
               Log.d( LOGTAG, "Unsubscribing from all notifactions handled by " + handler );
               for ( List<PlayerStatusHandler> handlerList : _handlers.values() )
               {
                  handlerList.remove( handler );
               }
               Log.d( LOGTAG, "Done unsubscribing from all notifactions handled by " + handler );
            }
         }
      };
      addToQueue( action );
   }
   
   public CommandHandler getTimeChangeHandler()
   {
      return timeChangeHandler;
   }

   public void setTimeChangeHandler(CommandHandler timeChangeHandler)
   {
      this.timeChangeHandler = timeChangeHandler;
   }

   public void setService(SqueezeService service)
   {
      _service = service;
   }


}