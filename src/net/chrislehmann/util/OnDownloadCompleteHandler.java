/**
 * 
 */
package net.chrislehmann.util;

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
      imageCache.load( this.currentGroup.url, this.currentGroup.image );
      imageCache = null;
      currentGroup = null;
   }
   
}