package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.ApplicationMenuItemListAdapter;
import net.chrislehmann.squeezedroid.model.ApplicationMenuItem;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.Application;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.view.TextInputDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseApplicationActivity extends ItemListActivity {
   protected static final int DIALOG_SEARCH_TEXT = 555;
   private Activity context = this;
   private int lastSelectedPosition;
   private String searchText;

   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     runWithService(new SqueezeServiceAwareThread() {
         public void runWithService(SqueezeService service) {
             ApplicationMenuItemListAdapter listAdapter = new ApplicationMenuItemListAdapter(service, context, getSelectedPlayer(), getSelectedApplication(), getParentItem());
             listAdapter.setSearchText(getIntent().getStringExtra(SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_SEARCHTEXT));
             getListView().setAdapter(listAdapter);
         }
     });
     getListView().setOnItemClickListener( onItemClick );
   }
   
   private OnItemClickListener onItemClick = new OnItemClickListener()
   {

      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         lastSelectedPosition = position;
         ApplicationMenuItem applicationMenuItem = (ApplicationMenuItem) getLastSelectedItem();
         if( "search".equals( applicationMenuItem.getType() ) )
         {
            showDialog(  DIALOG_SEARCH_TEXT );
         }
         else
         {
            handleListSelection();
         }
      }
   };
   
   private ApplicationMenuItem getLastSelectedItem()
   {
      Item item = (Item) listView.getAdapter().getItem( lastSelectedPosition );
      return (ApplicationMenuItem) item;
   }
   
   
   protected Dialog onCreateDialog(int id) 
   {
      Dialog d = null;
      switch ( id )
      {
         case DIALOG_SEARCH_TEXT :
            final TextInputDialog textDialog = new TextInputDialog( context );
            OnClickListener onClose = new OnClickListener()
            {
               public void onClick(View v)
               {
                  searchText = textDialog.getText();
                  handleListSelection();
               }
            };
            textDialog.setTitle( getLastSelectedItem().getName() );
            textDialog.setOnOkClickedListener( onClose );
            d = textDialog;
            break;

         default :
            d = super.onCreateDialog( id );
      }
      return d;
      
   };

   protected ApplicationMenuItem getParentItem()
   {
      return (ApplicationMenuItem) getIntent().getSerializableExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_PARENTITEM );
   }
   
   protected Application getSelectedApplication()
   {
      return (Application) getIntent().getSerializableExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_APPLICATION );
   }

   @Override
   protected boolean isItemPlayable( Item item )
   {
      boolean playable = false;
      if( item != null && item instanceof ApplicationMenuItem )
      {
         ApplicationMenuItem menuItem = (ApplicationMenuItem) item;
         playable = menuItem.isPlayable();
      }
      return playable;
   }
   private void handleListSelection()
   {
      final ApplicationMenuItem applicationMenuItem = getLastSelectedItem();
      if ( applicationMenuItem.isHasItems() )
      {
         Intent i = new Intent();
         i.setAction( "net.chrislehmann.squeezedroid.action.BrowseApplication" );
         i.putExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_APPLICATION, getSelectedApplication() );
         i.putExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_PARENTITEM, applicationMenuItem );
         if ( "search".equals( applicationMenuItem.getType() ) )
         {
            i.putExtra( SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_SEARCHTEXT, searchText );
         }
         startActivityForResult( i, SqueezeDroidConstants.RequestCodes.REQUEST_BROWSE );
      }
      else if ( applicationMenuItem.isPlayable() )
      {
         runWithService(new SqueezeServiceAwareThread() {
             public void runWithService(SqueezeService service) {
                 service.playItem(getSelectedPlayer(), applicationMenuItem);
             }
         });
      }
   }
}