package net.chrislehmann.squeezedroid.listadapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.PlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SimplePlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SqueezeService;

import java.util.ArrayList;
import java.util.List;

public class PlayListAdapter extends SongListAdapter
{
   private String _player;
   private PlayerStatus _currentStatus;

   private PlayerStatusHandler onPlayerStatusChanged = new SimplePlayerStatusHandler()
   {

      public void onSongChanged(PlayerStatus status)
      {
         _currentStatus = status;
         _parent.runOnUiThread(new Runnable() {
             public void run() {
                 notifyChange();
             }
         });
      }

      public void onPlaylistChanged(PlayerStatus status)
      {
         _currentStatus = status;
         _numItems = 1;
         resetPages();
      }

   };

   public void setPlayer(String player)
   {
      _player = player;
      _numItems = 1;
      resetPages();
   }

   public PlayListAdapter(SqueezeService service, Activity parent, String player)
   {
      super( service, parent, null );
      _player = player;
      _currentStatus = service.getPlayerStatus( player );
      service.subscribe( player, onPlayerStatusChanged );
   }

   public void updateCount()
   {
       createPage(0, _pageSize);
   }

   protected List<? extends Object> createPage(int start, int pageSize)
   {
      List<Song> playlist = new ArrayList<Song>();
      if ( _player != null )
      {
         BrowseResult<Song> result = _service.getCurrentPlaylist( _player, start, pageSize );
         playlist = result.getResutls();
         _numItems = result.getTotalItems();
      }
      return playlist;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View v = super.getView( position, convertView, parent );
      if ( _currentStatus != null && position == _currentStatus.getCurrentIndex() )
      {
          TextView titleTextView = (TextView) v.findViewById(R.id.song_name_text);
          if( titleTextView != null )
          {
              titleTextView.setText( "* " + titleTextView.getText() );
          }
      }
      return v;
   }
}
