package net.chrislehmann.squeezedroid.service;

import android.util.Log;
import net.chrislehmann.squeezedroid.exception.ApplicationException;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.RepeatMode;
import net.chrislehmann.squeezedroid.model.ShuffleMode;
import net.chrislehmann.util.SerializationUtils;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventThread extends Thread
{
   private static final String LOGTAG = "EventThread";
   private Socket _eventSocket;
   private Writer _eventWriter;
   private BufferedReader _eventReader;

   private Object _playerHandlersMutex = new Object();
   private Object _serverHandlersMutex = new Object();

   private Map<String, List<PlayerStatusHandler>> _playerHandlers = new HashMap<String, List<PlayerStatusHandler>>();
   private List<ServerStatusHandler> _serverHandlers = new ArrayList<ServerStatusHandler>();

   private String password;
   private String username;

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
         listen();
      }
      catch ( Exception e )
      {
         Log.e( LOGTAG, "Error listening to events from socke", e );
      }
      
      if ( _eventSocket != null && !_eventSocket.isClosed() )
      {
         try
         {
            _eventSocket.close();
         }
         catch ( IOException e )
         {
            Log.e( LOGTAG, "Error closing socket", e );
         }
      }

      _eventSocket = null;
      _eventReader = null;
      _eventWriter = null;

      synchronized ( _serverHandlersMutex )
      {
         for ( ServerStatusHandler handler : _serverHandlers )
         {
            handler.onDisconnect();
         }
      }
   }
   
   public void disconnect()
   {
      interrupt();
      if( this._eventSocket != null && this._eventSocket.isConnected() )
      {
         try
         {
            this._eventSocket.close();
         }
         catch ( IOException e )
         {
            Log.d( LOGTAG, "Error closing event socket", e );
         }
         
      }
      synchronized ( _serverHandlersMutex )
      {
         for ( ServerStatusHandler handler : _serverHandlers )
         {
            handler.onDisconnect();
         }
      }
   }

   private void listen() throws IOException
   {
      String line = "";
      while ( line  != null && !isInterrupted() && _eventSocket.isConnected()  ) 
      {
         Log.v( LOGTAG, "Reading line");
         line = _eventReader.readLine();
         Log.v( LOGTAG, "Got line '" + line + "'");
         if ( !isInterrupted() && line != null )
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
               if ( "sync".equals( eventType ) && !data.equals( "-" ) )
               {
                  notify( eventType, SerializationUtils.decode( data ), playerId );
               }
            }
         }
      }
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
         if ( "shuffle".equalsIgnoreCase( action ) )
         {
            ShuffleMode mode = ShuffleMode.intToShuffleModeMap.get( splitData[1] );
            handler.onShuffleModeChanged( mode );
         }
         if ( "repeat".equalsIgnoreCase( action ) )
         {
            RepeatMode mode = RepeatMode.intToRepeatModeMap.get( splitData[1] );
            handler.onRepeatModeChanged( mode );
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

   private CommandHandler playPauseStopHandler = new CommandHandler()
   {
      public void handleCommand(String playerId, String data, PlayerStatusHandler handler)
      {
         if ( _status.isPaused() )
         {
            handler.onPause();
         }
         if ( _status.isPlaying() )
         {
            handler.onPlay();
         }
         if ( _status.isStopped() )
         {
            handler.onStop();
         }
      }
   };


   private CommandHandler clientHandler = new CommandHandler()
   {
      public void handleCommand(String playerId, String data, PlayerStatusHandler handler)
      {
         if ( "forget".equals( data ) )
         {
            handler.onDisconnect();
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
            handler.onPlayerSynchronized( playerId, SerializationUtils.decode( data ) );
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
      _commandHandlers.put( "play", playPauseStopHandler );
      _commandHandlers.put( "pause", playPauseStopHandler );
      _commandHandlers.put( "stop", playPauseStopHandler );
      _commandHandlers.put( "client", clientHandler );

   }

   private void notify(String event, String playerId, String data)
   {
      synchronized ( _playerHandlersMutex )
      {
         Log.v( LOGTAG, "Got event '" + playerId + ":" + event + "', notifying any handlers that care" );
         updateStatus( playerId );
         if ( _playerHandlers.containsKey( playerId ) )
         {
            List<PlayerStatusHandler> handlers = _playerHandlers.get( playerId );
            for ( PlayerStatusHandler handler : handlers )
            {
               Log.v( LOGTAG, "Handler '" + handler + " cares about this player" );
               if ( handler != null && _commandHandlers.containsKey( event ) )
               {
                  Log.v( LOGTAG, "Using CommandHandler " + _commandHandlers.get( event ) + "' to handle event" );
                  try
                  {
                      _commandHandlers.get( event ).handleCommand( playerId, data, handler );
                  } catch (Exception e)
                  {
                      Log.e(LOGTAG, "Error calling handler: ", e);
                  }
               }
            }
         }
         Log.v( LOGTAG, "Done notifying event '" + playerId + ":" + event + "'" );
      }

   }

   protected void updateStatus(String playerId)
   {
      _status = _service.getPlayerStatus( playerId );
   }

   private void connect()
   {
      Log.d( LOGTAG, "Connecting to server at " + host + ":" + cliPort );
      try
      {
         _eventSocket = new Socket( host, cliPort );
         _eventWriter = new OutputStreamWriter( _eventSocket.getOutputStream() );
         _eventReader = new BufferedReader( new InputStreamReader( _eventSocket.getInputStream() ) );
         
         if( username != null && password != null )
         {
            _eventWriter.write( "login " + username + " " + password + "\n" );
            _eventWriter.flush();
            String result = _eventReader.readLine();
//            result.toString();
         }
         
         _eventWriter.write( "listen 1\n" );
         _eventWriter.flush();
         _eventWriter.write( "subscribe playlist,time,mixer,sync,play,pause,stop,client\n" );
         _eventWriter.flush();
         Log.v( LOGTAG, "subscribed to events" );
         

      }
      catch ( Exception e )
      {
         Log.e( LOGTAG, "Error connecting Squeezeserver CLI at " + host + ":" + cliPort, e );
         throw new ApplicationException( "Cannot connect to squeezeserver", e );
      }
   }
   


   public void subscribe(final String playerId, final PlayerStatusHandler handler)
   {
      synchronized ( _playerHandlersMutex )
      {
         Log.v( LOGTAG, "Suscribing to notifications for player " + playerId + " with handler " + handler );
         if ( !_playerHandlers.containsKey( playerId ) )
         {
            _playerHandlers.put( playerId, Collections.synchronizedList( new ArrayList<PlayerStatusHandler>() ) );
         }
         List<PlayerStatusHandler> handlers = _playerHandlers.get( playerId );
         if( !handlers.contains(handler))
         {
             handlers.add( handler );
         }
         Log.v( LOGTAG, "Done subscribing to notifications for player " + playerId + " with handler " + handler );
      }
   }

   public void subscribe(ServerStatusHandler handler)
   {
      synchronized ( _serverHandlersMutex )
      {
         _serverHandlers.add( handler );
      }
   }

   public void unsubscribe(ServerStatusHandler handler)
   {
      synchronized ( _serverHandlersMutex )
      {
         _serverHandlers.remove( handler );
      }
   }

   public void unsubscribe(final String playerId, final PlayerStatusHandler handler)
   {
      synchronized ( _playerHandlersMutex )
      {
         Log.v( LOGTAG, "Unsubscribing from all notifactions handled by " + handler + " with player " + playerId );
         if ( _playerHandlers.containsKey( playerId ) )
         {
            List<PlayerStatusHandler> handlers = _playerHandlers.get( playerId );
            synchronized ( handlers )
            {
               handlers.remove( handler );
            }
         }
         Log.v( LOGTAG, "Done unsubscribing from all notifactions handled by " + handler + " with player " + playerId );

      }
   }


   public void unsubscribe(final PlayerStatusHandler handler)
   {
      synchronized ( _playerHandlersMutex )
      {
         Log.d( LOGTAG, "Unsubscribing from all notifactions handled by " + handler );
         for ( List<PlayerStatusHandler> handlerList : _playerHandlers.values() )
         {
            handlerList.remove( handler );
         }
         Log.d( LOGTAG, "Done unsubscribing from all notifactions handled by " + handler );
      }
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

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

}