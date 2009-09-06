package net.chrislehmann.squeezedroid.view;

import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class UpdatingSeekBar
{
   private static final String LOGTAG = "UpdatingSeekBar";
   
   private SeekBar seekBar;
   private boolean isUserSeeking = false;
   private OnSeekBarChangeListener userOnSeekBarChangeListener;

   public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener)
   {
      userOnSeekBarChangeListener = listener;
   }

   public OnSeekBarChangeListener getOnSeekBarChangeListener()
   {
      return userOnSeekBarChangeListener;
   }

   private OnSeekBarChangeListener onSeekBarChanged = new OnSeekBarChangeListener()
   {
      public void onStopTrackingTouch(SeekBar seekBar)
      {
         isUserSeeking = false;
         Log.v( LOGTAG, "User finished drag" );
         if ( userOnSeekBarChangeListener != null )
         {
            userOnSeekBarChangeListener.onStopTrackingTouch( seekBar );
         }
      }

      public void onStartTrackingTouch(SeekBar seekBar)
      {
         isUserSeeking = true;
         Log.v( LOGTAG, "User starting drag" );
         if ( userOnSeekBarChangeListener != null )
         {
            userOnSeekBarChangeListener.onStartTrackingTouch( seekBar );
         }
      }

      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
      {
         if ( userOnSeekBarChangeListener != null )
         {
            userOnSeekBarChangeListener.onProgressChanged( seekBar, progress, fromUser );
         }
      }
   };


   public UpdatingSeekBar(SeekBar seekBar)
   {
      this.seekBar = seekBar;
      seekBar.setOnSeekBarChangeListener( onSeekBarChanged );
   }

   public void setMax(int max)
   {
      synchronized ( seekBar )
      {
         Log.d( LOGTAG, "Max set to " + max );
         this.seekBar.setMax( max );
      }
   }

   public void start()
   {
      Log.d( LOGTAG, "UpdatingSeekbar started" );
      this._updateSongTimeHandler.postDelayed( _updateSongTimeRunnable, 1000 );
   }

   public void pause()
   {
      this._updateSongTimeHandler.removeCallbacks( _updateSongTimeRunnable );
   }

   public void setProgress(int progress)
   {
      if( !isUserSeeking )
      {
         synchronized ( seekBar )
         {
            Log.d( LOGTAG, "Progress set to " + progress );
            this.seekBar.setProgress( progress );
         }
      }
   }

   private Handler _updateSongTimeHandler = new Handler();

   private Runnable _updateSongTimeRunnable = new Runnable()
   {
      public void run()
      {
         //TODO - calculate based on absolute time using postAtTime instead of postDelayed
         if( !isUserSeeking )
         {
            synchronized ( seekBar )
            {
               int progress = seekBar.getProgress();
               if ( progress < seekBar.getMax() )
               {
                  seekBar.setProgress( progress + 1 );
               }
            }
         }
         _updateSongTimeHandler.postDelayed( this, 1000 );
      }
   };
}
