package net.chrislehmann.squeezedroid.view;

import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class UpdatingSeekBar
{
   private static final String LOGTAG = "UpdatingSeekBar";
   
   private SeekBar _seekBar;
   private boolean _isUserSeeking = false;
   private Boolean _started = false;
   
   private OnSeekBarChangeListener _userOnSeekBarChangeListener;
   private Handler _updateSongTimeHandler = new Handler();

   public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener)
   {
      _userOnSeekBarChangeListener = listener;
   }

   public OnSeekBarChangeListener getOnSeekBarChangeListener()
   {
      return _userOnSeekBarChangeListener;
   }

   private OnSeekBarChangeListener onSeekBarChanged = new OnSeekBarChangeListener()
   {
      public void onStopTrackingTouch(SeekBar seekBar)
      {
         start();
         _isUserSeeking = false;
         Log.v( LOGTAG, "User finished drag" );
         if ( _userOnSeekBarChangeListener != null )
         {
            _userOnSeekBarChangeListener.onStopTrackingTouch( seekBar );
         }
      }

      public void onStartTrackingTouch(SeekBar seekBar)
      {
         pause();
         _isUserSeeking = true;
         Log.v( LOGTAG, "User starting drag" );
         if ( _userOnSeekBarChangeListener != null )
         {
            _userOnSeekBarChangeListener.onStartTrackingTouch( seekBar );
         }
      }

      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
      {
         if ( _userOnSeekBarChangeListener != null )
         {
            _userOnSeekBarChangeListener.onProgressChanged( seekBar, progress, fromUser );
         }
      }
   };


   public UpdatingSeekBar(SeekBar seekBar)
   {
      this._seekBar = seekBar;
      seekBar.setOnSeekBarChangeListener( onSeekBarChanged );
   }

   public void setMax(int max)
   {
      synchronized ( _seekBar )
      {
         Log.d( LOGTAG, "Max set to " + max );
         this._seekBar.setMax( max );
      }
   }

   public void start()
   {
      synchronized ( _started )
      {
         if( !_started )
         {
            Log.d( LOGTAG, "UpdatingSeekbar started" );
            this._updateSongTimeHandler.postDelayed( _updateSongTimeRunnable, 1000 );
            _started = true;
         }
         else
         {
            Log.d( LOGTAG, "UpdatingSeekbar already started, not starting a second time" );
         }
      }
   }

   public void pause()
   {
      synchronized ( _started )
      {
         this._updateSongTimeHandler.removeCallbacks( _updateSongTimeRunnable );
         _started = false;
      }
   }

   public void setProgress(int progress)
   {
      if( !_isUserSeeking )
      {
         synchronized ( _seekBar )
         {
            Log.d( LOGTAG, "Progress set to " + progress );
            this._seekBar.setProgress( progress );
         }
      }
   }


   private Runnable _updateSongTimeRunnable = new Runnable()
   {
      public void run()
      {
         //TODO - calculate based on absolute time using postAtTime instead of postDelayed
         if( !_isUserSeeking )
         {
            synchronized ( _seekBar )
            {
               int progress = _seekBar.getProgress();
               if ( progress < _seekBar.getMax() )
               {
                  _seekBar.setProgress( progress + 1 );
               }
            }
         }
         _updateSongTimeHandler.postDelayed( this, 1000 );
      }
   };
}
