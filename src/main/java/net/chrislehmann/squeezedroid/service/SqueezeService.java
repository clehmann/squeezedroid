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

    public BrowseResult<ApplicationMenuItem> browseApplication(Player player, Application application, int start, int numberOfItems);

    public BrowseResult<ApplicationMenuItem> browseApplication(Player player, Application application, ApplicationMenuItem parent, int start, int numberOfItems);

    public BrowseResult<ApplicationMenuItem> browseApplication(Player player, Application application, ApplicationMenuItem parent, String query, int start, int numberOfItems);

    public PlayerStatus getPlayerStatus(Player player);

    public BrowseResult<Song> getCurrentPlaylist(Player player, Integer start, Integer numberOfItems);

    public List<Player> getPlayers();

    public List<Player> getPlayers(boolean removeDuplicatePlayers);

    public Player getPlayer(String playerId);

    public void addItem(Player player, Item item);

    public void playItem(Player player, Item item);

    public void playItemNext(Player player, Item item);

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

    public void unsynchronize(Player player);

    public void synchronize(Player player, Player playerToSyncTo);

    public void setShuffleMode(Player player, ShuffleMode mode);

    public void setRepeatMode(Player player, RepeatMode mode);

    public SearchResult search(String searchTerms, int numResults);

    public void clearPlaylist(Player selectedPlayer);

    List<Song> getSongsForItem(Item selectedItem);

}