package net.chrislehmann.squeezedroid.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.activity.SqueezedroidActivitySupport;
import net.chrislehmann.squeezedroid.model.Song;

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

//    public void setParent(SqueezedroidActivitySupport parent) {
//        _parent = parent;
//    }

    public void setSong(Song song) {
         _songLabel.setText( song != null ? song.getName() : "" );
         _artistLabel.setText( song != null ? song.getArtist() : "" );
         _albumLabel.setText( song != null ? song.getAlbum() : "" );
    }

}
