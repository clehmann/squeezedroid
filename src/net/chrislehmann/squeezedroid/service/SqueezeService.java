package net.chrislehmann.squeezedroid.service;

import java.util.List;

import net.chrislehmann.squeezedroid.eventhandler.EventHandler;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Genre;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;

public interface SqueezeService {

	public BrowseResult<Genre> browseGenres(Item parent, int start, int numberOfItems);

	public BrowseResult<Artist> browseArtists(Item parent, int start, int numberOfItems);

	public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems);

	public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems, Sort sort);

	public BrowseResult<Song> browseSongs(Item parent, int start, int numberOfItems);

	public PlayerStatus getPlayerStatus(Player player);

	public BrowseResult<Song> getCurrentPlaylist(Player player, Integer start, Integer numberOfItems);

	public List<Player> getPlayers();

	public void addItem(Player player, Item item);

	public void playItem(Player player, Item item);

	public void play(Player player);
	
	public void pause( Player player );

	public void togglePause( Player player );

	public void stop( Player player );

	public void jump( Player player, String position );

	public void connect();

	public void disconnect();

	public boolean isConnected();

	public void subscribe( Player player, PlayerStatusHandler handler );

	public void unsubscribe(  Player player, PlayerStatusHandler handler );

//	public void subscribe(Event event, String playerId, EventHandler handler);

	enum Event {
		NEWSONG, OPEN, PREFSET, DISCONNECT, REPEAT, LOADTRACKS, ADDTRACKS, JUMP, DELETE, DELETETRACKS
	}
	enum Sort {
		TITLE, NEW
	}

//	public void unsubscribe(Event event, String id, EventHandler handler);
//
//	public void unsubscribeAll(Event event);

	public void removeItem(Player selectedPlayer, int playlistIndex);

	public void removeAllItemsByArtist(Player player, String artistId);

	public void removeAllItemsInAlbum(Player player, String albumId);

   public void seekTo(Player player, int time);
}