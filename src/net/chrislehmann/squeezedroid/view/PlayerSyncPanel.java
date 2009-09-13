package net.chrislehmann.squeezedroid.view;

import java.util.ArrayList;
import java.util.List;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.service.PlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SimplePlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayerSyncPanel extends LinearLayout
{

   private List<PlayerStatusHandler> handlers = new ArrayList<PlayerStatusHandler>();
   private Player player;
   private SqueezeService service;
   
   public PlayerSyncPanel(Context context, SqueezeService service)
   {
      super( context );
      this.setOrientation( LinearLayout.VERTICAL );
      this.service = service;
   }

   public PlayerSyncPanel(Context context, AttributeSet attrs)
   {
      super( context, attrs );
   }

   public Player getPlayer()
   {
      return player;
   }
   
   
   public void destroy()
   {
      this.removeAllViews();
      
      for ( PlayerStatusHandler handler : handlers )
      {
         service.unsubscribeAll( handler );
      }
      handlers.clear();
      
   }
   
   public synchronized void setPlayer(Player player)
   {
      destroy();
      
      addPlayerControl( player );
      
      for ( Player syncedPlayer : player.getSyncronizedPlayers() )
      {
         addPlayerControl( syncedPlayer );
      }

      
      this.player = player;
      
   }

   private void addPlayerControl(Player player)
   {
      PlayerStatus status = service.getPlayerStatus( player );
      View view = LayoutInflater.from( getContext() ).inflate( R.layout.player_sync_control_layout, null );
      SeekBar volumeSeekBar = (SeekBar) view.findViewById( R.id.volume_seek_bar );
      volumeSeekBar.setProgress( status.getVolume() );
      volumeSeekBar.setOnSeekBarChangeListener( new OnVolumeChangedListener( player ) );
      PlayerStatusHandler handler = new VolumeChangedStatusHandler( volumeSeekBar );
      service.subscribe( player, handler );
      handlers.add( handler );

      TextView playerNameLabel = (TextView) view.findViewById( R.id.player_name_text );
      playerNameLabel.setText( player.getName() );
      this.addView( view );
   }

   public SqueezeService getService()
   {
      return service;
   }

   public void setService(SqueezeService service)
   {
      this.service = service;
   }
   
   private class OnVolumeChangedListener implements OnSeekBarChangeListener
   {
      private Player _player;
      
      public OnVolumeChangedListener(Player player)
      {
         _player = player;
      }
      
      public void onStopTrackingTouch(SeekBar seekBar){}
      
      public void onStartTrackingTouch(SeekBar seekBar){}
      
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
      {
         if( fromUser )
         {
            service.changeVolume( _player, progress );
         }
      }
   };
  
   private class VolumeChangedStatusHandler extends SimplePlayerStatusHandler
   {
      private SeekBar seekBar;
      public VolumeChangedStatusHandler( SeekBar seekBar )
      {
         this.seekBar = seekBar;
      }
      
      @Override
      public void onVolumeChanged(int newVolume)
      {
         this.seekBar.setProgress( newVolume );
      }
   }
}
