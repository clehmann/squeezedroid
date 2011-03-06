package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.PlaylistListAdapter;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class ListPlaylistsActivity extends ItemListActivity {
   protected static final int DIALOG_SEARCH_TEXT = 555;
   private Activity context = this;

   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     runWithService(new SqueezeServiceAwareThread() {
         public void runWithService(SqueezeService service) {
             PlaylistListAdapter listAdapter = new PlaylistListAdapter(service, context);
             getListView().setAdapter(listAdapter);
         }
     });
     getListView().setOnItemClickListener( onItemClick );
   }
   
   private OnItemClickListener onItemClick = new OnItemClickListener()
   {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         Item item = (Item) listView.getAdapter().getItem( position );
         Intent i = new Intent();
         i.setAction( "net.chrislehmann.squeezedroid.action.BrowseSong" );
         i.setData( Uri.parse( "squeeze:///playlist/" + item.getId() ) );
         startActivityForResult( i, SqueezeDroidConstants.RequestCodes.REQUEST_BROWSE );
      }
   };

   @Override
   protected Item getParentItem()
   {
      return null;
   }
   

}