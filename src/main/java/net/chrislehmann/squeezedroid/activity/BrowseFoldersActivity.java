package net.chrislehmann.squeezedroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import net.chrislehmann.squeezedroid.listadapter.FolderListAdapter;
import net.chrislehmann.squeezedroid.model.Folder;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.service.SqueezeService;

public class BrowseFoldersActivity extends ItemListActivity {
   private Activity context = this;

   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     runWithService(new SqueezeServiceAwareThread() {
         public void runWithService(SqueezeService service) {
             getListView().setAdapter(new FolderListAdapter(service, context, getParentItem()));
         }
     });
     
     getListView().setOnItemClickListener( onItemClick );
   }
   
   private OnItemClickListener onItemClick = new OnItemClickListener()
   {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         Item item = (Item) listView.getAdapter().getItem( position );
         if( item instanceof Folder )
         {
            Intent i = new Intent();
            i.setAction( SqueezeDroidConstants.Actions.BROWSE_FOLDER_ACTION );
            i.setData( Uri.parse( "squeeze:///folder/" + item.getId() ) );
            startActivityForResult( i, SqueezeDroidConstants.RequestCodes.REQUEST_BROWSE );
         }
      }
   };

   protected Folder getParentItem()
   {
      Uri data = getIntent().getData();
      Folder item = null;
      if( data != null && data.getPathSegments().size() >= 2 )
      {
         String type = data.getPathSegments().get( 0 ); 
         
         if( "folder".equalsIgnoreCase( type ) )
         {
            item = new Folder();
            String id = data.getPathSegments().get( 1 );
            item.setId( id );
         }
      }
      return item;
   }

}