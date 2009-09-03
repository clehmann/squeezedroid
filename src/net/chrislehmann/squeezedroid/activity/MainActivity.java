package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.eventhandler.EventHandler;
import net.chrislehmann.squeezedroid.listadapter.PlayListAdapter;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.util.ImageLoader;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class MainActivity extends SqueezedroidActivitySupport {
	private static final int MENU_ADD_SONG = 0;
	private static final int MENU_ADD_TO_FAVORITES = 1;
	private static final int MENU_SETTINGS = 2;
	private static final int MENU_CHOOSE_PLAYER = 3;

	private PlayListAdapter _playlistListAdapter;
	private ViewSwitcher _coverArtImageView;

	private TextView _songLabel;
	private TextView _artistLabel;
	private TextView _albumLabel;

	private Activity context = this;

	private ImageButton _prevButton;
	private ImageButton _nextButton;
	private ImageButton _playButton;
	private ImageButton _playListButton;
	private ImageButton _libraryButton;
	
	private PlayerStatus _currentStatus;

	android.view.View.OnClickListener onPlayButtonPressed = new android.view.View.OnClickListener() {
		public void onClick(View v) {
			SqueezeService service = ActivityUtils.getService(context);
			if (service != null) {
				service.togglePause(getSqueezeDroidApplication()
						.getSelectedPlayer());
			}
		}
	};

	
	OnClickListener onNextButtonPressed = new android.view.View.OnClickListener() {
		public void onClick(View v) {
			SqueezeService service = ActivityUtils.getService(context);
			if (service != null) {
				service.jump(getSqueezeDroidApplication().getSelectedPlayer(), "+1");
			}
		}
	};
	
	OnClickListener onPrevButtonPressed = new android.view.View.OnClickListener() {
		public void onClick(View v) {
			SqueezeService service = ActivityUtils.getService(context);
			if (service != null) {
				service.jump(getSqueezeDroidApplication().getSelectedPlayer(), "-1");
			}
		}
	};
	
	OnClickListener onToggleVolumeButtonPressed = new OnClickListener() {
		public void onClick(View v) {
			int visibility = View.VISIBLE;
			if( _volumeSeekBar.getVisibility() == View.VISIBLE )
			{
				visibility = View.INVISIBLE;
			}
			_volumeSeekBar.setVisibility( visibility );
			_volumeSeekBar.bringToFront();
		}
	};

	private ImageButton _toggleVolumeButton;
	private SeekBar _volumeSeekBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.tab_layout);

//		_playListView = (ListView) findViewById(R.id.playlist_view);
		_coverArtImageView = (ViewSwitcher) findViewById(R.id.cover_image);
