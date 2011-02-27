package net.chrislehmann.squeezedroid.listadapter;

import android.app.Activity;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.service.Application;
import net.chrislehmann.squeezedroid.service.SqueezeService;

import java.util.List;

public class RadioStationsListAdapter extends ItemListAdapter
{
   protected SqueezeService _service;
   private Integer numItems = 1;

   public RadioStationsListAdapter(SqueezeService service, Activity parent)
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
      BrowseResult<Application> result = _service.listRadioStations( start, pageSize );
      numItems = result.getTotalItems();
      return result.getResutls();
   }
 
}
