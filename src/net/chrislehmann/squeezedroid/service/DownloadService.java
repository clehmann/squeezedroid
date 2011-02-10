package net.chrislehmann.squeezedroid.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import net.chrislehmann.squeezedroid.activity.MainActivity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DownloadService extends Service {
    public static final String DOWNLOAD_SERVICE_REQUESTED_PATH = "DownloadServiceRequestedPath";
    public static final String DOWNLOAD_SERVICE_REQUESTED_URL = "DownloadServiceRequestedUrl";

    private NotificationManager _notificationManager;
    private static final int NOTIFICATION_DOWNLOAD = 1;
    private static final int NOTIFICATION_DOWNLOAD_ONGOING = 2;

    private static final String LOGTAG = "DownloadService";

    private int _numberDownloaded = 0;
    private int _numberQueued = 0;

    private BlockingQueue<DownloadRequest> _queue = new LinkedBlockingQueue<DownloadRequest>();


    private Thread _downloaderThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    DownloadRequest request = _queue.poll();
                    if (request != null) {

                        String notificationTitle = "Downloading... (" + (_numberDownloaded + 1) + "/" + _numberQueued + ")";
                        String notificationMessage = "Downloading " + FilenameUtils.getName(request.getFile());

                        showNotification(notificationTitle, notificationMessage, true);
                        try {
                            Log.d( LOGTAG, "External Storage State: " + Environment.getExternalStorageState());
                            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {


                                File file = new File(FilenameUtils.normalize(request.getFile()));
                                Log.d(LOGTAG, "Downloading " + request.getUrl() + " to " + file.toString());

                                File directory = new File(file.getParent());
                                FileUtils.forceMkdir(directory);
                                FileUtils.copyURLToFile(new URL(request.getUrl()), file);
                                _numberDownloaded++;
                            }
                            else
                            {
                                showNotification("Error", "External storage not available", false);
                            }

                        } catch (IOException e) {
                            Log.e(LOGTAG, "Error copying file", e);
                            showNotification("Error", "Error copying file", false);
                        }
                        stopSelf(request.getRequestId());
                        sleep(3000);
                    }
                } catch (InterruptedException e) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

                    showNotification("Download Complete", "Downloaded " + _numberDownloaded + " file(s)", false);
                    _notificationManager.cancel(NOTIFICATION_DOWNLOAD);
                    break;
                }

            }

        }
    };

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    private final IBinder _binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    @Override
    public void onCreate() {
        Log.d(LOGTAG, "Creating download thread!");
        _notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setForeground(true);
        _downloaderThread.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _numberQueued++;
        String url = intent.getStringExtra(DOWNLOAD_SERVICE_REQUESTED_URL);
        String path = intent.getStringExtra(DOWNLOAD_SERVICE_REQUESTED_PATH);
        _queue.add(new DownloadRequest(url, path, startId));
        Log.d(LOGTAG, "Added " + url + " to queue!");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOGTAG, "Destroying download thread!");
        _notificationManager.cancel(NOTIFICATION_DOWNLOAD_ONGOING);
        _downloaderThread.interrupt();
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String label, String message, boolean ongoing) {
        Notification notification = new Notification(android.R.drawable.stat_notify_sync, message, System.currentTimeMillis());
        if (ongoing) {
            notification.flags = Notification.FLAG_ONGOING_EVENT;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        notification.setLatestEventInfo(this, label, message, contentIntent);
        _notificationManager.notify(ongoing ? NOTIFICATION_DOWNLOAD_ONGOING : NOTIFICATION_DOWNLOAD, notification);
    }

    private class DownloadRequest {
        private String file;
        private String url;


        private int requestId;

        private DownloadRequest(String url, String file, int requestId) {
            this.file = file;
            this.url = url;
            this.requestId = requestId;
        }

        public String getFile() {
            return file;
        }

        public String getUrl() {
            return url;
        }

        public int getRequestId() {
            return requestId;
        }


    }

}
