package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Playlist;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;

public class PlaylistListAdapter extends ItemListAdapter
{
   protected SqueezeService _service;
   private Integer numItems = 1;

   public PlaylistListAdapter(SqueezeService service, Activity parent)
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
      BrowseResult<Playlist> result = _service.listPlaylists( start, pageSize );
      numItems = result.getTotalItems();
      return result.getResutls();
   }
 
}
