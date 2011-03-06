package net.chrislehmann.squeezedroid.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import net.chrislehmann.squeezedroid.activity.ActivitySupport.IntentResultCallback;
import net.chrislehmann.squeezedroid.activity.ConnectToServerActivity;
import net.chrislehmann.squeezedroid.activity.SqueezeDroidConstants;
import net.chrislehmann.squeezedroid.activity.SqueezedroidActivitySupport;

import java.util.ArrayList;
import java.util.List;

public class ServiceConnectionManager {


    public enum Status {CONNECTED, CONNECTING, DISCONNECTED, CLOSED}

    private Status currentStatus = Status.DISCONNECTED;
    private List<SqueezeServiceAwareThread> onConnectQueue = new ArrayList<SqueezeServiceAwareThread>();

    /**
     * Interface that simply contains a callback that will be executed within the context of a valid, connected {@link SqueezeService}
     *
     * @author lehmanc
     */
    public interface SqueezeServiceAwareThread {
        public void runWithService(SqueezeService service);
    }

    private static final String LOGTAG = "SqueezeServiceAwareThread";

    private SqueezeService service;

    public void disconnect() {
        if (service != null && service.isConnected()) {
            try {
                service.disconnect();
            } catch (Exception e) {
                Log.e(LOGTAG, "Error disconnecting from service", e);
            }
        }
        currentStatus = Status.CLOSED;
        service = null;
    }

    /**
     * Gets the {@link SqueezeService}.  If the connect parameter is set to true and the {@link SqueezeService} is not connected,
     * this method will start the {@link ConnectToServerActivity} and return null.  Your code should take this into account.
     *
     * @param connect   If set to true, this method will attempt to connect the {@link SqueezeService} by starting the
     *                  {@link ConnectToServerActivity} and return null
     * @param onConnect A {@link SqueezeServiceAwareThread} that will be executed when the server is connected.  If the server is
     *                  already connected, this will be executed immediately.
     */
    public void getService(SqueezedroidActivitySupport context, boolean connect, final SqueezeServiceAwareThread onConnect) {
        final SqueezeService service = getService(context);
        if (connect && (service == null || !service.isConnected())) {
            if (currentStatus != Status.CONNECTING) {
                currentStatus = Status.CONNECTING;
                Intent intent = new Intent();
                intent.setAction(SqueezeDroidConstants.Actions.ACTION_CONNECT);
                context.launchSubActivity(ConnectToServerActivity.class, new ExecuteWithServiceCallback(context));
            }
            synchronized (onConnectQueue) {
                if (onConnect != null) {
                    onConnectQueue.add(onConnect);
                }
            }
        } else if (service != null && service.isConnected()) {
            //Already connected, just run the onConnectHandler they passed in (on a thread)
            if (onConnect != null) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            onConnect.runWithService(service);
                        } catch (Exception e) {
                            Log.e(LOGTAG, "Error executing callback", e);
                        }
                    }
                }.start();
            }
        }
    }

    private class ExecuteWithServiceCallback implements IntentResultCallback {
        private SqueezedroidActivitySupport context;


        public ExecuteWithServiceCallback(final SqueezedroidActivitySupport context) {
            this.context = context;
        }

        public void resultOk(String resultString, Bundle resultMap) {
            try {
                currentStatus = Status.CONNECTED;
                synchronized (onConnectQueue) {
                    for (SqueezeServiceAwareThread thread : onConnectQueue) {
                        if (thread != null) {
                            try {
                                thread.runWithService(service);
                            } catch (Exception e) {
                                Log.e(LOGTAG, "Error executing callback", e);
                            }
                        }
                    }
                    onConnectQueue.clear();
                }

            } catch (Exception e) {
                Log.e(LOGTAG, "Error executing callback", e);
            }
        }

        public void resultCancel(String resultString, Bundle resultMap) {
            context.closeApplication();
        }
    }

    ;

    public SqueezeService getService(SqueezedroidActivitySupport context) {
        if (service == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getBaseContext());
            String serverIp = prefs.getString("server_ip", "bonk");
            String serverWebPort = prefs.getString("server_web_port", "9000");
            String serverCliPort = prefs.getString("server_cli_port", "9090");

            CliSqueezeService service = new CliSqueezeService(serverIp, Integer.parseInt(serverCliPort), Integer.parseInt(serverWebPort));
            service.setUsername(prefs.getString("authentication_username", ""));
            service.setPassword(prefs.getString("authentication_password", ""));

            this.service = service;
        }

        return service;
    }

    public boolean isConnecting() {
        return Status.CONNECTING.equals(currentStatus);
    }
}
