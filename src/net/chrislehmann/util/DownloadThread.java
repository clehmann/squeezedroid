/**
 * 
 */
package net.chrislehmann.util;

import java.net.URL;
import java.util.concurrent.BlockingQueue;

import net.chrislehmann.util.ImageLoader.Group;
import android.util.Log;

class DownloadThread extends Thread
{
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
            currentGroup = groupQueue.take();
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
            this.groupCache.load( this.currentGroup.url, this.currentGroup.image );
            currentGroup.handler.post( threadCallback );
         }
         catch ( Exception ex )
         {
            Log.e( ImageLoader.LOGTAG, "Error fetching image", ex );
         }
         finally
         {
            currentGroup = null;
         }
      }
   }
}