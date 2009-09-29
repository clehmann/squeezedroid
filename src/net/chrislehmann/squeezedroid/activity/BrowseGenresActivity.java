package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.GenreListAdapter;
import net.chrislehmann.squeezedroid.model.Item;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseGenresActivity extends ItemListActivity
{

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );

      listView.setAdapter( new GenreListAdapter( ((SqueezeDroidApplication) getApplication()).getService(), this, null ) );
      listView.setOnItemClickListener( onItemClick );
   }

   private OnItemClickListener onItemClick = new OnItemClickListener()
   {

      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         Item item = (Item) listView.getAdapter().getItem( position );
         Intent i = new Intent();
         i.setAction( "net.chrislehmann.squeezedroid.action.BrowseArtist" );
         i.setData( Uri.parse( "squeeze:///genre/" + item.getId() ) );
         startActivityForResult( i, SqueezeDroidConstants.RequestCodes.REQUEST_BROWSE );
      }
   };

   @Override
   protected Item getParentItem()
   {
      //We have no parent...
      return null;
   }
}