package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.SongListAdapter;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.Item;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class BrowseSongsActivity extends ListActivity {
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     Item parentItem = getParentItem( getIntent().getData() );
     setListAdapter( new SongListAdapter( ActivityUtils.getService(this), this, parentItem  ) );
   }
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id)
   {
      //TODO - Fire event based on what is selected
      //String item = (String) getListView().getItemAtPosition(position);
       super.onListItemClick( l, v, position, id );
   }

   private Item getParentItem(Uri data)
   {
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