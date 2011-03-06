package net.chrislehmann.squeezedroid.listadapter;

import android.app.Activity;
import net.chrislehmann.squeezedroid.model.ApplicationMenuItem;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.service.Application;
import net.chrislehmann.squeezedroid.service.SqueezeService;

import java.util.List;

public class ApplicationMenuItemListAdapter extends ItemListAdapter
{
   protected SqueezeService service;
   private ApplicationMenuItem parentItem;
   private Application application;
   private String playerId;
   private Integer numItems = 1;
   private String searchText;

   public ApplicationMenuItemListAdapter(SqueezeService service, Activity parent, String playerId, Application application, ApplicationMenuItem parentItem)
   {
      super( parent );
      this.service = service;
      this.parentItem = parentItem;
      this.playerId = playerId;
      this.application = application;
   }

   @Override
   public int getCount()
   {
      return numItems;
   }

   protected List<? extends Object> createPage(int start, int pageSize)
   {
      BrowseResult<ApplicationMenuItem> result = service.browseApplication(playerId, application, parentItem, searchText,start, pageSize );
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