//		_coverArtImageView.setOnItemSelectedListener(onGalleryItemSelected);

		_artistLabel = (TextView) findViewById(R.id.artist_label);
		_albumLabel = (TextView) findViewById(R.id.album_label);
		_songLabel = (TextView) findViewById(R.id.title_label);

		_playButton = (ImageButton) findViewById(R.id.playButton);
		_nextButton = (ImageButton) findViewById(R.id.nextButton);
		_prevButton = (ImageButton) findViewById(R.id.prevButton);
		_playListButton = (ImageButton) findViewById(R.id.playlistButton);
		_libraryButton = (ImageButton) findViewById(R.id.libraryButton);
		_toggleVolumeButton = (ImageButton) findViewById(R.id.toggleVolumeButton);
		_volumeSeekBar = (SeekBar) findViewById(R.id.volume_seek_bar);
		
		_prevButton.setOnClickListener(onPrevButtonPressed);
		_playButton.setOnClickListener(onPlayButtonPressed);
		_nextButton.setOnClickListener(onNextButtonPressed);
		_playListButton.setOnClickListener(onPlaylisyButtonPressed);
		_libraryButton.setOnClickListener(onLibraryButtonPressed);
		_toggleVolumeButton.setOnClickListener(onToggleVolumeButtonPressed);
		if (!isPlayerSelected()) {
			startChoosePlayerActivity();
		} else {
			onServiceConnected();
			onPlayerChanged();
		}
   }
   
	private void onServiceConnected() {
		SqueezeService service = ActivityUtils.getService(context);
		if (service != null) {
		}
	}

	
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
	   SqueezeService service = ActivityUtils.getService(context, false);
	   if( service != null )
	   {
		   service.unsubscribe(SqueezeService.Event.DISCONNECT, null, serverDisconnectedHandler);
		   if( getSelectedPlayer() != null )
		   {
			   service.unsubscribe(SqueezeService.Event.NEWSONG, getSelectedPlayer().getId() , onSongChanged);
		   }
	   }
	   super.onDestroy();
	}

	private void startChoosePlayerActivity() {
		Intent intent = new Intent();
		intent.setAction(SqueezeDroidConstants.Actions.ACTION_CHOOSE_PLAYER);
		this.startActivityForResult(intent, SqueezeDroidConstants.RequestCodes.REQUEST_CHOOSE_PLAYER);
	}

	private void startSettingsActivity() {
		Intent intent = new Intent();
		intent.setAction("net.chrislehmann.squeezedroid.action.EditPreferences");
		this.startActivityForResult(intent, SqueezeDroidConstants.RequestCodes.REQUEST_SHOW_SETTINGS);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SETTINGS, 0, "Settings");
		menu.add(0, MENU_ADD_TO_FAVORITES, 0, "Add To Favorites");
		menu.add(0, MENU_CHOOSE_PLAYER, 0, "Choose Player");
		menu.add(0, MENU_ADD_SONG, 0, "Add Music");
		return true;
	}

	private Player getSelectedPlayer() {
		return ActivityUtils.getSqueezeDroidApplication(context).getSelectedPlayer();
	}

	private boolean isPlayerSelected() {
		return getSqueezeDroidApplication().getSelectedPlayer() != null;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent();
		switch (item.getItemId()) {
		case MENU_ADD_SONG:
			i.setAction("net.chrislehmann.squeezedroid.action.Browse");
			this.startActivity(i);
			return true;
		case MENU_SETTINGS:
			startSettingsActivity();
			return true;
		case MENU_ADD_TO_FAVORITES:
			return true;
		case MENU_CHOOSE_PLAYER:
			startChoosePlayerActivity();
			return true;
		}
		return false;
	}

	private synchronized void updateSongDisplay(final PlayerStatus status) {
		if (status != null) {
			Song currentSong = status.getCurrentSong();

			//Update the image if it has changed...
			if (_currentStatus == null || !_currentStatus.getCurrentSong().getImageUrl().equals(status.getCurrentSong().getImageUrl())) {
				if (_currentStatus != null && _currentStatus.getCurrentIndex() < status.getCurrentIndex() ) {
					_coverArtImageView.setOutAnimation(this, R.anim.slide_out_left);
					_coverArtImageView.setInAnimation(this, R.anim.slide_in_right);
				} else {
					_coverArtImageView.setOutAnimation(this, R.anim.slide_out_right);
					_coverArtImageView.setInAnimation(this, R.anim.slide_in_left);
				}

				ImageView nextView = (ImageView) _coverArtImageView.getNextView();
				nextView.setImageBitmap(null);
				ImageLoader.getInstance().load(nextView, status.getCurrentSong().getImageUrl(), true);
				_coverArtImageView.showNext();
			}

			_songLabel.setText(currentSong.getName());
			_artistLabel.setText(currentSong.getArtist());
			_albumLabel.setText(currentSong.getAlbum());

			_currentStatus = status;
		}
	}

	private void onPlayerChanged() {
		if (getSqueezeDroidApplication().getSelectedPlayer() != null) {
			SqueezeService service = ActivityUtils.getService(this);
			if (service != null) {
				service.unsubscribeAll(SqueezeService.Event.NEWSONG);
				service.subscribe(SqueezeService.Event.NEWSONG, getSqueezeDroidApplication().getSelectedPlayer().getId(), onSongChanged);

				_playlistListAdapter = new PlayListAdapter(service, this, getSqueezeDroidApplication().getSelectedPlayer());
				_playlistListAdapter.setPlayer(getSqueezeDroidApplication().getSelectedPlayer());
				
				PlayerStatus status = ActivityUtils.getService(this).getPlayerStatus(getSqueezeDroidApplication().getSelectedPlayer());
				updateSongDisplay(status);
			}
		}
	}

	EventHandler onSongChanged = new EventHandler() {
		public void onEvent(String result) {
			final PlayerStatus status = ActivityUtils.getService(context).getPlayerStatus(getSqueezeDroidApplication().getSelectedPlayer());
			runOnUiThread(new Thread() {
				public void run() {
					// Cache the next album art
					updateSongDisplay(status);
					BrowseResult<Song> playlist = ActivityUtils.getService(context).getCurrentPlaylist(getSqueezeDroidApplication().getSelectedPlayer(), status.getCurrentIndex(), 2);
					if (playlist.getResutls().size() > 1) {
						ImageLoader.getInstance().load(null, playlist.getResutls().get(1).getImageUrl(), true);
					}
				}

			});
		}
	};

	EventHandler serverDisconnectedHandler = new EventHandler() {
		public void onEvent(String result) {
			// just try and reconnect
			ActivityUtils.getService(context);
		}
	};
	
	OnClickListener onLibraryButtonPressed = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent();
			i.setAction("net.chrislehmann.squeezedroid.action.Browse");
			startActivity(i);
		}
	};
	
	OnClickListener onPlaylisyButtonPressed = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent();
			i.setAction("net.chrislehmann.squeezedroid.action.PlayList");
			startActivity(i);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			finish();
			return;
		}
		switch (requestCode) {
		case SqueezeDroidConstants.RequestCodes.REQUEST_CONNECT:
			Toast.makeText(getApplicationContext(), "Player selected", Toast.LENGTH_SHORT).show();
			onPlayerChanged();
			break;
		case SqueezeDroidConstants.RequestCodes.REQUEST_CHOOSE_PLAYER:
			onPlayerChanged();
			break;
		case SqueezeDroidConstants.RequestCodes.REQUEST_SHOW_SETTINGS:
			onPlayerChanged();
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	};

}