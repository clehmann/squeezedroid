package net.chrislehmann.squeezedroid.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import net.chrislehmann.squeezedroid.service.SqueezeService;


/**
 * Activity that connects to the server (showing a progress dialog).  If a connection
 * cannot be established, a dialog presenting some options (reconnect, show settings, cancel)
 * will be presented to the user.  If no connection can be established, a RESULT_CLOSE_APPLICATION_CHAIN
 * result type will be set, closing the application.
 *
 * @author lehmanc
 */
public class ConnectToServerActivity extends SqueezedroidActivitySupport {
    private static final int DIALOG_ERROR_CONNECTING = 0;
    private static final int DIALOG_CONNECTING = 1;
    private static final int DIALOG_WELCOME = 2;

    private String LOGTAG = "ConnectToServerActivity";

    private ProgressDialog _connectingDialog;

    @Override
    protected void onResume() {
        super.onResume();
        if (!areSettingInitalized()) {
            Log.d(LOGTAG, "Settings not initalized, showing welcome dialog");
            showDialog(DIALOG_WELCOME);
        } else {
            startConnectionThread();
        }
    }

    private OnCancelListener onConnectionCanceledListener = new OnCancelListener() {
        public void onCancel(DialogInterface dialog) {
            runOnUiThread(new Runnable() {
                public void run() {
                    cancelConnectionThread();
                    showDialog(DIALOG_ERROR_CONNECTING);
                }
            });
        }
    };

    private void startConnectionThread() {
        showDialog(DIALOG_CONNECTING);
        Log.d(LOGTAG, "Starting connection thread");
        _connectionThread = new ConnectionThread();
        _connectionThread.start();
    }

    private void cancelConnectionThread() {
        removeDialog(DIALOG_CONNECTING);
        Log.d(LOGTAG, "Cancelling connection thread");
        if (_connectionThread != null) {
            _connectionThread.interrupt();
            _connectionThread = null;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        cancelConnectionThread();
    }

    private SqueezedroidActivitySupport context = this;

    private ConnectionThread _connectionThread;

    /**
     * Thread responsible for initiating the connection to the server.
     *
     * @author lehmanc
     */
    private class ConnectionThread extends Thread {
        public void run() {
            Log.d(LOGTAG, "Starting connection thread");
            final Thread thisThread = this;
            SqueezeService service = null;
            try {
                service = getSqueezeDroidApplication().getConnectionManager().getService(context);
                if (!service.isConnected()) {
                    //throw new ApplicationException("bad", null);
                    Log.d(LOGTAG, "Attempting to connect to server");
                    service.connect();
                    Log.d(LOGTAG, "Connected to server");
                }

                removeDialog(DIALOG_CONNECTING);
                setResult(RESULT_OK);
                finish();
                Log.d(LOGTAG, "Finished");
            } catch (Exception e) {
                Log.e(LOGTAG, "Error connecting to server: ", e);

                if (!interrupted() && !isFinishing()) {
                    getSqueezeDroidApplication().resetService();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            removeDialog(DIALOG_CONNECTING);
                            showDialog(DIALOG_ERROR_CONNECTING);
                        }
                    });
                }
            }
        }
    }

    ;


    public boolean areSettingInitalized() {
        boolean initalized = true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String serverIp = prefs.getString("server_ip", "not_initalized");

        if ("not_initalized".equals(serverIp)) {
            initalized = false;
        }

        return initalized;
    }

    private IntentResultCallback settingsCallback = new IntentResultCallback() {

        public void resultOk(String resultString, Bundle resultMap) {
            getSqueezeDroidApplication().resetService();
            _connectionThread = new ConnectionThread();
            _connectionThread.start();
        }

        public void resultCancel(String resultString, Bundle resultMap) {
            resultOk(resultString, resultMap);
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = null;
        switch (id) {
            case DIALOG_ERROR_CONNECTING:
                d = createErrorConnectingDialog();
                break;
            case DIALOG_CONNECTING:
                _connectingDialog = ProgressDialog.show(this, "Connecting...", "Connecting to squeezeserver.", true, true);
                _connectingDialog.setOnCancelListener(onConnectionCanceledListener);
                d = _connectingDialog;
                break;
            case DIALOG_WELCOME:
                d = createWelcomeDialog();
        }
        return d;
    }


    @Override
    public void finish() {
        Log.d(LOGTAG, "Finished called...!");
        super.finish();    //To change body of overridden methods use File | Settings | File Templates.
    }

    private Dialog createWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome");
        builder.setMessage("Welcome to SqueezeDroid.  Before you can start, you'll need to enter some information about your server.");
        builder.setPositiveButton("Configure", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                launchSubActivity(EditPrefrencesActivity.class, settingsCallback);
            }
        });
        return builder.create();
    }

    private Dialog createErrorConnectingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error Connecting");
        builder.setMessage("There was an error connecting to the server.");
        builder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.d(LOGTAG, "Dialog canceled, closing application");
                cancelConnectionThread();
                getSqueezeDroidApplication().resetService();
                closeApplication();
            }
        });
        builder.setPositiveButton("Reconnect", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOGTAG, "Dialog finished, attempting to reconnect");
                startConnectionThread();
            }
        });
        builder.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOGTAG, "Dialog finished, closing application");
                cancelConnectionThread();
                getSqueezeDroidApplication().resetService();
                closeApplication();
            }
        });
        builder.setNeutralButton("Settings", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOGTAG, "Dialog finished, staring settings");
                launchSubActivity(EditPrefrencesActivity.class, settingsCallback);
            }
        });

        return builder.create();
    }
}
