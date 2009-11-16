package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Folder;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;

public class FolderListAdapter extends PagableAdapter
{
   protected SqueezeService _service;
   private Folder _parentFolder;
   private Integer numItems = 1;

   public FolderListAdapter(SqueezeService service, Activity parent)
   {
      super( parent );
      _service = service;
   }

   public FolderListAdapter(SqueezeService service, Activity parent, Folder parentFolder)
   {
      super( parent );
      _service = service;
      _parentFolder = parentFolder;
   }

   @Override
   public int getCount()
   {
      return numItems;
   }

   protected List<? extends Object> createPage(int start, int pageSize)
   {
      BrowseResult<Item> result = _service.browseFolders( _parentFolder, start, pageSize );
      numItems = result.getTotalItems();
      return result.getResutls();
   }


}
