package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.model.ApplicationItem;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.Application;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;

public class ApplicationItemListAdapter extends ItemListAdapter
{
   protected SqueezeService service;
   private ApplicationItem parentItem;
   private Application application;
   private Player selectedPlayer;
   private Integer numItems = 1;

   public ApplicationItemListAdapter(SqueezeService service, Activity parent, Player player, Application application, ApplicationItem parentItem)
   {
      super( parent );
      this.service = service;
      this.parentItem = parentItem;
      this.selectedPlayer = player;
      this.application = application;
   }

   @Override
   public int getCount()
   {
      return numItems;
   }

   protected List<? extends Object> createPage(int start, int pageSize)
   {
      BrowseResult<ApplicationItem> result = service.browseApplication( selectedPlayer, application, parentItem, start, pageSize );
      numItems = result.getTotalItems();
      return result.getResutls();
   }

}
