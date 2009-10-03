package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Base activity that contains some methods to manage the {@link SqueezeService} and the 
 * currently selected {@link Player} objects. 
 * @author lehmanc
 *
 */
public class SqueezedroidActivitySupport extends ActivitySupport
{
   private static final String LOGTAG = "SqueezeDroidActivitySupport";

   /**
    * Interface that simply contains a callback that will be executed within the context of a valid, connected {@link SqueezeService}
    * @author lehmanc
    */
   protected interface SqueezeServiceAwareThread
   {
      public void runWithService(SqueezeService service);
   }

   /**
    * Ensures that the {@link SqueezeService} is connected (possibly by forwarding to the {@link ConnectToServerActivity} and
    * calls the {@link SqueezeServiceAwareThread#runWithService(SqueezeService)} with a connected {@link SqueezeService}
    * 
    * @param onConnect {@link SqueezeServiceAwareThread} to run after the server connection has been obtained.
    * @param runOnThread If set to true, a new thread will be spawned to run the onConnect
    */
   protected void runWithService(final SqueezeServiceAwareThread onConnect, boolean runOnThread)
   {
      SqueezeServiceAwareThread onConnectThread = onConnect;
      if( runOnThread )
      {
         onConnectThread = new SqueezeServiceAwareThread()
         {
            public void runWithService(final SqueezeService service)
            {
               new Thread()
               {
                  public void run() { onConnect.runWithService( service ); };
               }.start();
            }
         };
      }
      
      getService( true, onConnect );
   }

   /**
    * Ensures that the {@link SqueezeService} is connected (possibly by forwarding to the {@link ConnectToServerActivity} and
    * calls the {@link SqueezeServiceAwareThread#runWithService(SqueezeService)} with a connected {@link SqueezeService}
    * 
    * @param onConnect {@link SqueezeServiceAwareThread} to run after the server connection has been obtained.
    */
   protected void runWithService(final SqueezeServiceAwareThread onConnect)
   {
      runWithService( onConnect, false );
   }
   /**
    * Gets the {@link SqueezeService}.  If the connect parameter is set to true and the {@link SqueezeService} is not connected, 
    * this method will start the {@link ConnectToServerActivity} and return null.  Your code should take this into account.
    * 
    * @param connect If true, try and connect to the service if it is not connected
    */
   protected SqueezeService getService(boolean connect)
   {
      return getService( connect, null );
   }

   /**
    * Gets the {@link SqueezeService}.  If the {@link SqueezeService} is not connected, 
    * this method will start the {@link ConnectToServerActivity} and return null.  Your code should take this into account.
    * 
    * @param connect If true, try and connect to the service if it is not connected
    */
   protected SqueezeService getService()
   {
      return getService( true, null );
   }

   /**
    * Gets the currently selected player.  If no player is selected, this will return null
    * @return the currently selected player
    */
   protected Player getSelectedPlayer()
   {
      return getSqueezeDroidApplication().getSelectedPlayer();
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
      return getSqueezeDroidApplication().getSelectedPlayer() != null;
   }

   /**
    * Helper method to simply get the application and cast it to a {@link SqueezeDroidApplication}
    * @param context
    * @return
    */
   protected SqueezeDroidApplication getSqueezeDroidApplication()
   {
      return (SqueezeDroidApplication) getApplication();
   }


   /**
    * Gets the {@link SqueezeService}.  If the connect parameter is set to true and the {@link SqueezeService} is not connected, 
    * this method will start the {@link ConnectToServerActivity} and return null.  Your code should take this into account.
    * 
    * @param connect  If set to true, this method will attempt to connect the {@link SqueezeService} by starting the 
    * {@link ConnectToServerActivity} and return null
    * @param onConnect A {@link SqueezeServiceAwareThread} that will be executed when the server is connected.  If the server is
    * already connected, this will be executed immediately.
    */
   private SqueezeService getService(boolean connect, final SqueezeServiceAwareThread onConnect)
   {
      SqueezeService service = getSqueezeDroidApplication().getService();
      if ( connect && (service == null || !service.isConnected()) )
      {
         service = null;
         Intent intent = new Intent();
         intent.setAction( SqueezeDroidConstants.Actions.ACTION_CONNECT );
         launchSubActivity( ConnectToServerActivity.class, new ExecuteWithServiceCallback( onConnect ) );
      }
      else if ( service != null && service.isConnected() )
      {
         if ( onConnect != null )
         {
            try
            {
               onConnect.runWithService( service );
            }
            catch ( Exception e )
            {
               Log.e( LOGTAG, "Error executing callback", e );
            }
         }
      }
      return service;
   }

   private class ExecuteWithServiceCallback implements IntentResultCallback
   {
      SqueezeServiceAwareThread thread;

      public ExecuteWithServiceCallback(SqueezeServiceAwareThread thread)
      {
         this.thread = thread;
      }

      public void resultOk(String resultString, Bundle resultMap)
      {
         if ( thread != null )
         {
            try
            {
               thread.runWithService( getSqueezeDroidApplication().getService() );
            }
            catch ( Exception e )
            {
               Log.e( LOGTAG, "Error executing callback", e );
            }
         }
      }

      public void resultCancel(String resultString, Bundle resultMap)
      {
         finish();
      }
   };

}
