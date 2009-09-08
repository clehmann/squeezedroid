package net.chrislehmann.squeezedroid.listadapter;

import java.util.ArrayList;
import java.util.List;

import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.PlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

public class PlayListAdapter extends SongListAdapter
{
   private Player _player;
   private PlayerStatus _currentStatus;

   private PlayerStatusHandler onPlayerStatusChanged = new PlayerStatusHandler()
   {

      public void onTimeChanged(int newPosition)
      {
      }

      public void onSongChanged(PlayerStatus status)
      {
         _currentStatus = status;
         notifyChange();
      }

      public void onPlaylistChanged(PlayerStatus status)
      {
         _currentStatus = status;
         numItems = 1;
         resetPages();
      }

      public void onVolumeChanged(int newVolume){}
   };

   public void setPlayer(Player player)
   {
      _player = player;
      numItems = 1;
      resetPages();
   }

   public PlayListAdapter(SqueezeService service, Activity parent, Player player)
   {
      super( service, parent, null );
      _player = player;
      _currentStatus = service.getPlayerStatus( player );
      service.subscribe( player, onPlayerStatusChanged );
   }

   protected List<? extends Object> createPage(int start, int pageSize)
   {
      List<Song> playlist = new ArrayList<Song>();
      if ( _player != null )
      {
         BrowseResult<Song> result = _service.getCurrentPlaylist( _player, start, pageSize );
         playlist = result.getResutls();
         numItems = result.getTotalItems();
      }
      return playlist;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View v = super.getView( position, convertView, parent );
      if ( _currentStatus != null && position == _currentStatus.getCurrentIndex() )
      {
         v.setBackgroundColor( Color.YELLOW );
      }
      return v;
   }
}
