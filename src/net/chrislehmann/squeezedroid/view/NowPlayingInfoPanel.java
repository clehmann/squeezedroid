package net.chrislehmann.squeezedroid.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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

    private TextView _songLabel;
    private TextView _artistLabel;
    private TextView _albumLabel;

    private SqueezedroidActivitySupport _parent;

    public NowPlayingInfoPanel(Context context) {
        super(context);
        initalize();
    }

    private void initalize() {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.now_playing_info_layout, this);

        _artistLabel = (TextView) findViewById(R.id.artist_label);
        _albumLabel = (TextView) findViewById(R.id.album_label);
        _songLabel = (TextView) findViewById(R.id.title_label);
    }


    public NowPlayingInfoPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
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
                service.subscribe(_parent.getSelectedPlayer(), onPlayerStatusChanged);
                PlayerStatus status = service.getPlayerStatus(_parent.getSelectedPlayer());
                updateStatus(status);
            }
        });
    }

    public void setSong(Song song) {
        _songLabel.setText(song != null ? song.getName() : "");
        _artistLabel.setText(song != null ? song.getArtist() : "");
        _albumLabel.setText(song != null ? song.getAlbum() : "");
    }


    private void updateStatus(PlayerStatus status) {
        if (status != null && status.getCurrentSong() != null) {
            setSong(status.getCurrentSong());
        } else {
            setSong(null);
        }
    }

    /**
     * {@link net.chrislehmann.squeezedroid.service.PlayerStatusHandler} to handle events from the
     * {@link net.chrislehmann.squeezedroid.service.SqueezeService}.  This will update the ui
     * when the song changes
     */
    private PlayerStatusHandler onPlayerStatusChanged = new SimplePlayerStatusHandler() {
        public void onSongChanged(final PlayerStatus status) {
            final BrowseResult<Song> playlist = _parent.getService().getCurrentPlaylist(_parent.getSelectedPlayer(), status.getCurrentIndex(), 2);
            _parent.runOnUiThread(new Thread() {
                public void run() {
                    updateStatus(status);
                }
            });
        }
    };


}
