package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.listadapter.PlayListAdapter;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class PlayListActivity extends SqueezedroidActivitySupport
{

   private static final int CONTEXTMENU_REMOVE_ITEM = 421;
   private static final int CONTEXTMENU_REMOVE_ALBUM = 422;
   private static final int CONTEXTMENU_REMOVE_ARTIST = 423;
   private static final int CONTEXTMENU_GROUP_REMOVE = 100;
   private static final int MENU_CLEAR_ALL = 1;
   private static final int MENU_LIBRARY = 2;
   private static final int MENU_DONE = 0;

   protected ListView listView;


   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );

      setContentView( R.layout.list_layout );
      listView = (ListView) findViewById( R.id.list );
      listView.setOnCreateContextMenuListener( onCreateContextMenu );
      listView.setOnItemClickListener( onItemClick );

      SqueezeService service = getService();
      if ( service != null )
      {
         listView.setAdapter( new PlayListAdapter( service, this, getSelectedPlayer() ) );
         PlayerStatus status = service.getPlayerStatus( getSelectedPlayer() );
         if ( status != null && status.getCurrentIndex() <= listView.getCount() )
         {
            listView.setSelection( status.getCurrentIndex() );
         }
      }
   }


   OnCreateContextMenuListener onCreateContextMenu = new OnCreateContextMenuListener()
   {

      public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
      {
         menu.add( CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ITEM, 0, "Remove song" );
         menu.add( CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ALBUM, 1, "Remove artist" );
         menu.add( CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ARTIST, 2, "Remove album" );
      }
   };

   private OnItemClickListener onItemClick = new OnItemClickListener()
   {

      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         SqueezeService service = getService();
         if ( service != null )
         {
            service.jump( getSelectedPlayer(), String.valueOf( position ) );
         }
      }
   };


   @Override
   public boolean onContextItemSelected(MenuItem item)
   {
      boolean handled = false;
      AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
      Song song = (Song) listView.getItemAtPosition( menuInfo.position );

      SqueezeService service = getService();
      if ( service != null )
      {

         switch ( item.getItemId() )
         {
            case CONTEXTMENU_REMOVE_ITEM :
               service.removeItem( getSelectedPlayer(), menuInfo.position );
               handled = true;
               break;
            case CONTEXTMENU_REMOVE_ARTIST :
               service.removeAllItemsByArtist( getSelectedPlayer(), song.getArtistId() );
               handled = true;
               break;
            case CONTEXTMENU_REMOVE_ALBUM :
               service.removeAllItemsInAlbum( getSelectedPlayer(), song.getAlbumId() );
               handled = true;
               break;
            default :
               break;
         }

      }
      if ( !handled )
      {
         handled = super.onContextItemSelected( item );
      }
      return handled;
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      menu.add( 0, MENU_LIBRARY, 0, "Add" );
      menu.add( 0, MENU_CLEAR_ALL, 0, "Clear" );
      menu.add( 0, MENU_DONE, 0, "Done" );
      return super.onCreateOptionsMenu( menu );
   }
   
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch ( item.getItemId() )
      {
         case MENU_LIBRARY :
            launchSubActivity( BrowseRootActivity.class, null );
            return true;
         case MENU_CLEAR_ALL :
            runWithService( new SqueezeServiceAwareThread()
            {
               public void runWithService(SqueezeService service)
               {
                  service.clearPlaylist( getSelectedPlayer() );
               }
            });
         case MENU_DONE:
            finish();
      }
      return false;
   }
   
}