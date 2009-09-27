package net.chrislehmann.squeezedroid.activity;

import java.util.HashMap;
import java.util.Map;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.listadapter.PlayListAdapter;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.PlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SimplePlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.SqueezeService.RepeatMode;
import net.chrislehmann.squeezedroid.view.PlayerSyncPanel;
import net.chrislehmann.squeezedroid.view.TransparentPanel;
import net.chrislehmann.squeezedroid.view.UpdatingSeekBar;
import net.chrislehmann.util.ImageLoader;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressWarnings("serial")
public class MainActivity extends SqueezedroidActivitySupport
{
   private static final String LOGTAG = "MainActivity";
   
   private static final int MENU_SETTINGS = 1;
   private static final int MENU_CHOOSE_PLAYER = 2;
   private static final int MENU_ADD_PLAYER = 3;
   private static final int MENU_PLAYLIST = 4;
   private static final int MENU_LIBRARY = 5;

   private PlayListAdapter _playlistListAdapter;
   private ViewSwitcher _coverArtImageView;

   private TextView _songLabel;
   private TextView _artistLabel;
   private TextView _albumLabel;

   private Activity context = this;

   private ImageButton _prevButton;
   private ImageButton _nextButton;
   private ImageButton _playButton;
   private ImageButton _shuffleButton;
   private ImageButton _repeatButton;
   private ImageButton _toggleVolumeButton;

   private PlayerSyncPanel _syncPanel;
   private TransparentPanel _volumePanel;
   private UpdatingSeekBar _timeSeekBar;

   private PlayerStatus _currentStatus;
   
   OnClickListener onPlayButtonPressed = new android.view.View.OnClickListener()
   {
      public void onClick(View v)
      {
         SqueezeService service = ActivityUtils.getService( context );
         if ( service != null )
         {
            service.togglePause( getSqueezeDroidApplication().getSelectedPlayer() );
         }
      }
   };
  
   OnClickListener onNextButtonPressed = new android.view.View.OnClickListener()
   {
      public void onClick(View v)
      {
         SqueezeService service = ActivityUtils.getService( context );
         if ( service != null )
         {
            service.jump( getSqueezeDroidApplication().getSelectedPlayer(), "+1" );
         }
      }
   };

   OnClickListener onPrevButtonPressed = new android.view.View.OnClickListener()
   {
      public void onClick(View v)
      {
         SqueezeService service = ActivityUtils.getService( context );
         if ( service != null )
         {
            service.jump( getSqueezeDroidApplication().getSelectedPlayer(), "-1" );
         }
      }
   };

