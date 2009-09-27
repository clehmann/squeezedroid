package net.chrislehmann.squeezedroid.activity;


import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public abstract class ItemListActivity extends ListActivity
{
   static final int MENU_DONE = 111;
   static final int MENU_PLAY_ALL = 112;
   static final int MENU_ENQUE_ALL = 113;
   
   private static final int CONTEXTMENU_PLAY_ITEM = 7070;
   private static final int CONTEXTMENU_ADD_ITEM = 7080;
   
   protected Activity _context = this;
   
   protected abstract Item getParentItem();
   
   public ItemListActivity()
   {
      super();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      getListView().setOnCreateContextMenuListener( new OnCreateContextMenuListener() {
         public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
             menu.add(Menu.NONE, CONTEXTMENU_ADD_ITEM, 0, "Add To Playlist");
             menu.add(Menu.NONE, CONTEXTMENU_PLAY_ITEM, 1, "Play Now");
         }
     });
      super.onCreate( savedInstanceState );
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      menu.add( 0, MENU_DONE, 0, "Done" );
      if( getParentItem() != null )
      {
         menu.add( 0, MENU_PLAY_ALL, 0, "Play All" );
         menu.add( 0, MENU_ENQUE_ALL, 0, "Enqueue All" );
      }
      return super.onCreateOptionsMenu( menu );
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      boolean handled = true;
      SqueezeService service = ActivityUtils.getSqueezeDroidApplication( this ).getService();
      if ( service != null )
      {
         Item parentItem = getParentItem();
         switch ( item.getItemId() )
         {
            case MENU_DONE :
               setResult( SqueezeDroidConstants.ResultCodes.RESULT_DONE );
               finish();
               break;
            case MENU_ENQUE_ALL :
               service.addItem( ActivityUtils.getSqueezeDroidApplication( this ).getSelectedPlayer(), parentItem );
               Toast.makeText( this, parentItem.getName() + " added to playlist.", Toast.LENGTH_SHORT );
               break;
            case MENU_PLAY_ALL :
               service.playItem( ActivityUtils.getSqueezeDroidApplication( this ).getSelectedPlayer(), parentItem );
               Toast.makeText( this, "Now playing " + parentItem.getName(), Toast.LENGTH_SHORT );
               break;
            default :
               handled = false;
         }
      }
      if ( !handled )
      {
         handled = super.onOptionsItemSelected( item );
      }
      return handled;
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      super.onActivityResult( requestCode, resultCode, data );
      if( resultCode == SqueezeDroidConstants.ResultCodes.RESULT_DONE )
      {
         setResult( resultCode );
         finish();
      }
   }
   
   @Override
   public boolean onContextItemSelected(MenuItem item) {
       SqueezeService service = ActivityUtils.getService(this);
       boolean handled = false;

       AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
       final Item selectedItem = (Item) getListAdapter().getItem( menuInfo.position );
       
       if( selectedItem != null && service != null )
       {
           switch (item.getItemId()) {
           case CONTEXTMENU_ADD_ITEM:
               service.addItem(ActivityUtils.getSqueezeDroidApplication(this).getSelectedPlayer(), selectedItem);
               runOnUiThread(new Runnable() {
                   public void run() {
                       Toast.makeText(_context, selectedItem.getName() + " added to playlist.", Toast.LENGTH_LONG);
                   }
               });
               handled = true;
               break;
           case CONTEXTMENU_PLAY_ITEM:
               service.playItem(ActivityUtils.getSqueezeDroidApplication(this).getSelectedPlayer(), selectedItem);
               runOnUiThread( new Runnable() {
                   public void run() {
                       Toast.makeText(_context, "Now playing " + selectedItem.getName(), Toast.LENGTH_LONG);
                   }
               });
               handled = true;
               break;
           default:
               break;
           }
           
       }
       if( !handled )
       {
           handled = super.onContextItemSelected(item);
       }
       return handled;
   }

}