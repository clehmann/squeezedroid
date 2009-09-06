package net.chrislehmann.squeezedroid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;

import net.chrislehmann.squeezedroid.exception.ApplicationException;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.util.SerializationUtils;

public class EventThread extends Thread
{
   private Socket _eventSocket;
   private Writer _eventWriter;
   private BufferedReader _eventReader;

   private Map<String, List<PlayerStatusHandler>> _handlers = new HashMap<String, List<PlayerStatusHandler>>();

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

   private interface CommandHandler {
      void handleCommand( String data, PlayerStatusHandler handler );
   }
   
   private CommandHandler playListHandler = new CommandHandler()
   {
      public void handleCommand(String data, PlayerStatusHandler handler)
      {
         String[] splitData = data.split( " " );
         if( splitData.length == 0 )
         {
            return;
         }
         String action = splitData[0];
         
         if( "newsong".equalsIgnoreCase( action ) )
         {
            handler.onSongChanged( _status );
         }
         
         if( "loadtracks".equalsIgnoreCase( action ) 
               ||  "delete".equalsIgnoreCase( action ) 
               || "deletetracks".equalsIgnoreCase( action ) )
         {
            handler.onPlaylistChanged( _status );
         }
      }
   };

   private CommandHandler timeChangeHandler = new CommandHandler()
   {
      public void handleCommand(String data, PlayerStatusHandler handler)
      {
         try
         {
            Integer newTime = Integer.parseInt( data );
            handler.onTimeChanged( newTime );
            
         } catch (NumberFormatException e) {/* Invalid time, do not notify */}
      }
   };
   
   private Map<String, CommandHandler> _commandHandlers = new HashMap<String, CommandHandler>();
   {
      _commandHandlers.put( "playlist", playListHandler );
      _commandHandlers.put( "time", timeChangeHandler );
}
   
   private void notify(String event, String playerId, String data)
   {
      synchronized ( _handlers )
      {
         updateStatus(playerId);
         if ( _handlers.containsKey( playerId ) )
         {
            List<PlayerStatusHandler> handlers = _handlers.get( playerId );
            for ( PlayerStatusHandler handler : handlers )
            {
               if( handler != null && _commandHandlers.containsKey( event ) )
               {
                  _commandHandlers.get( event ).handleCommand( data, handler );
               }
            }
         }
      }
   }

   protected void updateStatus(String playerId)
   {
      _status = _service.getPlayerStatus( new Player( playerId) );
   }

   private void connect()
   {
      try
      {
         _eventSocket = new Socket( host, cliPort );
         _eventWriter = new OutputStreamWriter( _eventSocket.getOutputStream() );
         _eventReader = new BufferedReader( new InputStreamReader( _eventSocket.getInputStream() ) );

         _eventWriter.write( "listen 1\n" );
         _eventWriter.flush();
         _eventWriter.write( "subscribe playlist,time\n" );
         _eventWriter.flush();
      }
      catch ( Exception e )
      {
         throw new ApplicationException( "Cannot connect to squeezeserver", e );
      }
   }

   public synchronized void subscribe(Player player, PlayerStatusHandler handler)
   {
      synchronized ( _handlers )
      {
         if ( !_handlers.containsKey( player.getId() ) )
         {
            _handlers.put( player.getId(), new ArrayList<PlayerStatusHandler>() );
         }
         _handlers.get( player.getId() ).add( handler );
      }
   }

   public void unsubscribe(Player player, PlayerStatusHandler handler)
   {
      synchronized ( _handlers )
      {
         if ( _handlers.containsKey( player.getId() ) )
         {
            _handlers.get( player.getId() ).remove( handler );
         }
      }
   }

   
   public void unsubscribe(PlayerStatusHandler handler)
   {
      synchronized ( _handlers )
      {
         for ( List<PlayerStatusHandler> handlerList : _handlers.values() )
         {
            handlerList.remove( handler );
         }
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


}