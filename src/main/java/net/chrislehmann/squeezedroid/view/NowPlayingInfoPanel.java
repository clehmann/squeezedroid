package net.chrislehmann.squeezedroid.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.activity.SqueezedroidActivitySupport;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.PlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager;
import net.chrislehmann.squeezedroid.service.SimplePlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SqueezeService;

/**
 * View that contains information about a song.  Also contains logic to update that information when the song changes
 */
public class NowPlayingInfoPanel extends RelativeLayout {

    private static final String LOGTAG = "NowPlayingInfoPanel";
    protected static final String CONFIG_ATTRIBUTE_NAMESPACE = "android";
    private static final String SHOW_CONTROLS_ATTRIBUTE = "show_controls";

    private TextView _songLabel;
    private TextView _artistLabel;
    private TextView _albumLabel;


    private boolean _showControls = false;

    private SqueezedroidActivitySupport _parent;

    public NowPlayingInfoPanel(Context context) {
        super(context);
        initalize();
    }

    private void initalize() {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(_showControls ? R.layout.now_playing_info_with_controls_layout : R.layout.now_playing_info_layout, this);

        _artistLabel = (TextView) findViewById(R.id.artist_label);
        _albumLabel = (TextView) findViewById(R.id.album_label);
        _songLabel = (TextView) findViewById(R.id.title_label);
        if (_showControls) {
            findViewById(R.id.skip_next_button).setOnClickListener(onNextButtonClicked);
            findViewById(R.id.play_button).setOnClickListener(onPlayButtonClicked);
        }
    }


    public NowPlayingInfoPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NowPlayingInfoPanel);
        _showControls = typedArray.getBoolean(R.styleable.NowPlayingInfoPanel_show_controls, false);
        initalize();
    }

    public void setParent(SqueezedroidActivitySupport parent) {
        if (_parent != null) {
            _parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.unsubscribeAll(onPlayerStatusChanged);
                }
            });
        }
        _parent = parent;

        _parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
            public void runWithService(SqueezeService service) {
                String selectedPlayer = _parent.getSelectedPlayer();
                if (selectedPlayer != null) {
                    service.subscribe(selectedPlayer, onPlayerStatusChanged);
                    PlayerStatus status = service.getPlayerStatus(_parent.getSelectedPlayer());
                    updateStatus(status);
                }
            }
        });
    }

    public void setSong(Song song) {
        _songLabel.setText(song != null ? song.getName() : "");
        _artistLabel.setText(song != null ? song.getArtist() : "");
        _albumLabel.setText(song != null ? song.getAlbum() : "");
    }


    private void updateStatus(final PlayerStatus status) {
        if (status != null) {
            _parent.runOnUiThread(new Runnable() {
                public void run() {
                        if (status.getCurrentSong() != null) {
                        setSong(status.getCurrentSong());
                    } else {
                        setSong(null);
                    }
                    ImageButton playButton = (ImageButton) findViewById(R.id.play_button);
                    if (playButton != null) {
                        playButton.setImageResource(status.isPlaying() ? R.drawable.pause : R.drawable.play);
                    }
                    ;
                }
            });

        }
    }

    /**
     * {@link net.chrislehmann.squeezedroid.service.PlayerStatusHandler} to handle events from the
     * {@link net.chrislehmann.squeezedroid.service.SqueezeService}.  This will update the ui
     * when the song changes
     */
    private PlayerStatusHandler onPlayerStatusChanged = new SimplePlayerStatusHandler() {
        public void onSongChanged(final PlayerStatus status) {
            _parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    final BrowseResult<Song> playlist = service.getCurrentPlaylist(_parent.getSelectedPlayer(), status.getCurrentIndex(), 2);
                    _parent.runOnUiThread(new Thread() {
                        public void run() {
                            updateStatus(status);
                        }
                    });
                }
            });

        }

        public void onPlay() {
            _parent.runOnUiThread(new Runnable() {
                public void run() {
                    ImageButton playButton = (ImageButton) findViewById(R.id.play_button);
                    if (playButton != null) {
                        playButton.setImageResource(R.drawable.pause);
                    }
                    ;
                }
            });
        }

        public void onPause() {
            _parent.runOnUiThread(new Runnable() {
                public void run() {
                    ImageButton playButton = (ImageButton) findViewById(R.id.play_button);
                    if (playButton != null) {
                        playButton.setImageResource(R.drawable.play);
                    }
                    ;
                }
            });
        }
    };


    private OnClickListener onNextButtonClicked = new OnClickListener() {
        public void onClick(View view) {
            _parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.jump(_parent.getSelectedPlayer(), "+1");
                }
            });
        }
    };


    private OnClickListener onPlayButtonClicked = new OnClickListener() {
        public void onClick(View view) {
            _parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.togglePause(_parent.getSelectedPlayer());
                }
            });
        }
    };

}
