package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.Application;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.util.ImageLoader;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationListAdapter extends PagableAdapter
{
   protected SqueezeService _service;
   private Integer numItems = 1;

   public ApplicationListAdapter(SqueezeService service, Activity parent)
   {
      super( parent );
      _service = service;
   }

   @Override
   public int getCount()
   {
      return numItems;
   }

   protected List<? extends Object> createPage(int start, int pageSize)
   {
      BrowseResult<Application> result = _service.listApplications( start, pageSize );
      numItems = result.getTotalItems();
      return result.getResutls();
   }
   
   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
       View view = null;

       Item item = (Item) getItem(position);
       if (item != null) {
           view = _parent.getLayoutInflater().inflate(R.layout.icon_row_layout, null);
           ImageView icon = (ImageView) view.findViewById(R.id.icon);

           if (item.getImageThumbnailUrl() != null) {
               ImageLoader.getInstance().load(icon, item.getImageThumbnailUrl(), true);
           }
           
           TextView label = (TextView) view.findViewById(R.id.label);
           label.setText(item.getName());
       }
       else 
       {
           view = super.getView(position, convertView, parent);
       }
       return view;
   }

}
