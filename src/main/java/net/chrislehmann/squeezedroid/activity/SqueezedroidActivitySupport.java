package net.chrislehmann.squeezedroid.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.DownloadService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.service.SqueezeService;

import java.util.List;

/**
 * Base activity that contains some methods to manage the {@link SqueezeService} and the
 * currently selected {@link Player} objects.
 *
 * @author lehmanc
 */
public class SqueezedroidActivitySupport extends ActivitySupport {
    //private static final String LOGTAG = "SqueezeDroidActivitySupport";


    /**
     * {@link BroadcastReceiver} to listen for connection changes and re-connect the service.
     */
    BroadcastReceiver onConnectionChanged = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isDisconnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (isDisconnected) {
                getSqueezeDroidApplication().resetService();
            }
        }
    };
    private boolean lookingForPlayer = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(onConnectionChanged, filter);

        String dialogName = getIntent().getStringExtra(SqueezeDroidConstants.IntentDataKeys.KEY_DIALOG_NAME);
        if (dialogName != null) {
            setTitle(dialogName);
        }

    }

    /**
     * Gets the currently selected player.  This will try the following, in this order:
     * <p/>
     * 1) Use the player held in the {@link SqueezeDroidApplication}
     * 2) Retrieve the last used player from this application's {@link SharedPreferences} and load that
     * from ther server
     * 3) Start an activity that will prompt the user to choose a player
     */
    public String getSelectedPlayer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        String selectedPlayerId = prefs.getString(SqueezeDroidConstants.Preferences.LAST_SELECTED_PLAYER, null);
        if (!lookingForPlayer && selectedPlayerId == null && !getSqueezeDroidApplication().getConnectionManager().isConnecting()) {
            launchSubActivity(ChoosePlayerActivity.class, choosePlayerIntentCallback);
            lookingForPlayer = true;
        }
        return selectedPlayerId;
    }


    /**
     * Sets the currently selected player.
     *
     * @player the currently selected player
     */
    protected void setSelectedPlayer(String playerId) {
        if (playerId != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(SqueezeDroidConstants.Preferences.LAST_SELECTED_PLAYER, playerId);
            editor.commit();
        }

    }

    /**
     * Returns true if a player is selected
     */
    protected boolean isPlayerSelected() {
        return getSelectedPlayer() != null;
    }

    /**
     * Helper method to simply get the application and cast it to a {@link SqueezeDroidApplication}
     *
     * @return
     */
    public SqueezeDroidApplication getSqueezeDroidApplication() {
        return (SqueezeDroidApplication) getApplication();
    }


    /**
     * Ensures that the {@link SqueezeService} is connected (possibly by forwarding to the {@link ConnectToServerActivity} and
     * calls the {@link SqueezeServiceAwareThread#runWithService(SqueezeService)} with a connected {@link SqueezeService}
     *
     * @param onConnect {@link SqueezeServiceAwareThread} to run after the server connection has been obtained.
     */
    public void runWithService(final SqueezeServiceAwareThread onConnect) {
        if( !closing )
        {
            getSqueezeDroidApplication().getConnectionManager().getService(this, true, onConnect);
        }
    }


    protected void runWithService(SqueezeServiceAwareThread onConnect, boolean connectIfDisconnected) {
        if( !closing )
        {
            getSqueezeDroidApplication().getConnectionManager().getService(this, connectIfDisconnected, onConnect);
        }
    }


    /**
     * Force a connection to the squeezeserver if we are not already connected
     */
    protected void forceConnect() {
        runWithService(null);
    }

    /**
     * Child Activity callback {@link IntentResultCallback}s
     */
    protected IntentResultCallback choosePlayerIntentCallback = new IntentResultCallback() {
        public void resultOk(String resultString, Bundle resultMap) {

            String selectedPlayer = (String) resultMap.getSerializable(SqueezeDroidConstants.IntentDataKeys.KEY_SELECTED_PLAYER);
            if (selectedPlayer == null) {
                closeApplication();
            }
            setSelectedPlayer(selectedPlayer);
            lookingForPlayer = false;
        }

        public void resultCancel(String resultString, Bundle resultMap) {
            if (getSelectedPlayer() == null) {
                closeApplication();
            }

        }
    };

    protected void addDownloadsForItem(final Item selectedItem) {
        runWithService(new SqueezeServiceAwareThread() {
            public void runWithService(SqueezeService service) {
                List<Song> songs = service.getSongsForItem(selectedItem);
                for (Song song : songs) {
                    addDownload(song.getUrl(), Environment.getExternalStorageDirectory() + "/music/" + song.getLocalPath());
                }
            }
        });
    }


    protected void addDownload(String url, String path) {
        Intent i = new Intent(this, DownloadService.class);
        i.putExtra(DownloadService.DOWNLOAD_SERVICE_REQUESTED_URL, url);
        i.putExtra(DownloadService.DOWNLOAD_SERVICE_REQUESTED_PATH, path);
        startService(i);
    }


}
