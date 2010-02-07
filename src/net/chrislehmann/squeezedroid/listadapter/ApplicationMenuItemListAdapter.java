package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.model.ApplicationMenuItem;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.Application;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;

public class ApplicationMenuItemListAdapter extends ItemListAdapter
{
   protected SqueezeService service;
   private ApplicationMenuItem parentItem;
   private Application application;
   private Player selectedPlayer;
   private Integer numItems = 1;
   private String searchText;

   public ApplicationMenuItemListAdapter(SqueezeService service, Activity parent, Player player, Application application, ApplicationMenuItem parentItem)
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
      BrowseResult<ApplicationMenuItem> result = service.browseApplication( selectedPlayer, application, parentItem, searchText,start, pageSize );
      numItems = result.getTotalItems();
      return result.getResutls();
   }

   public String getSearchText()
   {
      return searchText;
   }

   public void setSearchText(String searchText)
   {
      this.searchText = searchText;
   }

}
