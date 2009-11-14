package net.chrislehmann.squeezedroid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.chrislehmann.squeezedroid.exception.ApplicationException;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Genre;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.util.SerializationUtils;
import net.chrislehmann.util.SerializationUtils.Unserializer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import android.util.Log;

/**
 * Implementation of {@link SqueezeService} that uses the SqueezeCenter command
 * line interface
 * 
 * @author lehmanc
 */
public class CliSqueezeService implements SqueezeService
{

   private static final String LOGTAG = "SQUEEZE";
   private static final String SONG_TAGS = "aslepPdxKJ";
   /**
    * Host to connect to
    */
   private String host = "localhost";
   /**
    * Port to connect to
    */
   private int cliPort = 9090;
   private int httpPort = 9000;

   private Socket clientSocket;
   private Writer clientWriter;
   private BufferedReader clientReader;
   
   private String username;
   private String password;

   private EventThread eventThread;
   private BlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<Runnable>();
   private Thread commandThread = new Thread()
   {
      public void run()
      {
         try
         {
            while ( !isInterrupted() )
            {
               Runnable r = commandQueue.take();
               if ( !isInterrupted() )
               {
                  r.run();
               }
            }
         }
         catch ( InterruptedException e )
         {
            //just finish...
         }

      };
   };

   public CliSqueezeService(String host, int cliPort, int httpPort)
   {
      super();
      this.host = host;
      this.cliPort = cliPort;
      this.httpPort = httpPort;
   }

   private Pattern countPattern = Pattern.compile( "count%3A([^ ]*)" );
   private Pattern artistsResponsePattern = Pattern.compile( "id%3A([^ ]*) artist%3A([^ ]*)" );
   private Pattern genresResponsePattern = Pattern.compile( "id%3A([^ ]*) genre%3A([^ ]*)" );
   private Pattern albumsResponsePattern = Pattern.compile( "id%3A([^ ]*) album%3A([^ ]*)( artwork_track_id%3A([0-9]+)){0,1} artist%3A([^ ]*)" );
   private Pattern playersResponsePattern = Pattern.compile( "playerid%3A([^ ]*) uuid%3A([^ ]*) ip%3A([^ ]*) name%3A([^ ]*)" );
   private Pattern songsResponsePattern = Pattern
         .compile( "id%3A([^ ]*) .*?title%3A([^ ]*) .*?artist%3A([^ ]*) .*?(artist_id%3A([^ ]*) )*.*?(album%3A([^ ]*) )*.*?(album_id%3A([^ ]*) )*.*?duration%3A([^ ]*).*?( remote%3A([^ ]*))*.*?( artwork_url%3A([^ ]*))*.*?( artwork_track_id%3A([^ ]*))*" );
   private Pattern playlistCountPattern = Pattern.compile( "playlist_tracks%3A([^ ]*)" );
   private Pattern playerStatusResponsePattern = Pattern.compile( " mode%3A([^ ]*) .*?(time%3A([^ ]*))* .*?mixer%20volume%3A([^ ]*) .*?playlist%20repeat%3A([^ ]*) .*?playlist%20shuffle%3A([^ ]*) .*?playlist_cur_index%3A([0-9]*)" );
   private Pattern syncgroupsResponsePattern = Pattern.compile( "sync (.*)" );
   private Pattern versionResponsePattern = Pattern.compile( "version ([0-9|.]+)" );

   private Unserializer<Song> songUnserializer = new SerializationUtils.Unserializer<Song>()
   {
      public Song unserialize(Matcher matcher)
      {
         Song song = new Song();
         song.setId( matcher.group( 1 ) );
         song.setName( SerializationUtils.decode( matcher.group( 2 ) ) );
         song.setArtist( SerializationUtils.decode( matcher.group( 3 ) ) );
         if ( matcher.group( 5 ) != null )
         {
            song.setArtistId( SerializationUtils.decode( matcher.group( 5 ) ) );
         }
         song.setAlbum( SerializationUtils.decode( matcher.group( 7 ) ) );
         if ( matcher.group( 9 ) != null )
         {
            song.setAlbumId( SerializationUtils.decode( matcher.group( 10 ) ) );
         }


         if ( matcher.group( 11 ) != null && "1".equals( matcher.group( 12 ) ) )
         {
            song.setRadioStation( true );
         }

         //set the artwork images
         if ( matcher.group( 14 ) != null )
         {
            song.setImageUrl( SerializationUtils.decode( matcher.group( 14 ) ) );
         }
         else if ( matcher.group( 16 ) != null )
         {
            song.setImageThumbnailUrl( "http://" + host + ":" + httpPort + "/music/" + matcher.group( 16 ) + "/cover_50x50_o" );
            song.setImageUrl( "http://" + host + ":" + httpPort + "/music/" + matcher.group( 16 ) + "/cover_320x320_o" );
         }

         try
         {
            Float duration = Float.parseFloat( matcher.group( 10 ) );
            song.setDurationInSeconds( duration.intValue() );
         }
         catch ( NumberFormatException e )
         {
         }
         return song;
      }
   };
   
   

