package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.ApplicationItemListAdapter;
import net.chrislehmann.squeezedroid.model.ApplicationItem;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.Application;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseApplicationActivity extends ItemListActivity {
   private Activity context = this;

   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     runWithService( new SqueezeServiceAwareThread()
     {
        public void runWithService(SqueezeService service)
        {
           getListView().setAdapter( new ApplicationItemListAdapter( service, context, getSelectedPlayer(), getSelectedApplication(), getParentItem()  ) );
        }
     });
     
     getListView().setOnItemClickListener( onItemClick );
   }
   
   private OnItemClickListener onItemClick = new OnItemClickListener()
   {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         Item item = (Item) listView.getAdapter().getItem( position );
         if( item instanceof ApplicationItem )
         {
            final ApplicationItem applicationItem = (ApplicationItem) item; 
            if( applicationItem.isHasItems() )
            {
               Intent i = new Intent();
               i.setAction( "net.chrislehmann.squeezedroid.action.BrowseApplication" );
               i.putExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_APPLICATION, getSelectedApplication() );
               i.putExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_PARENTITEM, applicationItem );
               startActivityForResult( i, SqueezeDroidConstants.RequestCodes.REQUEST_BROWSE );
            }
            else if( applicationItem.isPlayable() )
            {
               runWithService( new SqueezeServiceAwareThread()
               {
                  public void runWithService(SqueezeService service)
                  {
                     service.playItem( getSelectedPlayer(), applicationItem );
                  }
               });
            }
         }
      }
   };

   protected ApplicationItem getParentItem()
   {
      return (ApplicationItem) getIntent().getSerializableExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_PARENTITEM );
   }
   
   protected Application getSelectedApplication()
   {
      return (Application) getIntent().getSerializableExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_APPLICATION );
   }
}