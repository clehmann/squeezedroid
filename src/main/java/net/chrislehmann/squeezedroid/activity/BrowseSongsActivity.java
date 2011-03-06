package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.SongListAdapter;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Playlist;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class BrowseSongsActivity extends ItemListActivity {
   private Activity context = this;

   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     runWithService(new SqueezeServiceAwareThread() {
         public void runWithService(SqueezeService service) {
             getListView().setAdapter(new SongListAdapter(service, context, getParentItem()));
         }
     });
   }

   protected Item getParentItem()
   {
      Uri data = getIntent().getData();
      Item item = null;
      if( data != null && data.getPathSegments().size() >= 2 )
      {
         String type = data.getPathSegments().get( 0 ); 
         String id = data.getPathSegments().get( 1 ); 
         
         if( "artist".equalsIgnoreCase( type ) )
         {
            item = new Artist();
         }
         else if( "genre".equalsIgnoreCase( type ) )
         {
            
         }
         else if( "album".equalsIgnoreCase( type ) )
         {
            item = new Album();
         }
         else if( "playlist".equalsIgnoreCase( type ) )
         {
            item = new Playlist();
         }            
         item.setId( id );
      }
      return item;
   }
}