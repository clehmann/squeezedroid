package net.chrislehmann.squeezedroid.listadapter;

import java.util.ArrayList;
import java.util.List;

import net.chrislehmann.squeezedroid.eventhandler.EventHandler;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.SqueezeService.Event;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

public class PlayListAdapter extends SongListAdapter {
	private Player _player;
	private PlayerStatus _currentStatus;
	
	private EventHandler onPlaylistChanged = new EventHandler() {
		public void onEvent(String result) {
			_currentStatus = _service.getPlayerStatus(_player);
			numItems = 1;
			resetPages();
		}
	};
	
	private EventHandler onSongChanged = new EventHandler() {
		public void onEvent(String result) {
			_currentStatus = _service.getPlayerStatus(_player);
			notifyChange();
		}
	};

	public void setPlayer(Player player) {
		_player = player;
		numItems = 1;
		resetPages();
	}

	public PlayListAdapter(SqueezeService service, Activity parent, Player player) {
		super(service, parent, null);
		_player = player;
		_currentStatus = service.getPlayerStatus(player);
		service.subscribe(Event.NEWSONG, player.getId(), onSongChanged );
		service.subscribe(Event.LOADTRACKS, player.getId(), onPlaylistChanged );
		service.subscribe(Event.DELETE, player.getId(), onPlaylistChanged );
		service.subscribe(Event.DELETETRACKS, player.getId(), onPlaylistChanged );
	}
	
	protected List<? extends Object> createPage(int start, int pageSize) {
		List<Song> playlist = new ArrayList<Song>();
		if (_player != null) {
			BrowseResult<Song> result = _service.getCurrentPlaylist(_player, start, pageSize);
			playlist = result.getResutls();
			numItems = result.getTotalItems();
		}
		return playlist;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		if( _currentStatus != null && position == _currentStatus.getCurrentIndex() )
		{
			v.setBackgroundColor(Color.YELLOW);
		}
		return v;
	}
	
	
}
