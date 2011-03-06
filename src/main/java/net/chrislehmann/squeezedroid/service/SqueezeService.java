package net.chrislehmann.squeezedroid.service;

import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.ApplicationMenuItem;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Folder;
import net.chrislehmann.squeezedroid.model.Genre;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Playlist;
import net.chrislehmann.squeezedroid.model.RepeatMode;
import net.chrislehmann.squeezedroid.model.SearchResult;
import net.chrislehmann.squeezedroid.model.ShuffleMode;
import net.chrislehmann.squeezedroid.model.Song;

import java.util.List;

/**
 * Interface that that provides a connection to the Squeezebox Server.  This allows
 * you to control {@link Player}s, query for information and Subscribe to Events.
 *
 * @author lehmanc
 */
public interface SqueezeService {

    /**
     * Enum representing the possible sort states for various browse methods.
     */
    enum Sort {
        TITLE, NEW
    }

    public BrowseResult<Genre> browseGenres(Item parent, int start, int numberOfItems);

    public BrowseResult<Item> browseFolders(Folder parent, int start, int numberOfItems);

    public BrowseResult<Artist> browseArtists(Item parent, int start, int numberOfItems);

    public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems);

    public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems, Sort sort);

    public BrowseResult<Song> browseSongs(Item parent, int start, int numberOfItems);

    public BrowseResult<Playlist> listPlaylists(int start, int numberOfItems);

    public BrowseResult<Application> listApplications(int start, int numberOfItems);

    public BrowseResult<Application> listRadioStations(int start, int numberOfItems);

    public BrowseResult<ApplicationMenuItem> browseApplication(String playerId, Application application, int start, int numberOfItems);

    public BrowseResult<ApplicationMenuItem> browseApplication(String playerId, Application application, ApplicationMenuItem parent, int start, int numberOfItems);

    public BrowseResult<ApplicationMenuItem> browseApplication(String playerId, Application application, ApplicationMenuItem parent, String query, int start, int numberOfItems);

    public PlayerStatus getPlayerStatus(String playerId);

    public BrowseResult<Song> getCurrentPlaylist(String playerId, Integer start, Integer numberOfItems);

    public List<Player> getPlayers();

    public List<Player> getPlayers(boolean removeDuplicatePlayers);

    public Player getPlayer(String playerId);

    public void addItem(String playerId, Item item);

    public void playItem(String playerId, Item item);

    public void playItemNext(String playerId, Item item);

    public void removeItem(String playerId, int playlistIndex);

    public void removeAllItemsByArtist(String playerId, String artistId);

    public void removeAllItemsInAlbum(String playerId, String albumId);

    public void togglePower( String playerId );

    public void play(String playerId);

    public void pause(String playerId);

    public void togglePause(String playerId);

    public void stop(String playerId);

    public void jump(String playerId, String position);

    public void connect();

    public void disconnect();

    public boolean isConnected();

    public void subscribe(ServerStatusHandler handler);

    public void unsubscribe(ServerStatusHandler onServiceStatusChanged);

    public void subscribe(String playerId, PlayerStatusHandler handler);

    public void unsubscribe(String playerId, PlayerStatusHandler handler);

    public void unsubscribeAll(PlayerStatusHandler onPlayerStatusChanged);

    public void seekTo(String playerId, int time);

    public void changeVolume(String playerId, int volumeLevel);

    public void unsynchronize(String playerId);

    public void synchronize(String playerId, String playerIdToSyncTo);

    public void setShuffleMode(String playerId, ShuffleMode mode);

    public void setRepeatMode(String playerId, RepeatMode mode);

    public SearchResult search(String searchTerms, int numResults);

    public void clearPlaylist(String selectedPlayer);

    List<Song> getSongsForItem(Item selectedItem);

}