   /**
    * Connect to the squeezecenter server and log in if required. Will throw an
    * {@link ApplicationException} if the connection fails.
    */
   public void connect()
   {
      try
      {
         clientSocket = new Socket( host, cliPort );
         clientWriter = new OutputStreamWriter( clientSocket.getOutputStream() );
         clientReader = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );

         clientSocket.setSoTimeout( 10 * 1000 );
      }
      catch ( Exception e )
      {
         throw new ApplicationException( "Cannot connect to host '" + host + "' at port '" + cliPort, e );
      }
      if( username != null && password != null )
      {
         executeCommand( "login " + username + " " + password );
      }
      //Check for a valid 'version' response to make sure we are actually connected.
      String response = executeCommand( "version ?" );
      Matcher matcher = null;
      if( response != null )
      {
         matcher = versionResponsePattern.matcher( response );
      }
      if( response == null || !matcher.matches() )
      {
         clientSocket = null;
         clientWriter = null;
         clientWriter = null;
         throw new ApplicationException( "Cannot log into server" );
      }

      eventThread = new EventThread( host, cliPort );
      eventThread.setUsername( username );
      eventThread.setPassword( password );
      eventThread.setService( this );
      eventThread.start();

      commandThread.start();
   }

   /**
    * Disconnect from the server. Throws an {@link ApplicationException} if an
    * error occours
    */
   public void disconnect()
   {
      if ( clientSocket != null && clientSocket.isConnected() )
      {
         try
         {
            clientSocket.close();
            clientReader = null;
            clientWriter = null;
         }
         catch ( Exception e )
         {
            throw new ApplicationException( "Error closing socket", e );
         }
         clientSocket = null;
      }

      if( eventThread != null )
      {
         eventThread.disconnect();
         eventThread = null;
      }
      if( commandThread != null )
      {
         commandThread.interrupt();
      }
   }

   public boolean isConnected()
   {
      return clientSocket != null && clientSocket.isConnected();
   }

   synchronized private String executeCommand(String command)
   {
      String response = null;
      if ( writeCommand( command ) )
      {
         response = readResponse();
      }
      return response;
   }

   private void executeAsyncCommand(final String commandString)
   {
      Runnable command = new Runnable()
      {
         public void run()
         {
            executeCommand( commandString );
         }
      };
      commandQueue.add( command );
   }


   private String readResponse()
   {
      String response = null;
      try
      {
         if ( clientReader != null )
         {
            response = clientReader.readLine();
         }
      }
      catch ( SocketTimeoutException sto )
      {
         Log.e( LOGTAG, "Timeout reading socket, disconnecting from server" );
      }
      catch ( IOException e )
      {
         Log.e( LOGTAG, "error reading response", e );
      }

      //Disconnect from the server if we don't have a response.
      if ( response == null )
      {
         Log.e( LOGTAG, "Error reading response, disconnecting from server" );
         this.disconnect();
      }
      return response;
   }

   private boolean writeCommand(String command)
   {
      boolean written = false;
      try
      {
         if ( clientWriter != null )
         {
            clientWriter.write( command + "\n" );
            clientWriter.flush();
            written = true;
         }
      }
      catch ( IOException e )
      {
         Log.e( LOGTAG, "Error writing response, disconnecting from server" );
         disconnect();
      }
      return written;
   }

   public BrowseResult<Genre> browseGenres(Item parent, int start, int numberOfItems)
   {

      String command = "genres " + start + " " + numberOfItems;
      Unserializer<Genre> unserializer = new Unserializer<Genre>()
      {

         public Genre unserialize(Matcher matcher)
         {
            Genre genre = new Genre();
            genre.setId( matcher.group( 1 ) );
            genre.setName( SerializationUtils.decode( matcher.group( 2 ) ) );
            return genre;
         }
      };
      BrowseResult<Genre> browseResult = new BrowseResult<Genre>();
      String result = executeCommand( command );
      if ( result != null )
      {
         List<Genre> genres = SerializationUtils.unserializeList( genresResponsePattern, result, unserializer );
         browseResult.setResutls( genres );
         browseResult.setTotalItems( unserializeCount( result ) );
      }
      return browseResult;
   }


   public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems)
   {
      return browseAlbums( parent, start, numberOfItems, Sort.TITLE );
   }

   public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems, Sort sort)
   {
      String command = "albums " + start + " " + numberOfItems;
      if ( parent instanceof Artist )
      {
         command += " artist_id:" + parent.getId();
      }

      if ( parent instanceof Genre )
      {
         command += " genre_id:" + parent.getId();
      }
      if ( sort != Sort.TITLE )
      {
         command += " sort:" + sort.toString().toLowerCase();
      }

      command += " tags:laj";
      String result = executeCommand( command );
      BrowseResult<Album> browseResult = new BrowseResult<Album>();

      if ( result != null )
      {
         List<Album> albums = SerializationUtils.unserializeList( albumsResponsePattern, result, new Unserializer<Album>()
         {
            public Album unserialize(Matcher matcher)
            {
               Album album = new Album();
               album.setId( matcher.group( 1 ) );
               album.setName( SerializationUtils.decode( matcher.group( 2 ) ) );
               album.setArtist( SerializationUtils.decode( matcher.group( 5 ) ) );
               album.setCoverThumbnailUrl( "http://" + host + ":" + httpPort + "/music/" + matcher.group( 4 ) + "/cover_50x50_o" );
               album.setCoverUrl( "http://" + host + ":" + httpPort + "/music/" + matcher.group( 4 ) + "/cover_320x320	_o" );
               return album;
            }
         } );

         browseResult.setTotalItems( unserializeCount( result ) );
         browseResult.setResutls( albums );
      }
      return browseResult;
   }

   public BrowseResult<Artist> browseArtists(Item parent, int start, int numberOfItems)
   {
      String command = "artists " + start + " " + numberOfItems;
      if ( parent instanceof Genre )
      {
         command += " genre_id:" + parent.getId();
      }

      BrowseResult<Artist> browseResult = new BrowseResult<Artist>();
      String result = executeCommand( command );
      if ( result != null )
      {
         Matcher matcher = artistsResponsePattern.matcher( result );

         List<Artist> artists = new ArrayList<Artist>();
         while ( matcher.find() )
         {
            Artist artist = new Artist();
            artist.setId( matcher.group( 1 ) );
            artist.setName( SerializationUtils.decode( matcher.group( 2 ) ) );
            artists.add( artist );
         }

         browseResult.setResutls( artists );
         browseResult.setTotalItems( unserializeCount( result ) );
      }
      return browseResult;
   }

   public BrowseResult<Song> browseSongs(Item parent, int start, int numberOfItems)
   {
      String command = "titles " + start + " " + numberOfItems + " tags:" + SONG_TAGS;


      if ( parent instanceof Artist )
      {
         command += " artist_id:" + parent.getId();
      }
      else if ( parent instanceof Album )
      {
         command += " album_id:" + parent.getId();
      }

      BrowseResult<Song> browseResult = new BrowseResult<Song>();
      String result = executeCommand( command );
      if ( result != null )
      {
         List<Song> songs = SerializationUtils.unserializeList( songsResponsePattern, result, songUnserializer );
         browseResult.setTotalItems( unserializeCount( result ) );
         browseResult.setResutls( songs );

      }

      return browseResult;
   }

   private Integer unserializeCount(String result)
   {
      Integer numSongs = 0;
      Matcher countMatcher = countPattern.matcher( result );
      if ( countMatcher.find() )
      {
         String countString = countMatcher.group( 1 );
         numSongs = Integer.valueOf( countString );
      }
      else
      {
         android.util.Log.e( this.getClass().getCanonicalName(), "Cannot find match for count from response '" + result + "'" );
      }
      return numSongs;
   }

   public List<Player> getPlayers()
   {

      String command = new String( "players 0 1000" );
      List<Player> players = new ArrayList<Player>();
      String result = executeCommand( command );
      if ( result != null )
      {
         players = SerializationUtils.unserializeList( playersResponsePattern, result, new SerializationUtils.Unserializer<Player>()
         {
            public Player unserialize(Matcher matcher)
            {
               Player player = new Player();
               player.setId( SerializationUtils.decode( matcher.group( 1 ) ) );
               player.setName( SerializationUtils.decode( matcher.group( 4 ) ) );
               return player;
            }
         } );
      }

      List<Player> groupedPlayers = new ArrayList<Player>();

      for ( Player player : players )
      {
         command = player.getId() + " sync ?";
         String playerSyncResult = executeCommand( command );
         Matcher matcher = syncgroupsResponsePattern.matcher( playerSyncResult );
         if ( matcher.find() )
         {
            String syncedPlayersString = SerializationUtils.decode( matcher.group( 1 ) );
            String[] syncedPlayersArray = syncedPlayersString.split( "," );
            for ( int i = 0; i < syncedPlayersArray.length; i++ )
            {
               String syncedPlayerId = syncedPlayersArray[i];
               Player syncedPlayer = (Player) CollectionUtils.find( players, new PlayerIdEqualsPredicate( syncedPlayerId ) );
               if ( syncedPlayer != null )
               {
                  player.getSyncronizedPlayers().add( syncedPlayer );
               }
            }
         }
         groupedPlayers.add( player );
      }
      return groupedPlayers;
   }

   public Player getPlayer(String playerId)
   {
      List<Player> players = getPlayers();
      return (Player) CollectionUtils.find( players, new PlayerIdEqualsPredicate( playerId ) );
   }

   private class PlayerIdEqualsPredicate implements Predicate
   {
      private String playerId;

      public PlayerIdEqualsPredicate(String playerId)
      {
         this.playerId = playerId;
      }

      public boolean evaluate(Object arg0)
      {
         boolean matches = false;
         if ( arg0 instanceof Player )
         {
            Player rhs = (Player) arg0;
            matches = playerId.equals( rhs.getId() );
         }
         return matches;
      }
   }

   public PlayerStatus getPlayerStatus(Player player)
   {
      String command = new String( player.getId() + " status - 1 tags:" + SONG_TAGS );
      String result = executeCommand( command );

      PlayerStatus status = new PlayerStatus();
      if ( result != null )
      {

         Song song = SerializationUtils.unserialize( songsResponsePattern, result, songUnserializer );
         status.setCurrentSong( song );


         Matcher statusMatcher = playerStatusResponsePattern.matcher( result );
         if ( status != null && statusMatcher.find() && statusMatcher.group( 1 ) != null )
         {
            Log.d( LOGTAG, "Status: " + statusMatcher.group( 1 ) );
            Log.d( LOGTAG, "Time: " + statusMatcher.group( 3 ) );
            Log.d( LOGTAG, "Volume: " + statusMatcher.group( 4 ) );
            Log.d( LOGTAG, "Repeat: " + statusMatcher.group( 5 ) );
            Log.d( LOGTAG, "Shuffle: " + statusMatcher.group( 6 ) );
            Log.d( LOGTAG, "Playlist Index: " + statusMatcher.group( 7 ) );

            status.setStatus( statusMatcher.group( 1 ) );
            status.setCurrentIndex( Integer.parseInt( statusMatcher.group( 7 ) ) );
            String positionString = statusMatcher.group( 3 );
            try
            {
               if ( positionString != null )
               {
                  Double d = Double.parseDouble( positionString );
                  status.setCurrentPosition( d.intValue() );
               }
               if ( statusMatcher.group( 4 ) != null )
               {
                  Double d = Double.parseDouble( statusMatcher.group( 4 ) );
                  status.setVolume( d.intValue() );
               }
            }
            catch ( NumberFormatException nfd )
            {/* Invalid, don't set volume. */
            }

            status.setRepeatMode( RepeatMode.intToRepeatModeMap.get( statusMatcher.group( 5 ) ) );
            status.setShuffleMode( ShuffleMode.intToShuffleModeMap.get( statusMatcher.group( 6 ) ) );
         }

      }
      return status;

   }

   public BrowseResult<Song> getCurrentPlaylist(Player player, Integer start, Integer numberOfItems)
   {
      String command = player.getId() + " status " + start + " " + numberOfItems + " tags:" + SONG_TAGS;
      String result = executeCommand( command );

      BrowseResult<Song> browseResult = new BrowseResult<Song>();
      if ( result != null )
      {
         List<Song> songs = SerializationUtils.unserializeList( songsResponsePattern, result, songUnserializer );
         browseResult.setResutls( songs );

         Matcher countMatcher = playlistCountPattern.matcher( result );
         if ( countMatcher.find() )
         {
            String countString = countMatcher.group( 1 );
            browseResult.setTotalItems( Integer.valueOf( countString ) );
         }
         else
         {
            android.util.Log.e( this.getClass().getCanonicalName(), "Cannot find match for count from status response '" + result + "'" );
         }
      }
      return browseResult;
   }

   public void addItem(Player player, Item item)
   {
      String extraParams = getParamName( item );
      if ( extraParams != null )
      {
         String command = player.getId() + " playlist addtracks " + extraParams + "=" + item.getId();
         executeAsyncCommand( command );
      }
   }

   public void playItem(Player player, Item item)
   {
      String extraParams = getParamName( item );
      if ( extraParams != null )
      {
         String command = player.getId() + " playlist loadtracks " + extraParams + "=" + item.getId();
         executeAsyncCommand( command );
      }
   }

   private String getParamName(Item item)
   {
      String extraParams = null;
      if ( item instanceof Album )
      {
         extraParams = "album.id";
      }
      else if ( item instanceof Artist )
      {
         extraParams = "contributor.id";
      }
      else if ( item instanceof Artist )
      {
         extraParams = "track.id";
      }
      else if ( item instanceof Genre )
      {
         extraParams = "genre.id";
      }
      return extraParams;
   }

   public void jump(Player player, String position)
   {
      executeAsyncCommand( player.getId() + " playlist index " + position );
   }

   public void togglePause(Player player)
   {
      executeAsyncCommand( player.getId() + " pause" );
   }

   public void pause(Player player)
   {
      executeAsyncCommand( player.getId() + " pause 1" );
   }

   public void play(Player player)
   {
      executeAsyncCommand( player.getId() + " play" );
   }

   public void stop(Player player)
   {
      executeAsyncCommand( player.getId() + " stop" );
   }

   public void removeAllItemsByArtist(Player player, String artistId)
   {
      executeAsyncCommand( player.getId() + " playlistcontrol cmd:delete artist_id:" + artistId );
   }

   public void removeAllItemsInAlbum(Player player, String albumId)
   {
      executeAsyncCommand( player.getId() + " playlistcontrol cmd:delete album_id:" + albumId );
   }

   public void removeItem(Player player, int playlistIndex)
   {
      executeAsyncCommand( player.getId() + " playlist delete " + playlistIndex );
   }

   public void subscribe(final Player player, final PlayerStatusHandler handler)
   {
      Runnable r = new Runnable()
      {
         public void run()
         {
            eventThread.subscribe( player, handler );
         }
      };
      commandQueue.add( r );
   }

   public void unsubscribe(final Player player, final PlayerStatusHandler handler)
   {
      Runnable r = new Runnable()
      {
         public void run()
         {
            if ( eventThread != null )
            {
               eventThread.unsubscribe( player, handler );
            }
         }
      };
      commandQueue.add( r );
   }

   public void unsubscribeAll(final PlayerStatusHandler handler)
   {
      Runnable r = new Runnable()
      {
         public void run()
         {
            if ( eventThread != null )
            {
               eventThread.unsubscribe( handler );
            }
         }
      };
      commandQueue.add( r );
   }

   public void seekTo(Player player, int time)
   {
      executeAsyncCommand( player.getId() + " time " + time );
   }

   public void changeVolume(Player player, int volumeLevel)
   {
      executeAsyncCommand( player.getId() + " mixer volume " + volumeLevel );
   }

   public void synchronize(Player player, Player playerToSyncTo)
   {
      executeAsyncCommand( playerToSyncTo.getId() + " sync " + player.getId() );
   }

   public void unsynchronize(Player player)
   {
      executeAsyncCommand( player.getId() + " sync -" );
   }

   public void setShuffleMode(Player player, ShuffleMode mode)
   {
      executeAsyncCommand( player.getId() + " playlist shuffle " + mode.id );
   }

   public void setRepeatMode(Player player, RepeatMode mode)
   {
      executeAsyncCommand( player.getId() + " playlist repeat " + mode.id );
   }

   public void unsubscribe(final ServerStatusHandler handler)
   {
      Runnable r = new Runnable()
      {

         public void run()
         {
            if( eventThread != null )
            {
               eventThread.unsubscribe( handler );
            }
         }
      };
      commandQueue.add( r );

   }

   public void subscribe(final ServerStatusHandler handler)
   {
      Runnable r = new Runnable()
      {

         public void run()
         {
            eventThread.subscribe( handler );
         }
      };
      commandQueue.add( r );
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }
}