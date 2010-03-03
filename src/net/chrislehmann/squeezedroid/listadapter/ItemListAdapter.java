package net.chrislehmann.squeezedroid.listadapter;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.util.ImageLoader;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Extension of {@link PagableAdapter} that creates a {@link View} suitable for
 * {@link Item}s.  This consists of an {@link ImageView} containing the image from 
 * {@link Item#getImageThumbnailUrl() and the {@link Item#getName()}
 * 
 * @author lehmanc
 */
public abstract class ItemListAdapter extends PagableAdapter
{

   public ItemListAdapter(Activity parent)
   {
      super( parent );
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View view = null;

      Item item = (Item) getItem( position );
      if ( item != null )
      {
         if ( convertView == null || convertView.getId() == R.id.loading_row_layout )
         {
            view = _parent.getLayoutInflater().inflate( R.layout.icon_row_layout, null );
         }
         else
         {
            view = convertView;
         }
         ImageView icon = (ImageView) view.findViewById( R.id.icon );

         icon.setImageResource( R.drawable.default_album_thumb );
         if ( item.getImageThumbnailUrl() != null )
         {
            ImageLoader.getInstance().load( icon, item.getImageThumbnailUrl() );
         }

         TextView label = (TextView) view.findViewById( R.id.label );
         label.setText( item.getName() );
      }
      else
      {
         view = super.getView( position, convertView, parent );
      }
      return view;
   }

}
