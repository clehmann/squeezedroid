package net.chrislehmann.squeezedroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.listadapter.PlayListAdapter;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.RepeatMode;
import net.chrislehmann.squeezedroid.model.ShuffleMode;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.PlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.ServerStatusHandler;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.service.SimplePlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.view.NowPlayingInfoPanel;
import net.chrislehmann.squeezedroid.view.PlayerSyncPanel;
import net.chrislehmann.squeezedroid.view.TransparentPanel;
import net.chrislehmann.squeezedroid.view.UpdatingSeekBar;
import net.chrislehmann.util.ImageLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Main activity of the SqueezeDroid application.  This contains a view of the current player's status.
 * And some playback controls.
 *
 * @author lehmanc
 */
@SuppressWarnings("serial")
public class MainActivity extends SqueezedroidActivitySupport {
    protected MainActivity context = this;

    private static final String LOGTAG = "MainActivity";

    /**
     * Menu Constants
     */
    private static final int MENU_SETTINGS = 1;
    private static final int MENU_CHOOSE_PLAYER = 2;
    private static final int MENU_SYNC_PLAYER = 3;
    private static final int MENU_PLAYLIST = 4;
    private static final int MENU_LIBRARY = 5;

    /**
     * Views
     */
    private PlayListAdapter _playlistListAdapter;
    private ViewSwitcher _coverArtImageView;

    private NowPlayingInfoPanel _nowPlayingInfoPanel;

    private TextView _noSongSelectedText;

    private ImageButton _prevButton;
    private ImageButton _nextButton;
    private ImageButton _playButton;
    private ImageButton _shuffleButton;
    private ImageButton _repeatButton;
    private ImageButton _toggleSyncPanelButton;

    private PlayerSyncPanel _syncPanel;
    private TransparentPanel _volumePanel;
    private UpdatingSeekBar _timeSeekBar;

    private PlayerStatus _currentStatus;