   OnClickListener onToggleVolumeButtonPressed = new OnClickListener()
   {
      public void onClick(View v)
      {
         int visibility = View.VISIBLE;
         if ( _volumePanel.getVisibility() == View.VISIBLE )
         {
            visibility = View.INVISIBLE;
         }
         _volumePanel.setVisibility( visibility );
         _volumePanel.bringToFront();
      }
   };


   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );

      requestWindowFeature( Window.FEATURE_NO_TITLE );
      this.setContentView( R.layout.main_layout );

      _coverArtImageView = (ViewSwitcher) findViewById( R.id.cover_image );

      _artistLabel = (TextView) findViewById( R.id.artist_label );
      _albumLabel = (TextView) findViewById( R.id.album_label );
      _songLabel = (TextView) findViewById( R.id.title_label );
      
      _playButton = (ImageButton) findViewById( R.id.playButton );
      _nextButton = (ImageButton) findViewById( R.id.nextButton );
      _prevButton = (ImageButton) findViewById( R.id.prevButton );
      _shuffleButton = (ImageButton) findViewById( R.id.shuffleButton );
      _repeatButton = (ImageButton) findViewById( R.id.repeatButton );
      _toggleVolumeButton = (ImageButton) findViewById( R.id.toggleVolumeButton );
      _timeSeekBar = new UpdatingSeekBar( (SeekBar) findViewById( R.id.timeSeekBar ) );
      _volumePanel = (TransparentPanel) findViewById( R.id.volume_panel);
      
      _timeSeekBar.setOnSeekBarChangeListener( onTimeUpdatedByUser );
      _prevButton.setOnClickListener( onPrevButtonPressed );
      _playButton.setOnClickListener( onPlayButtonPressed );
      _nextButton.setOnClickListener( onNextButtonPressed );
      _shuffleButton.setOnClickListener( onShuffleButtonPressed );
      _repeatButton.setOnClickListener( onRepeatButtonPressed );
      _toggleVolumeButton.setOnClickListener( onToggleVolumeButtonPressed );
      
      if ( !isPlayerSelected() )
      {
         launchSubActivity( ChoosePlayerActivity.class,  choosePlayerIntentCallback);
      }
      else
      {
         onPlayerChanged();
      }
   }


   private IntentResultCallback choosePlayerIntentCallback = new IntentResultCallback()
   {
      public void resultOk(String resultString, Bundle resultMap)
      {
            Player selectedPlayer = (Player) resultMap.getSerializable( SqueezeDroidConstants.IntentDataKeys.KEY_SELECTED_PLAYER );
            getSqueezeDroidApplication().setSelectedPlayer( selectedPlayer );
            onPlayerChanged();
      }
      
      public void resultCancel(String resultString, Bundle resultMap)
      {
         if( getSqueezeDroidApplication().getSelectedPlayer() == null )
         {
            finish();
         }
      }
   };

   
   private IntentResultCallback choosePlayerForAddCallback = new IntentResultCallback()
   {
      public void resultOk(String resultString, Bundle resultMap)
      {
         SqueezeService service = getSqueezeDroidApplication().getService();
         if( service != null )
         {
            //service.unsynchronize( getSelectedPlayer() );
            service.synchronize( getSelectedPlayer(), (Player) resultMap.getSerializable( SqueezeDroidConstants.IntentDataKeys.KEY_SELECTED_PLAYER ) );         
         }
      }
      
      public void resultCancel(String resultString, Bundle resultMap){}
   };

   @Override
   protected void onDestroy()
   {
      SqueezeService service = ActivityUtils.getService( context, false );
      if ( service != null )
      {
         service.unsubscribeAll( onPlayerStatusChanged );
      }
      super.onDestroy();
   }

   public boolean onCreateOptionsMenu(Menu menu)
   {
      menu.add( 0, MENU_LIBRARY, 0, "Library" );
      menu.add( 0, MENU_PLAYLIST, 0, "Playlist" );
      menu.add( 0, MENU_SETTINGS, 0, "Settings" );
      menu.add( 0, MENU_ADD_PLAYER, 0, "Sync Player" );
      menu.add( 0, MENU_CHOOSE_PLAYER, 0, "Choose Player" );
      return true;
   }
   
   private Player getSelectedPlayer()
   {
      return ActivityUtils.getSqueezeDroidApplication( context ).getSelectedPlayer();
   }

   private boolean isPlayerSelected()
   {
      return getSqueezeDroidApplication().getSelectedPlayer() != null;
   }
   

   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch ( item.getItemId() )
      {
         case MENU_LIBRARY :
            launchSubActivity( BrowseRootActivity.class, null );
            return true;
         case MENU_PLAYLIST :
            launchSubActivity( PlayListActivity.class, null );
            return true;
         case MENU_SETTINGS :
            launchSubActivity( EditPrefrencesActivity.class, null );
            return true;
         case MENU_CHOOSE_PLAYER :
            launchSubActivity( ChoosePlayerActivity.class, choosePlayerIntentCallback );
            return true;
         case MENU_ADD_PLAYER:
            launchSubActivity( ChoosePlayerActivity.class, choosePlayerForAddCallback );
            return true;
      }
      return false;
   }

   private synchronized void updateSongDisplay(final PlayerStatus status)
   {
      if ( status != null && status.getCurrentSong() != null )
      {
         Song currentSong = status.getCurrentSong();

         //Update the image if it has changed...
         if ( _currentStatus == null || !_currentStatus.getCurrentSong().getImageUrl().equals( status.getCurrentSong().getImageUrl() ) )
         {
            if ( _currentStatus != null && _currentStatus.getCurrentIndex() <= status.getCurrentIndex() )
            {
               _coverArtImageView.setOutAnimation( this, R.anim.slide_out_left );
               _coverArtImageView.setInAnimation( this, R.anim.slide_in_right );
            }
            else
            {
               _coverArtImageView.setOutAnimation( this, R.anim.slide_out_right );
               _coverArtImageView.setInAnimation( this, R.anim.slide_in_left );
            }

            ImageView nextView = (ImageView) _coverArtImageView.getNextView();
            nextView.setImageBitmap( null );
            ImageLoader.getInstance().load( nextView, status.getCurrentSong().getImageUrl(), true );
            _coverArtImageView.showNext();
         }

         _songLabel.setText( currentSong.getName() );
         _artistLabel.setText( currentSong.getArtist() );
         _albumLabel.setText( currentSong.getAlbum() );
         
         if(status.isPlaying())
         {
            _playButton.setImageResource( R.drawable.pause );
         }
         else
         {
            _playButton.setImageResource( R.drawable.play );
         }

         _timeSeekBar.setMax( currentSong.getDurationInSeconds() );
         if( status.isPlaying() )
         {
            _timeSeekBar.start();
         }

         _currentStatus = status;
         updateRepeatMode( status.getRepeatMode() );
         updateShuffleMode( status.getShuffleMode() );
      }
      else
      {
         ImageView nextView = (ImageView) _coverArtImageView.getNextView();
         nextView.setImageBitmap( null );
         _coverArtImageView.showNext();
         
         _songLabel.setText( "" );
         _artistLabel.setText( "" );
         _albumLabel.setText( "" );
         _timeSeekBar.pause();
         _timeSeekBar.setProgress( 0 );
      }
   }


   private PlayerStatusHandler onPlayerStatusChanged = new SimplePlayerStatusHandler()
   {

      public void onSongChanged(final PlayerStatus status)
      {
               
         if ( _currentStatus == null || _currentStatus.getCurrentSong().getId() != status.getCurrentSong().getId() )
         {
            final BrowseResult<Song> playlist = ActivityUtils.getService( context ).getCurrentPlaylist( getSqueezeDroidApplication().getSelectedPlayer(), status.getCurrentIndex(), 2 );
            runOnUiThread( new Thread()
            {
               public void run()
               {
                  // Cache the next album art
                  updateSongDisplay( status );
                  if ( playlist.getResutls().size() > 1 )
                  {
                     ImageLoader.getInstance().load( null, playlist.getResutls().get( 1 ).getImageUrl(), true );
                  }
               }
            } );
         }
         _timeSeekBar.setProgress( status.getCurrentPosition() );

      }
      
      public void onPlay() { 
         runOnUiThread( new Runnable()
         {
            public void run()
            {
               _playButton.setImageResource( R.drawable.pause ); 
               _timeSeekBar.start();
            }
         });
      }
      
      public void onPause() { 
         runOnUiThread( new Runnable()
         {
            public void run()
            {
               _playButton.setImageResource( R.drawable.play );
               _timeSeekBar.pause();
            }
         });
      }
      
      public void onStop() { onPause();}
      
      public void onDisconnect() 
      {
         getSqueezeDroidApplication().setSelectedPlayer( null );
         launchSubActivity( ChoosePlayerActivity.class,  choosePlayerIntentCallback);
      };

      public void onRepeatModeChanged(SqueezeService.RepeatMode newMode) {
         updateRepeatMode( newMode );
      };
      
      public void onShuffleModeChanged(SqueezeService.ShuffleMode newMode) {
         updateShuffleMode( newMode );
      };
   };

   private Map<SqueezeService.ShuffleMode, Integer> shuffleModeToIconMap = new HashMap<SqueezeService.ShuffleMode, Integer>()
   {{
      put( SqueezeService.ShuffleMode.ALBUM, R.drawable.shuffle_album );
      put( SqueezeService.ShuffleMode.NONE, R.drawable.shuffle_off );
      put( SqueezeService.ShuffleMode.SONG, R.drawable.shuffle_all );

   }};

   private void updateShuffleMode( final SqueezeService.ShuffleMode newMode)
   {
      _currentStatus.setShuffleMode( newMode );
      runOnUiThread( new Runnable()
      {
         public void run()
         {
            _shuffleButton.setImageResource( shuffleModeToIconMap.get( newMode ) );
         }
      });
   }
   
   private Map<SqueezeService.RepeatMode, Integer> repeatModeToIconMap = new HashMap<SqueezeService.RepeatMode, Integer>()
   {{
      put( SqueezeService.RepeatMode.ALL, R.drawable.repeat_all );
      put( SqueezeService.RepeatMode.NONE, R.drawable.repeat_off );
      put( SqueezeService.RepeatMode.SONG, R.drawable.repeat_song );

   }};

   private void updateRepeatMode( final SqueezeService.RepeatMode newMode)
   {
      _currentStatus.setRepeatMode( newMode );
      runOnUiThread( new Runnable()
      {
         public void run()
         {
            _repeatButton.setImageResource( repeatModeToIconMap.get( newMode ) );
         }
      });
   }
   
   private void onPlayerChanged()
   {
      if ( getSqueezeDroidApplication().getSelectedPlayer() != null )
      {
         SqueezeService service = ActivityUtils.getService( this );
         if ( service != null )
         {
            _volumePanel.removeAllViews();
            _syncPanel = new PlayerSyncPanel( this, service, this );
            _syncPanel.setPlayer( getSelectedPlayer() );
            _volumePanel.addView( _syncPanel );
            
            service.unsubscribeAll( onPlayerStatusChanged );
            service.subscribe( getSelectedPlayer(), onPlayerStatusChanged );

            _playlistListAdapter = new PlayListAdapter( service, this, getSqueezeDroidApplication().getSelectedPlayer() );
            _playlistListAdapter.setPlayer( getSqueezeDroidApplication().getSelectedPlayer() );
            PlayerStatus status = ActivityUtils.getService( this ).getPlayerStatus( getSqueezeDroidApplication().getSelectedPlayer() );
            updateSongDisplay( status );
         }  
      }
   }

   OnClickListener onShuffleButtonPressed = new OnClickListener()
   {
      private Map<SqueezeService.ShuffleMode, SqueezeService.ShuffleMode> nextShuffleModeMap = new HashMap<SqueezeService.ShuffleMode, SqueezeService.ShuffleMode>(){{
         put( SqueezeService.ShuffleMode.NONE, SqueezeService.ShuffleMode.SONG );
         put( SqueezeService.ShuffleMode.SONG, SqueezeService.ShuffleMode.ALBUM );
         put( SqueezeService.ShuffleMode.ALBUM, SqueezeService.ShuffleMode.NONE );
      }};
      
      public void onClick(View v)
      {
         SqueezeService service = getSqueezeDroidApplication().getService();
         if( service != null )
         {
            SqueezeService.ShuffleMode nextMode = nextShuffleModeMap.get( _currentStatus.getShuffleMode() );
            if( nextMode != null )
            {
               service.setShuffleMode( getSelectedPlayer(), nextMode);
            }
         }
      }
   };

   @SuppressWarnings("serial")
   OnClickListener onRepeatButtonPressed = new OnClickListener()
   {
      private Map<SqueezeService.RepeatMode, SqueezeService.RepeatMode> nextRepeatModeMap = new HashMap<SqueezeService.RepeatMode, SqueezeService.RepeatMode>(){
      {
         put( SqueezeService.RepeatMode.NONE, SqueezeService.RepeatMode.SONG );
         put( SqueezeService.RepeatMode.SONG, SqueezeService.RepeatMode.ALL);
         put( SqueezeService.RepeatMode.ALL, SqueezeService.RepeatMode.NONE );
      }};
      
      public void onClick(View v)
      {
         SqueezeService service = getSqueezeDroidApplication().getService();
         if( service != null )
         {
            SqueezeService.RepeatMode nextMode = nextRepeatModeMap.get( _currentStatus.getRepeatMode() );
            if( nextMode != null )
            {
               service.setRepeatMode( getSelectedPlayer(), nextMode);
            }
         }
      }
   };

   @Override
   protected void onResume()
   {
      onPlayerChanged();
      super.onResume();
   }

   OnSeekBarChangeListener onTimeUpdatedByUser = new OnSeekBarChangeListener()
   {
      private int time = 0;

      public void onStopTrackingTouch(SeekBar seekBar)
      {
         Log.v( LOGTAG, "User changed time seek bar position to " + time );
         SqueezeService service = ActivityUtils.getService( context );
         if ( service != null )
         {
            service.seekTo( getSelectedPlayer(), time );
         }
      }
      
      public void onStartTrackingTouch(SeekBar seekBar){}

      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
      {
         if ( fromUser )
         {
            time = progress;
         }
      }
   };
}