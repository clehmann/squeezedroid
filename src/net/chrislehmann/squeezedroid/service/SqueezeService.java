package net.chrislehmann.squeezedroid.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Folder;
import net.chrislehmann.squeezedroid.model.Genre;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.SearchResult;
import net.chrislehmann.squeezedroid.model.Song;

/**
 * Represents a connection to a SqueezeServer instance. 
 * @author lehmanc
 *
 */
public interface SqueezeService
{
   enum Sort {
      TITLE( ), NEW
   }
   
   @SuppressWarnings("serial")
   enum ShuffleMode {
      NONE(0), SONG(1), ALBUM(2);
      int id;
      private ShuffleMode( int id )
      {
         this.id = id;
      };
      
      public static Map<String, ShuffleMode> intToShuffleModeMap = new HashMap<String, ShuffleMode>(){{
         put( "0", ShuffleMode.NONE );
         put( "1", ShuffleMode.SONG );
         put( "2", ShuffleMode.ALBUM );
      }};
   }

   @SuppressWarnings("serial")
   enum RepeatMode {
      NONE(0), SONG(1), ALL(2);
      int id;
      private RepeatMode( int id )
      {
         this.id = id;
      };

      public static Map<String, RepeatMode> intToRepeatModeMap = new HashMap<String, RepeatMode>(){{
         put( "0", RepeatMode.NONE );
         put( "1", RepeatMode.SONG );
         put( "2", RepeatMode.ALL );
      }};
   }

   public BrowseResult<Genre> browseGenres(Item parent, int start, int numberOfItems);
   
   public BrowseResult<Item> browseFolders( Folder parent, int start, int numberOfItems );

   public BrowseResult<Artist> browseArtists(Item parent, int start, int numberOfItems);

   public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems);

   public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems, Sort sort);

   public BrowseResult<Song> browseSongs(Item parent, int start, int numberOfItems);

   public PlayerStatus getPlayerStatus(Player player);

   public BrowseResult<Song> getCurrentPlaylist(Player player, Integer start, Integer numberOfItems);

   public List<Player> getPlayers();

   public List<Player> getPlayers(boolean removeDuplicatePlayers);

   public Player getPlayer(String playerId);

   public void addItem(Player player, Item item);

   public void playItem(Player player, Item item);

   public void removeItem(Player selectedPlayer, int playlistIndex);
   
   public void removeAllItemsByArtist(Player player, String artistId);
   
   public void removeAllItemsInAlbum(Player player, String albumId);

   public void play(Player player);

   public void pause(Player player);

   public void togglePause(Player player);

   public void stop(Player player);

   public void jump(Player player, String position);

   public void connect();

   public void disconnect();

   public boolean isConnected();

   public void subscribe(ServerStatusHandler handler);

   public void unsubscribe(ServerStatusHandler onServiceStatusChanged);

   public void subscribe(Player player, PlayerStatusHandler handler);

   public void unsubscribe(Player player, PlayerStatusHandler handler);

   public void unsubscribeAll(PlayerStatusHandler onPlayerStatusChanged);

   public void seekTo(Player player, int time);

   public void changeVolume(Player player, int volumeLevel);

   public void unsynchronize( Player player );

   public void synchronize( Player player, Player playerToSyncTo );

   public void setShuffleMode( Player player, ShuffleMode mode );
   
   public void setRepeatMode( Player player, RepeatMode mode );
   
   public SearchResult search( String searchTerms, int numResults);

   public void clearPlaylist(Player selectedPlayer);

}