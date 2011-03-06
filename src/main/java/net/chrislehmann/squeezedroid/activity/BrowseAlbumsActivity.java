package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.AlbumListAdapter;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.Genre;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.service.SqueezeService.Sort;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseAlbumsActivity extends ItemListActivity
{
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );

      final SqueezeService.Sort sort = getSort( getIntent().getData() );
      final Item parentItem = getParentItem();
      runWithService(new SqueezeServiceAwareThread() {
          public void runWithService(SqueezeService service) {
              listView.setAdapter(new AlbumListAdapter(service, context, parentItem, sort));
          }
      });
      listView.setOnItemClickListener( onListItemClick );
   };

   private Sort getSort(Uri data)
   {
      SqueezeService.Sort sort = Sort.TITLE;

      String sortString = data.getQueryParameter( "sort" );
      if ( "new".equals( sortString ) )
      {
         sort = Sort.NEW;
      }

      return sort;
   }

   protected Item getParentItem()
   {
      Uri data = getIntent().getData();
      Item item = null;
      if ( data.getPathSegments().size() >= 2 )
      {
         String type = data.getPathSegments().get( 0 );
         String id = data.getPathSegments().get( 1 );

         if ( "artist".equalsIgnoreCase( type ) )
         {
            item = new Artist();
         }
         else if ( "genre".equalsIgnoreCase( type ) )
         {
            item = new Genre();
         }

         item.setId( id );
      }
      return item;
   }

   OnItemClickListener onListItemClick = new OnItemClickListener()
   {

      @SuppressWarnings("unchecked")
      public void onItemClick(AdapterView parent, View view, int position, long id)
      {
         Item item = (Item) listView.getAdapter().getItem( position );
         Intent i = new Intent();
         i.setAction( "net.chrislehmann.squeezedroid.action.BrowseSong" );
         i.setData( Uri.parse( "squeeze:///album/" + item.getId() ) );
         startActivityForResult( i, SqueezeDroidConstants.RequestCodes.REQUEST_BROWSE );
      }
   };
}