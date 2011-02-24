package net.chrislehmann.util;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

/**
 * Singleton that can load {@link ImageView}s with data from a Url and cache them.  The {@link ImageLoader}
 * is backed by some {@link ImageCache} that will cache previously-downloaded images.  By default,
 * the {@link ImageLoader} will try to use the {@link FileImageCache} (if the sd card is writeable), but will
 * fall back on the {@link MemoryImageCache} if needed.
 * 
 * @author lehmanc
 */
public enum ImageLoader
{
   INSTANCE();
   
   public static ImageLoader getInstance() {return INSTANCE;};
   
   static final String LOGTAG = "ImageLoader";
   
   private BlockingQueue<Group> _queue;
   private List<DownloadThread> _threads;
   private int numThreads = 4;
   ImageCache _cache;

   /**
    * Group passed to Downlad queue to indicate that the Thread should be stopped
    */
   public static final Group STOP_GROUP = new Group(null, null, null);

   
   private class UsernamePasswordAuthenticator extends Authenticator
   {
      String username;
      String password;
      public UsernamePasswordAuthenticator(String username, String password)
      {
         this.username = username;
         this.password = password;
      }
      
      public PasswordAuthentication getPasswordAuthentication()
      {
         return new PasswordAuthentication( username, password.toCharArray() );
      }
   };


   /**
    * Set the default credentials that all HTTP Requests will use when downloading images
    * @param username
    * @param password
    */
   public void setCredentials(final String username, final String password)
   {

      Authenticator.setDefault( new UsernamePasswordAuthenticator( username, password ));

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
      _queue = new LinkedBlockingQueue<Group>();
      start();
   }

   /**
    * Load an image at url into the {@link ImageView} image.  if the image is already in the cache, it will be loaded
    * from there.  If not, it will be downloaded and saved into the cache.
    * @param image
    * @param url
    */
   public void load(ImageView image, String url)
   {
      if ( _cache.has( url ) )
      {
         if ( image != null )
         {
            Log.d( LOGTAG, "Loading image from cache: " +  url);
            _cache.load( url, image );
         }
      }
      else
      {            
         Log.d( LOGTAG, "Image not in cache, queuing to load: " +  url);
         queue( image, url );
      }
   }

   /**
    * Empty the queue of items to be downloaded.  Any items cuurently being downloaded will finish.
    */
   public void clearQueue()
   {
      _queue.clear();
   }

   /**
    * Clears out the Image Cache
    */
   public void clearCache()
   {
      _cache.clear();
   }
   
   
   /**
    * Clear out the queue of items to be downloaded and stops all of the download threads.  Any calls to {@link ImageLoader#load(ImageView, String)}
    * will do nothing until {@link ImageLoader#start()} is called
    */
   public void stop()
   {
      clearQueue();
      for( int i = 0; i < numThreads; i++ )
      {
         _queue.offer( STOP_GROUP );
      }
   }

   /**
    * Starts all of the download threads.
    */
   public void start()
   {
      _threads = new ArrayList<DownloadThread>();
      for( int i = 0; i < numThreads; i++ )
      {
         DownloadThread thread = new DownloadThread( _cache, _queue );
         _threads.add( thread );
         thread.start();
      }
      
   }

   protected void queue(ImageView image, String url)
   {
      if ( image != null )
      {
         Iterator<Group> it = _queue.iterator();
         while ( it.hasNext() )
         {
            Group group = it.next();
            if ( group != null && group.image != null && group.image.equals( image ) )
            {
               Log.d( LOGTAG, "ImageView already in queue, removing and replacing with current url: " +  url);
               it.remove();
               break;
            }
         }
         Log.d( LOGTAG, "Queue size before add: " +  _queue.size() );
         _queue.offer( new Group( image, url, new Handler() ) );
         Log.d( LOGTAG, "Image added to queue: " +  url );

      }
   }

   /**
    * Class that contains information about an image to be downloaded and the {@link ImageView} it will
    * be loaded into
    */
   public static class Group implements Comparable<Group>
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
      
      public int compareTo(Group rhs)
      {
         return CompareToBuilder.reflectionCompare( this, rhs );
      }
      
      @Override
      public int hashCode()
      {
         return HashCodeBuilder.reflectionHashCode( this );
      }
   }
   
   
}
