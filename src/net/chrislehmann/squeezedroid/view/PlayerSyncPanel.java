package net.chrislehmann.squeezedroid.view;

import java.util.HashMap;
import java.util.Map;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.service.PlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SimplePlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayerSyncPanel extends LinearLayout
{

   private static final String LOGTAG = "PlayerSyncPanel";

   private class Syncronization
   {
      public Syncronization(View view, PlayerStatusHandler volumeHandler, PlayerStatusHandler syncHandler)
      {
         this.view = view;
         this.volumeHandler = volumeHandler;
         this.syncHandler = syncHandler;
      }

      public View view;
      public PlayerStatusHandler volumeHandler;
      public PlayerStatusHandler syncHandler;
   }

   private Map<String, Syncronization> syncronizations = new HashMap<String, Syncronization>();
   private Player player;
   private SqueezeService service;
   
   private Activity parent;
   
   
   public PlayerSyncPanel(Context context, SqueezeService service, Activity parent)
   {
      super( context );
      this.setOrientation( LinearLayout.VERTICAL );
      setBaselineAligned( false ); 
      this.service = service;
      this.parent = parent;
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
      for ( String id: syncronizations.keySet() )
      {
         removeSyncronization( id );
      }
   }
   
   private void removeSyncronization( String playerId  )
   {
      final Syncronization sync = syncronizations.get( playerId );
      service.unsubscribeAll( sync.syncHandler );
      service.unsubscribeAll( sync.volumeHandler );
      parent.runOnUiThread( new Runnable()
      {
         public void run()
         {
            removeView( sync.view );
         }
      });
   }
   
   public synchronized void setPlayer(Player player)
   {
      destroy();
      
      addSynchronization( player, true );
      
      for ( Player syncedPlayer : player.getSyncronizedPlayers() )
      {
         addSynchronization( syncedPlayer, false );
      }
      
      this.player = player;
      
   }

   private void addSynchronization(Player player, boolean isPrimary)
   {
      PlayerStatus status = service.getPlayerStatus( player );
      View view = LayoutInflater.from( getContext() ).inflate( R.layout.player_sync_control_layout, null );
      SeekBar volumeSeekBar = (SeekBar) view.findViewById( R.id.volume_seek_bar );
      volumeSeekBar.setProgress( status.getVolume() );
      volumeSeekBar.setOnSeekBarChangeListener( new OnVolumeChangedListener( player ) );
      
      ImageButton unsyncButton = (ImageButton) view.findViewById( R.id.unsync_button );
      if( isPrimary )
      {
         unsyncButton.setVisibility( view.INVISIBLE );
      }
      else
      {
         unsyncButton.setOnClickListener( new OnUnsyncButtonPressedListener( player ) );
      }
      
      PlayerStatusHandler volumeHandler = new VolumeChangedStatusHandler( volumeSeekBar );
      service.subscribe( player, volumeHandler );

      PlayerStatusHandler syncHandler = new MainPlayerOnSyncHandler();
      syncronizations.put( player.getId(), new Syncronization( view, volumeHandler, syncHandler ) );
      service.subscribe( player, syncHandler );
      
      TextView playerNameLabel = (TextView) view.findViewById( R.id.player_name_text );
      playerNameLabel.setText( player.getName() );

      this.addView( view );
   }

   public SqueezeService getService()
   {
      return service;
   }

   private class OnUnsyncButtonPressedListener implements OnClickListener
   {
      private Player player;

      public OnUnsyncButtonPressedListener(Player player)
      {
         this.player = player;
      }

      public void onClick(View v)
      {
         Toast.makeText( getContext(), player.getName() + " unsynchronized.", Toast.LENGTH_LONG ).show();
         service.unsynchronize( player );
      }
   }
   
   public void setService(SqueezeService service)
   {
      this.service = service;
   }
   
   private class OnVolumeChangedListener implements OnSeekBarChangeListener
   {
      private Player _player;
      private int volume = 0;
      private boolean seeking = false;
      
      public OnVolumeChangedListener(Player player)
      {
         _player = player;
      }
      
      public void onStartTrackingTouch(SeekBar seekBar){ seeking = true; }
      
      public void onStopTrackingTouch(SeekBar seekBar)
      {
         if ( seeking )
         {
            service.changeVolume( _player, volume );
         }
         seeking = false;
      }
      
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
      {
         if( fromUser )
         {
            volume = progress;
            //This will happen if the hardware buttons are used
            if( !seeking )
            {
               service.changeVolume( _player, volume );
            }
            
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
   
   private class MainPlayerOnSyncHandler extends SimplePlayerStatusHandler
   {

      @Override
      public void onPlayerSynchronized(final Player mainPlayer, String newPlayerId)
      {
         Log.d( LOGTAG, "Got player sync event mainPlayer: " +  mainPlayer.getId() + ", new player: " + newPlayerId);
         parent.runOnUiThread( new Runnable()
         {
            public void run()
            {
               setPlayer( mainPlayer );
            }
         });

      }

      @Override
      public void onPlayerUnsynchronized()
      {
         updatePlayer();
      }
      
      @Override
      public void onDisconnect()
      {
         updatePlayer();
      }

      private void updatePlayer()
      {
         final Player updatedPlayer = service.getPlayer( player.getId() );
         parent.runOnUiThread( new Runnable()
         {
            public void run()
            {
               setPlayer( updatedPlayer );
            }
         });
      }
      
   }
}
