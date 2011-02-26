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
import android.widget.RemoteViews;
import net.chrislehmann.squeezedroid.R;
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


    private String _currentFileName;
    private Thread _downloaderThread = new Thread() {


        @Override
        public void run() {
            createOngoingNotification();
            while (true) {
                try {
                    DownloadRequest request = _queue.poll();
                    if (request != null) {


                        try {
                            Log.d(LOGTAG, "External Storage State: " + Environment.getExternalStorageState());
                            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {


                                File file = new File(FilenameUtils.normalize(request.getFile()));

                                _currentFileName = FilenameUtils.getName(request.getFile());
                                updateOngoingNotification();

                                Log.d(LOGTAG, "Downloading " + request.getUrl() + " to " + file.toString());

                                File directory = new File(file.getParent());
                                FileUtils.forceMkdir(directory);
                                FileUtils.copyURLToFile(new URL(request.getUrl()), file);
                                _numberDownloaded++;
                                _currentFileName = null;
                            } else {
                                showNotification("Error", "External storage not available");
                            }

                        } catch (IOException e) {
                            Log.e(LOGTAG, "Error copying file", e);
                            showNotification("Error", "Error copying file");
                            _currentFileName = null;
                        }
                        stopSelf(request.getRequestId());
                        sleep(3000);
                    }
                } catch (InterruptedException e) {
                    //Will be thrown when the activity is stopped (i.e. either the user has canceled or all downloads are finished)
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

                    showNotification("Download Complete", "Downloaded " + _numberDownloaded + " file(s)");
                    _notificationManager.cancel(NOTIFICATION_DOWNLOAD_ONGOING);
                    _currentFileName = null;
                    break;
                }

            }

        }
    };
    private Notification _notification;



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
        if (_notification != null) {
            updateOngoingNotification();
        }
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
    private void showNotification(String label, String message) {
        Notification notification = null;
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        notification = new Notification(android.R.drawable.stat_notify_sync, message, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(this, label, message, contentIntent);
        _notificationManager.notify(NOTIFICATION_DOWNLOAD, notification);

    }

    private void createOngoingNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        _notification = new Notification(android.R.drawable.stat_notify_sync, "Downloading Music", System.currentTimeMillis());
        _notification.flags = _notification.flags | Notification.FLAG_ONGOING_EVENT;
        _notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.layout_download_progress);
        _notification.contentIntent = contentIntent;
        _notification.contentView.setImageViewResource(R.id.status_icon, android.R.drawable.ic_menu_save);
        _notification.contentView.setTextViewText(R.id.status_text, "Downloading Music" );
        _notification.contentView.setProgressBar(R.id.status_progress, _numberQueued, _numberDownloaded, false);
        _notificationManager.notify(NOTIFICATION_DOWNLOAD_ONGOING, _notification);
    }



    private void updateOngoingNotification()
    {
        _notification.contentView.setTextViewText(R.id.status_text, "Downloading " + _currentFileName + " (" + (_numberDownloaded + 1) + "/" + _numberQueued + ")" );
        _notification.contentView.setProgressBar(R.id.status_progress, _numberQueued, _numberDownloaded, false);
        _notificationManager.notify(NOTIFICATION_DOWNLOAD_ONGOING, _notification);
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
