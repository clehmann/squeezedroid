package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.SongListAdapter;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.Item;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class BrowseSongsActivity extends ItemListActivity {


   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     setListAdapter( new SongListAdapter( ActivityUtils.getService(this), this, getParentItem()  ) );
   }
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id)
   {
       super.onListItemClick( l, v, position, id );
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