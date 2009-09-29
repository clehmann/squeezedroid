package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.SongListAdapter;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.Item;
import android.net.Uri;
import android.os.Bundle;

public class BrowseSongsActivity extends ItemListActivity {


   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     getListView().setAdapter( new SongListAdapter( getService(), this, getParentItem()  ) );
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
         item.setId( id );
      }
      return item;
   }
}