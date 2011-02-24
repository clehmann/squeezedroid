/**
 * 
 */
package net.chrislehmann.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

class MemoryImageCache implements ImageCache
{
   private Map<String, Bitmap> cache = new HashMap<String, Bitmap>();
   private Queue<String> _cacheQueue = new LinkedList<String>();
   private int _maxCacheSize = 10;

   public void load(String name, ImageView image)
   {
      Bitmap bitmap = null;
      if ( has( name ) )
      {
         bitmap = cache.get( name );
      }
      image.setImageBitmap( bitmap );
   }

   public boolean has(String url)
   {
      return cache.containsKey( url );
   }

   public void put(String name, URL url)
   {
      Bitmap bitmap = null;
      InputStream inStream = null;
      HttpURLConnection connection = null;
      try
      {
         connection = (HttpURLConnection) url.openConnection();
         connection.setDoInput( true );
         connection.connect();
         inStream = connection.getInputStream();
         bitmap = BitmapFactory.decodeStream( inStream );
         cache.put( name, bitmap );
         _cacheQueue.add( name );

         inStream.close();
         connection.disconnect();
         if ( cache.size() > _maxCacheSize )
         {
            String urlToRemove = _cacheQueue.remove();
            cache.remove( urlToRemove );
         }
      }
      catch ( IOException e )
      {
         Log.e( ImageLoader.LOGTAG, "Unable to download file " + url, e );
      }
      finally
      {
         if( inStream != null )
         {
            try
            {
               inStream.close();
            }
            catch ( IOException e )
            {
               Log.e( ImageLoader.LOGTAG, "Error closing connection to " + url, e );
            }
         }
         if( connection != null )
         {
            connection.disconnect();
         }
      }
   }

   public void clear()
   {
      cache.clear();
   }

}