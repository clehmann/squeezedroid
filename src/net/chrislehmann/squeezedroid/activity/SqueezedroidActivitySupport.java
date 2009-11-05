package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.activity.ActivitySupport.IntentResultCallback;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Base activity that contains some methods to manage the {@link SqueezeService} and the 
 * currently selected {@link Player} objects. 
 * @author lehmanc
 *
 */
public class SqueezedroidActivitySupport extends ActivitySupport
{
   //private static final String LOGTAG = "SqueezeDroidActivitySupport";

   BroadcastReceiver onConnectionChanged = new BroadcastReceiver()
   {

      @Override
      public void onReceive(Context context, Intent intent)
      {
         boolean isDisconnected = intent.getBooleanExtra( ConnectivityManager.EXTRA_NO_CONNECTIVITY, false );
         if ( isDisconnected )
         {
            getSqueezeDroidApplication().resetService();
         }
      }
   };

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );

      IntentFilter filter = new IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION );
      registerReceiver( onConnectionChanged, filter );
   }

   /**
    * Gets the currently selected player.  The previous selected player will be tried If no player is selected. 
    * Finally, this will forward to the choose player action if the user needs to choose a player.
    * @return the currently selected player
    */
   protected Player getSelectedPlayer()
   {
      Player selectedPlayer = getSqueezeDroidApplication().getSelectedPlayer();
      if ( selectedPlayer == null )
      {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this.getBaseContext() );
         String lastPlayerId = prefs.getString( SqueezeDroidConstants.Preferences.LAST_SELECTED_PLAYER, null );
         SqueezeService service = getService();
         if ( service != null )
         {
            if ( lastPlayerId != null )
            {
               selectedPlayer = service.getPlayer( lastPlayerId );
            }
            if( selectedPlayer == null )
            {
               launchSubActivity( ChoosePlayerActivity.class,  choosePlayerIntentCallback);
            }
         }
      }
      return selectedPlayer;
   }

   /**
    * Sets the currently selected player.
    * @player the currently selected player
    */
   protected void setSelectedPlayer(Player player)
   {
      getSqueezeDroidApplication().setSelectedPlayer( player );
   }

   /**
    * Returns true if a player is selected
    */
   protected boolean isPlayerSelected()
   {
      return getSelectedPlayer() != null;
   }

   /**
    * Helper method to simply get the application and cast it to a {@link SqueezeDroidApplication}
    * @param context
    * @return
    */
   public SqueezeDroidApplication getSqueezeDroidApplication()
   {
      return (SqueezeDroidApplication) getApplication();
   }


   /**
    * Ensures that the {@link SqueezeService} is connected (possibly by forwarding to the {@link ConnectToServerActivity} and
    * calls the {@link SqueezeServiceAwareThread#runWithService(SqueezeService)} with a connected {@link SqueezeService}
    * 
    * @param onConnect {@link SqueezeServiceAwareThread} to run after the server connection has been obtained.
    * @param runOnThread If set to true, a new thread will be spawned to run the onConnect
    */
   public void runWithService(final SqueezeServiceAwareThread onConnect, boolean runOnThread)
   {

      if ( runOnThread )
      {
         new SqueezeServiceAwareThread()
         {
            public void runWithService(final SqueezeService service)
            {
               new Thread()
               {
                  public void run()
                  {
                     onConnect.runWithService( service );
                  };
               }.start();
            }
         };
      }

      getSqueezeDroidApplication().getConnectionManager().getService( this, true, onConnect );
   }

   /**
    * Ensures that the {@link SqueezeService} is connected (possibly by forwarding to the {@link ConnectToServerActivity} and
    * calls the {@link SqueezeServiceAwareThread#runWithService(SqueezeService)} with a connected {@link SqueezeService}
    * 
    * @param onConnect {@link SqueezeServiceAwareThread} to run after the server connection has been obtained.
    */
   public void runWithService(final SqueezeServiceAwareThread onConnect)
   {
      runWithService( onConnect, false );
   }


   /**
    * Gets the {@link SqueezeService}.  If the connect parameter is set to true and the {@link SqueezeService} is not connected, 
    * this method will start the {@link ConnectToServerActivity} and return null.  Your code should take this into account.
    * 
    * @param connect If true, try and connect to the service if it is not connected
    */
   public SqueezeService getService(boolean connect)
   {
      return getSqueezeDroidApplication().getConnectionManager().getService( this, connect, null );
   }

   /**
    * Gets the {@link SqueezeService}.  If the {@link SqueezeService} is not connected, 
    * this method will start the {@link ConnectToServerActivity} and return null.  Your code should take this into account.
    * 
    * @param connect If true, try and connect to the service if it is not connected
    */
   public SqueezeService getService()
   {
      return getService( true );
   }


   /**
    * Child Activity callback {@link IntentResultCallback}s
    */
   protected IntentResultCallback choosePlayerIntentCallback = new IntentResultCallback()
   {
      public void resultOk(String resultString, Bundle resultMap)
      {
         Player selectedPlayer = (Player) resultMap.getSerializable( SqueezeDroidConstants.IntentDataKeys.KEY_SELECTED_PLAYER );
         if ( selectedPlayer == null )
         {
            closeApplication();
         }
         getSqueezeDroidApplication().setSelectedPlayer( selectedPlayer );
      }

      public void resultCancel(String resultString, Bundle resultMap)
      {
         if ( getSqueezeDroidApplication().getSelectedPlayer() == null )
         {
            closeApplication();
         }

      }
   };

}
