/**
 * 
 */
package net.chrislehmann.util;

import android.util.Log;
import net.chrislehmann.util.ImageLoader.Group;

class OnDownloadCompleteHandler implements Runnable
{
   private Group currentGroup;
   private ImageCache imageCache;
   
   public OnDownloadCompleteHandler(ImageCache cache, Group group)
   {
      imageCache = cache; 
      currentGroup = group;
   }
   
   public void run()
   {
      Log.d( ImageLoader.LOGTAG, "Loading bitmap for url " + currentGroup.url );
      imageCache.load( this.currentGroup.url, this.currentGroup.image );
      Log.d( ImageLoader.LOGTAG, "Done loading bitmap for url " + currentGroup.url );
      imageCache = null;
      currentGroup = null;
   }
   
}