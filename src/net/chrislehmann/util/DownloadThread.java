/**
 * 
 */
package net.chrislehmann.util;

import android.util.Log;
import net.chrislehmann.util.ImageLoader.Group;

import java.net.URL;
import java.util.concurrent.BlockingQueue;

class DownloadThread extends Thread
{
   private static final String LOGTAG = "ImageLoader.DownloadThread";
   private Group currentGroup;
   private BlockingQueue<Group> groupQueue;
   private ImageCache groupCache;

   public DownloadThread( ImageCache cache, BlockingQueue<Group> queue)
   {
      this.groupCache = cache;
      groupQueue = queue;
   }
   
   @Override
   public void run()
   {
      while( !isInterrupted() )
      {
         try
         {
            Log.d( LOGTAG, "Waiting for image group in queue, queueSize is " + groupQueue.size());
            currentGroup = groupQueue.take();
            Log.d( LOGTAG, "Got image group from queue: " + currentGroup.url );
            if( currentGroup == ImageLoader.STOP_GROUP )
            {
               Log.i( ImageLoader.LOGTAG, "Found stop currentGroup, shutting down Download Thread" );
               break;
            }
            if( !groupCache.has( currentGroup.url ) )
            {
               groupCache.put( currentGroup.url, new URL( currentGroup.url ) );
            }
            
            Runnable threadCallback = new OnDownloadCompleteHandler( this.groupCache, this.currentGroup );
            Log.d( LOGTAG, "Download finished for image " + currentGroup.url + ", Executing callback" );
            currentGroup.handler.post( threadCallback );
            Log.d( LOGTAG, "Callback finished for url " + currentGroup.url );
         }
         catch ( Exception ex )
         {
            Log.e( LOGTAG, "Error fetching image", ex );
         }
         finally
         {
            currentGroup = null;
         }
      }
      Log.d( LOGTAG, "Thread interrupted, finishing");
   }
}