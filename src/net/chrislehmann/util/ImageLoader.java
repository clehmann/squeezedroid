package net.chrislehmann.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;
 
public class ImageLoader {
    static private ImageLoader _instance;
    static public ImageLoader getInstance() {
        if (_instance == null) {
            _instance = new ImageLoader();
        }
        return _instance;
    }
 
    private HashMap<String , Bitmap> _urlToBitmap;
    private Queue<Group> _queue;
    private DownloadThread _thread;
    private Bitmap _missing;
    private boolean _busy;
    
    private Queue<String> _cacheQueue = new  LinkedList<String>();
	private int _maxCacheSize = 10;
 
    /**
     * Constructor
     */
    private ImageLoader () {
        _urlToBitmap = new HashMap<String , Bitmap>();
        _queue = new LinkedList<Group>();
        _busy = false;
    }
 
    public Bitmap get(String url) {
        return _urlToBitmap.get(url);
    }
 
    public void load(ImageView image, String url) {
        load(image, url, false);
    }
 
    public void load(ImageView image, String url, boolean cache) {
        if (_urlToBitmap.get(url) != null) {
            if(image!=null) {
                image.setImageBitmap(_urlToBitmap.get(url));
            }
        } else {
            //image.setImageBitmap(null);
            queue(image, url, cache);
        }
    }
 
    public void queue(ImageView image, String url, boolean cache) {
        Iterator<Group> it = _queue.iterator();
        if (image!=null) {
            while (it.hasNext()) {
            	Group group = it.next();
                if (group != null && group.image != null && group.image.equals(image)) {
                    it.remove();
                    break;
                }
            }
        } else if (url!=null) {
            while (it.hasNext()) {
                if (it.next().url.equals(url)) {
                    it.remove();
                    break;
                }
            }
        }
        _queue.add(new Group(image, url, null, cache));
        loadNext();
    }
 
    public void clearQueue() {
        _queue = new LinkedList<Group>();
    }
 
    public void clearCache() {
        _urlToBitmap = new HashMap<String , Bitmap>();
       // _cacheQueue.clear();
    }
 
    public void cancel() {
        clearQueue();
        if ( _thread != null ) {
            _thread.disconnect();
            _thread = null;
        }
    }
 
    public void setMissingBitmap(Bitmap bitmap) {
        _missing = bitmap;
    }
 
    private void loadNext() {
        Iterator<Group> it = _queue.iterator();
        if (!_busy && it.hasNext() ) {
            _busy = true;
            Group group = it.next();
            it.remove();
            // double check image availability
            if (_urlToBitmap.get(group.url) != null) {
                if (group.image!=null) {
                    group.image.setImageBitmap(_urlToBitmap.get(group.url));
                }
                _busy = false;
                loadNext();
            } else {
                _thread = new DownloadThread(group);
                _thread.start();
            }
        }
    }
 
    private void onLoad() {
        if (_thread != null) {
            Group group = _thread.group;
            if (group.bitmap != null) {
                if (group.cache) {
                	if( _urlToBitmap.size() > _maxCacheSize)
                	{
                		String urlToRemove = _cacheQueue.remove();
                		_urlToBitmap.remove(urlToRemove);
                	}
                    _urlToBitmap.put(group.url, group.bitmap);
                    _cacheQueue.add(group.url);
                }
                if (group.image != null) {
                    group.image.setImageBitmap(group.bitmap);
                }
            } else if (_missing != null) {
                if (group.image != null) {
                    group.image.setImageBitmap(_missing);
                }
            }
        }
        _thread = null;
        _busy = false;
        loadNext();
    }
 
    private class Group {
        public Group(ImageView image, String url, Bitmap bitmap, boolean cache) {
            this.image = image;
            this.url = url;
            this.bitmap = bitmap;
            this.cache = cache;
        }
        public ImageView image;
        public String url;
        public Bitmap bitmap;
        public boolean cache;
 
    }
 
    private class DownloadThread extends Thread {
        final Handler threadHandler = new Handler();
        final Runnable threadCallback = new Runnable() {
            public void run() {
                onLoad();
            }
        };
        private HttpURLConnection _conn;
        public Group group;
        public DownloadThread(Group group) {
            this.group = group;
        }
 
        @Override
        public void run() {
            InputStream inStream = null;
            _conn = null;
            try {
                _conn = (HttpURLConnection) new URL(group.url).openConnection();
                _conn.setDoInput(true);
                _conn.connect();
                inStream = _conn.getInputStream();
                group.bitmap = BitmapFactory.decodeStream(inStream);
                inStream.close();
                _conn.disconnect();
                inStream = null;
                _conn = null;
            } catch (Exception ex) {
                // nothing
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception ex) {}
            }
            disconnect();
            inStream = null;
            _conn = null;
            threadHandler.post(threadCallback);
        }
 
        public void disconnect() {
            if (_conn != null) {
                _conn.disconnect();
            }
        }
    }
}
