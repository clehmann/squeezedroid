package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BrowseRootActivity extends ListActivity {
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
     super.onCreate(savedInstanceState);
     
     String[] values = new String[] { "Artists",  "Albums", "Genres", "New Music" };
     int[] icons = new int[] { R.drawable.artists, R.drawable.albums_25x25_f, R.drawable.genres_25x25_f, R.drawable.newmusic_25x25_f };
     setListAdapter( new IconicAdapter<String>( this, R.layout.icon_row_layout, values, icons ) );
   }
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id)
   {

      String item = (String) getListAdapter().getItem( position );
      
      Intent intent = new Intent();
      if( item.equals( "Artists" ))
      {
         intent.setAction( "net.chrislehmann.squeezedroid.action.BrowseArtist" );
         intent.setData( Uri.parse( "squeeze:///" ));
      }
      if( item.equals( "Albums" ))
      {
    	  intent.setAction( "net.chrislehmann.squeezedroid.action.BrowseAlbum" );
    	  intent.setData( Uri.parse( "squeeze:///" ));
      }      
      if( item.equals( "Genres" ))
      {
         intent.setAction( "net.chrislehmann.squeezedroid.action.BrowseGenre" );
         intent.setData( Uri.parse( "squeeze:///" ));
      }      
      if( item.equals( "New Music" ))
      {
         intent.setAction( "net.chrislehmann.squeezedroid.action.BrowseAlbum" );
         intent.setData( Uri.parse( "squeeze:///?sort=new" ));
      }         
      this.startActivityForResult( intent, SqueezeDroidConstants.RequestCodes.REQUEST_BROWSE );
      super.onListItemClick( l, v, position, id );
   }

   class IconicAdapter<T> extends ArrayAdapter<T>
   {

      Activity context;
      private int rowLayout = R.layout.icon_row_layout;
      private int iconId = R.id.icon;
      private int textId = R.id.label;

      private int[] images;

      IconicAdapter(Activity context, int rowLayout, T[] items, int[] images)
      {
         super( context, rowLayout, items );
         this.rowLayout = rowLayout;
         this.context = context;
         this.images = images;
      }

      public View getView(int position, View convertView, ViewGroup parent)
      {
         View row = context.getLayoutInflater().inflate( rowLayout, null );

         TextView label = (TextView) row.findViewById( textId );
         label.setText( (CharSequence) getItem( position ) );

         if ( position < images.length )
         {
            ImageView icon = (ImageView) row.findViewById( iconId );
            icon.setImageResource( images[position] );
         }

         return (row);
      }
      
   }
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      if( resultCode == SqueezeDroidConstants.ResultCodes.RESULT_DONE )
      {
         finish();
      }
   }
}