    /**
     * {@link Activity} Lifecycle overrides
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setContentView(R.layout.main_layout);

        _coverArtImageView = (ViewSwitcher) findViewById(R.id.cover_image);

        _syncPanel = new PlayerSyncPanel(context);

        _nowPlayingInfoPanel = (NowPlayingInfoPanel) findViewById(R.id.song_info_container);
        _nowPlayingInfoPanel.setParent(this);

        _noSongSelectedText = (TextView) findViewById(R.id.no_song_selected_text);
        _playButton = (ImageButton) findViewById(R.id.playButton);
        _nextButton = (ImageButton) findViewById(R.id.nextButton);
        _prevButton = (ImageButton) findViewById(R.id.prevButton);
        _shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        _repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        _toggleSyncPanelButton = (ImageButton) findViewById(R.id.toggleVolumeButton);
        _timeSeekBar = new UpdatingSeekBar((SeekBar) findViewById(R.id.timeSeekBar));
        _volumePanel = (TransparentPanel) findViewById(R.id.volume_panel);

        _timeSeekBar.setOnSeekBarChangeListener(onTimeUpdatedByUser);
        _prevButton.setOnClickListener(onPrevButtonPressed);
        _playButton.setOnClickListener(onPlayButtonPressed);
        _nextButton.setOnClickListener(onNextButtonPressed);
        _shuffleButton.setOnClickListener(onShuffleButtonPressed);
        _repeatButton.setOnClickListener(onRepeatButtonPressed);
        _toggleSyncPanelButton.setOnClickListener(onToggleSyncPanelButtonPressed);

        _volumePanel.addView(_syncPanel);

    }

    @Override
    protected void onResume() {
        if (!closing) {
            forceConnect();
            if (getSelectedPlayer() != null) {
                onPlayerChanged();
                runWithService(new SqueezeServiceAwareThread() {
                    public void runWithService(SqueezeService service) {
                        service.subscribe(onServiceStatusChanged);
                    }
                });
            }
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        runWithService(new SqueezeServiceAwareThread() {
            public void runWithService(SqueezeService service) {
                service.unsubscribeAll(onPlayerStatusChanged);
            }
        }, false);

        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent();
        switch (item.getItemId()) {
            case R.id.menuItem_Library:
                launchSubActivity(BrowseRootActivity.class, null);
                return true;
            case R.id.menuItem_Playlist:
                launchSubActivity(PlayListActivity.class, null);
                return true;
            case R.id.menuItem_Settings:
                launchSubActivity(EditPrefrencesActivity.class, editSettingsIntentCallback);
                return true;
            case R.id.menuItem_Choose:
                i.setAction(SqueezeDroidConstants.Actions.ACTION_CHOOSE_PLAYER);
                i.putExtra(SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_INCLUDE_SELECTED_PLAYER, true);
                i.putExtra(SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_REMOVE_DUPLICATE_PLAYERS, false);
                launchSubActivity(i, choosePlayerIntentCallback);
                return true;
            case R.id.menuItem_Sync:
                i.setAction(SqueezeDroidConstants.Actions.ACTION_CHOOSE_PLAYER);
                i.putExtra(SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_INCLUDE_SELECTED_PLAYER, false);
                i.putExtra(SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_REMOVE_DUPLICATE_PLAYERS, true);
                i.putExtra(SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_EMPTY_PLAYER_NAME, "No Synchronization");
                i.putExtra(SqueezeDroidConstants.IntentDataKeys.KEY_DIALOG_NAME, "Choose Player to Synchronize With");
                launchSubActivity(i, choosePlayerForSyncCallback);
                return true;
            case R.id.menuItem_Power:
                runWithService(new SqueezeServiceAwareThread() {
                    public void runWithService(SqueezeService service) {
                        service.togglePower(getSelectedPlayer());
                    }
                });
        }
        return false;
    }

    /**
     * View OnClick listeners
     */
    OnClickListener onPlayButtonPressed = new OnClickListener() {
        public void onClick(View v) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.togglePause(getSelectedPlayer());
                }
            });
        }
    };

    OnClickListener onNextButtonPressed = new OnClickListener() {
        public void onClick(View v) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.jump(getSelectedPlayer(), "+1");
                }
            });
        }
    };

    OnClickListener onPrevButtonPressed = new OnClickListener() {
        public void onClick(View v) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.jump(getSelectedPlayer(), "-1");
                }
            });
        }
    };

    OnClickListener onToggleSyncPanelButtonPressed = new OnClickListener() {
        public void onClick(View v) {
            int visibility = View.VISIBLE;
            int imageId = R.drawable.up;
            if (_volumePanel.getVisibility() == View.VISIBLE) {
                imageId = R.drawable.down;
                visibility = View.INVISIBLE;
            }
            _toggleSyncPanelButton.setImageResource(imageId);
            _volumePanel.setVisibility(visibility);
            _volumePanel.bringToFront();
        }
    };

    OnClickListener onShuffleButtonPressed = new OnClickListener() {
        private Map<ShuffleMode, ShuffleMode> nextShuffleModeMap = new HashMap<ShuffleMode, ShuffleMode>() {{
            put(ShuffleMode.NONE, ShuffleMode.SONG);
            put(ShuffleMode.SONG, ShuffleMode.ALBUM);
            put(ShuffleMode.ALBUM, ShuffleMode.NONE);
        }};

        public void onClick(View v) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    if (getSelectedPlayer() != null && _currentStatus != null) {
                        ShuffleMode nextMode = nextShuffleModeMap.get(_currentStatus.getShuffleMode());
                        if (nextMode != null && getSelectedPlayer() != null) {
                            service.setShuffleMode(getSelectedPlayer(), nextMode);
                        }
                    }
                }
            });
        }

        ;
    };

    OnClickListener onRepeatButtonPressed = new OnClickListener() {
        private Map<RepeatMode, RepeatMode> nextRepeatModeMap = new HashMap<RepeatMode, RepeatMode>() {
            {
                put(RepeatMode.NONE, RepeatMode.SONG);
                put(RepeatMode.SONG, RepeatMode.ALL);
                put(RepeatMode.ALL, RepeatMode.NONE);
            }
        };

        public void onClick(View v) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    if (getSelectedPlayer() != null && _currentStatus != null) {
                        RepeatMode nextMode = nextRepeatModeMap.get(_currentStatus.getRepeatMode());
                        if (nextMode != null) {
                            service.setRepeatMode(getSelectedPlayer(), nextMode);
                        }
                    }
                }
            });
        }

    };

    OnSeekBarChangeListener onTimeUpdatedByUser = new OnSeekBarChangeListener() {
        private int time = 0;

        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.v(LOGTAG, "User changed time seek bar position to " + time);
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.seekTo(getSelectedPlayer(), time);
                }
            });
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                time = progress;
            }
        }
    };

    private IntentResultCallback choosePlayerForSyncCallback = new IntentResultCallback() {
        public void resultOk(String resultString, final Bundle resultMap) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    String selectedPlayer = resultMap.getString(SqueezeDroidConstants.IntentDataKeys.KEY_SELECTED_PLAYER);
                    if (selectedPlayer != null) {
                        service.synchronize(getSelectedPlayer(), selectedPlayer);
                    } else {
                        service.unsynchronize(getSelectedPlayer());
                    }
//                    setSelectedPlayer(selectedPlayer);
                    _syncPanel.setPlayer(selectedPlayer);
                }
            });
        }

        public void resultCancel(String resultString, Bundle resultMap) {
        }
    };

    private IntentResultCallback editSettingsIntentCallback = new IntentResultCallback() {
        public void resultOk(String resultString, Bundle resultMap) {
            getSqueezeDroidApplication().resetService();
        }

        public void resultCancel(String resultString, Bundle resultMap) {
            getSqueezeDroidApplication().resetService();
        }
    };


    /**
     * Called when the currently selected {@link Player} has changed
     */
    private void onPlayerChanged() {
        runWithService(new SqueezeServiceAwareThread() {
            public void runWithService(final SqueezeService service) {

                final PlayerStatus status = service.getPlayerStatus(getSelectedPlayer());
                runOnUiThread(new Runnable() {
                    public void run() {
                        _syncPanel.setPlayer(getSelectedPlayer());

                        service.unsubscribeAll(onPlayerStatusChanged);
                        service.subscribe(getSelectedPlayer(), onPlayerStatusChanged);

                        _playlistListAdapter = new PlayListAdapter(service, context, getSelectedPlayer());
                        _playlistListAdapter.setPlayer(getSelectedPlayer());

                        updateSongDisplay(status);
                    }
                });
            }
        });
        _nowPlayingInfoPanel.setParent(this);
    }


    /**
     * Called to update the main screen to display information about a new song
     *
     * @param status
     */
    private synchronized void updateSongDisplay(final PlayerStatus status) {
        if (status != null && status.getCurrentSong() != null) {
            _noSongSelectedText.setVisibility(View.INVISIBLE);
            _coverArtImageView.setVisibility(View.VISIBLE);

            Song currentSong = status.getCurrentSong();

            if (hasCoverImageChanged(status)) {
                if (_currentStatus != null && _currentStatus.getCurrentIndex() <= status.getCurrentIndex()) {
                    _coverArtImageView.setOutAnimation(this, R.anim.slide_out_left);
                    _coverArtImageView.setInAnimation(this, R.anim.slide_in_right);
                } else {
                    _coverArtImageView.setOutAnimation(this, R.anim.slide_out_right);
                    _coverArtImageView.setInAnimation(this, R.anim.slide_in_left);
                }

                ImageView nextView = (ImageView) _coverArtImageView.getNextView();
                if (status.getCurrentSong().getImageUrl() != null) {
                    nextView.setImageBitmap(null);
                    ImageLoader.getInstance().load(nextView, status.getCurrentSong().getImageUrl());
                } else {
                    nextView.setImageResource(R.drawable.default_album);
                }
                _coverArtImageView.showNext();

            }

            if (status.isPlaying()) {
                _playButton.setImageResource(R.drawable.pause);
            } else {
                _playButton.setImageResource(R.drawable.play);
            }

            _timeSeekBar.setMax(currentSong.getDurationInSeconds());
            _timeSeekBar.setProgress(status.getCurrentPosition());
            if (status.isPlaying()) {
                _timeSeekBar.start();
            }

            _currentStatus = status;
            updateRepeatMode(status.getRepeatMode());
            updateShuffleMode(status.getShuffleMode());
        } else {
            ImageView nextView = (ImageView) _coverArtImageView.getNextView();
            nextView.setImageBitmap(null);
            _coverArtImageView.showNext();


            _timeSeekBar.pause();
            _timeSeekBar.setProgress(0);
            _coverArtImageView.setVisibility(View.INVISIBLE);
            _noSongSelectedText.setVisibility(View.VISIBLE);
        }
    }

    private boolean hasCoverImageChanged(final PlayerStatus newStatus) {
        boolean coverImageHasChanged = false;
        if (_currentStatus == null) {
            coverImageHasChanged = true;
        } else if (_currentStatus.getCurrentSong().getImageUrl() == null && newStatus.getCurrentSong().getImageUrl() != null) {
            coverImageHasChanged = true;
        } else if (_currentStatus.getCurrentSong().getImageUrl() != null) {
            coverImageHasChanged = !_currentStatus.getCurrentSong().getImageUrl().equals(newStatus.getCurrentSong().getImageUrl());
        }
        return coverImageHasChanged;
    }

    private ServerStatusHandler onServiceStatusChanged = new ServerStatusHandler() {
        public void onDisconnect() {
            //Just try and reconnect...
            getSqueezeDroidApplication().resetService();
            forceConnect();
        }
    };

    /**
     * {@link PlayerStatusHandler} to handle events from the {@link SqueezeService}.  This will update the ui based on
     * various status change events from the current player
     */
    private PlayerStatusHandler onPlayerStatusChanged = new SimplePlayerStatusHandler() {
        public void onSongChanged(final PlayerStatus status) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    final BrowseResult<Song> playlist = service.getCurrentPlaylist(getSelectedPlayer(), status.getCurrentIndex(), 2);
                    runOnUiThread(new Thread() {
                        public void run() {
                            // Cache the next album art
                            updateSongDisplay(status);
                            if (playlist.getResutls().size() > 1 && playlist.getResutls().get(1).getImageUrl() != null) {
                                ImageLoader.getInstance().load(null, playlist.getResutls().get(1).getImageUrl());
                            }
                        }
                    });
                }
            });

        }

        public void onPlay() {
            runOnUiThread(new Runnable() {
                public void run() {
                    _playButton.setImageResource(R.drawable.pause);
                    _timeSeekBar.start();
                }
            });
        }

        public void onPause() {
            runOnUiThread(new Runnable() {
                public void run() {
                    _playButton.setImageResource(R.drawable.play);
                    _timeSeekBar.pause();
                }
            });
        }

        public void onStop() {
            onPause();
        }

        public void onDisconnect() {
            setSelectedPlayer(null);
            getSelectedPlayer();
        }

        ;

        public void onRepeatModeChanged(RepeatMode newMode) {
            updateRepeatMode(newMode);
        }

        ;

        public void onShuffleModeChanged(ShuffleMode newMode) {
            updateShuffleMode(newMode);
        }

        ;
    };

    private Map<ShuffleMode, Integer> shuffleModeToIconMap = new HashMap<ShuffleMode, Integer>() {{
        put(ShuffleMode.ALBUM, R.drawable.shuffle_album);
        put(ShuffleMode.NONE, R.drawable.shuffle_off);
        put(ShuffleMode.SONG, R.drawable.shuffle_all);

    }};

    private void updateShuffleMode(final ShuffleMode newMode) {
        _currentStatus.setShuffleMode(newMode);
        runOnUiThread(new Runnable() {
            public void run() {
                _shuffleButton.setImageResource(shuffleModeToIconMap.get(newMode));
            }
        });
    }

    private Map<RepeatMode, Integer> repeatModeToIconMap = new HashMap<RepeatMode, Integer>() {{
        put(RepeatMode.ALL, R.drawable.repeat_all);
        put(RepeatMode.NONE, R.drawable.repeat_off);
        put(RepeatMode.SONG, R.drawable.repeat_song);

    }};

    private void updateRepeatMode(final RepeatMode newMode) {
        _currentStatus.setRepeatMode(newMode);
        runOnUiThread(new Runnable() {
            public void run() {
                _repeatButton.setImageResource(repeatModeToIconMap.get(newMode));
            }
        });
    }
}