package net.chrislehmann.util;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

/**
 * Singleton that can load {@link ImageView}s with data from a Url and cache them
 * @author lehmanc
 */
public enum ImageLoader
{
   INSTANCE();
   
   public static ImageLoader getInstance() {return INSTANCE;};
   
   static final String LOGTAG = "ImageLoader";
   
   private BlockingQueue<Group> _queue;
   private List<DownloadThread> _threads;
   private int numThreads = 10;
   ImageCache _cache;

   /**
    * Group passed to Downlad queue to indicate that the Thread should be stopped
    */
   static final Group STOP_GROUP = new Group(null, null, null);

   public void setCredentials(final String username, final String password)
   {

      Authenticator authenticator = new Authenticator()
      {
         public PasswordAuthentication getPasswordAuthentication()
         {
            return new PasswordAuthentication( username, password.toCharArray() );
         }
      };

      Authenticator.setDefault( authenticator );

   }

   /**
    * Constructor
    */
   private ImageLoader()
   {
      File sdcardRoot = new File( "/sdcard/" );
      if ( sdcardRoot.exists() && sdcardRoot.canWrite() )
      {
         _cache = new FileImageCache( "/sdcard/net.chrislehmann/" );
         Log.i( LOGTAG, "Using filesystem based image cache" );
      }
      else
      {
         _cache = new MemoryImageCache();
         Log.i( LOGTAG, "Can't write to sd card, Using memory based image cache" );
      }
      _queue = new SynchronousQueue<Group>();
      _threads = new ArrayList<DownloadThread>();
      for( int i = 0; i < numThreads; i++ )
      {
         DownloadThread thread = new DownloadThread( _cache, _queue );
         _threads.add( thread );
         thread.start();
      }
         
   }

   public void load(ImageView image, String url)
   {
      if ( _cache.has( url ) )
      {
         if ( image != null )
         {
            _cache.load( url, image );
         }
      }
      else
      {
         queue( image, url );
      }
   }

   public void queue(ImageView image, String url)
   {
      Iterator<Group> it = _queue.iterator();

      if ( image != null )
      {
         while ( it.hasNext() )
         {
            Group group = it.next();
            if ( group != null && group.image != null && group.image.equals( image ) )
            {
               it.remove();
               break;
            }
         }
      }
      _queue.offer( new Group( image, url, new Handler() ) );
   }

   public void clearQueue()
   {
      _queue = new SynchronousQueue<Group>();
   }

   public void clearCache()
   {
      _cache.clear();
   }
   
   public void stop()
   {
      for( int i = 0; i < numThreads; i++ )
      {
         _queue.offer( STOP_GROUP );
      }
   }

   static class Group
   {
      public Group(ImageView image, String url, Handler handler)
      {
         this.image = image;
         this.url = url;
         this.handler = handler;
      }

      public ImageView image;
      public String url;
      public Handler handler;

   }
   
}
