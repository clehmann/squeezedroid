package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SqueezedroidActivitySupport extends ActivitySupport
{
   private String LOGTAG = "SqueezeDroidActivitySupport";

   /**
    * Gets the SqueezeService and makes sure it is still connected.  If not connected, this fill start the {@link ConnectToServerActivity} 
    * and return null.  Your code should take this into account.
    * @return
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
         if( onConnect != null )
         {
            try
            {
               onConnect.runWithService( service );
            }
            catch ( Exception e )
            {
               Log.e( LOGTAG, "Error executing callback",  e);
            } 
         }
      }
      return service;
   }

   protected void runWithService(final SqueezeServiceAwareThread onConnect)
   {
      getService(true, onConnect);
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

   protected SqueezeService getService(boolean connect)
   {
      return getService( connect, null );
   }

   protected SqueezeService getService()
   {
      return getService( true, null );
   }

   protected Player getSelectedPlayer()
   {
      return getSqueezeDroidApplication().getSelectedPlayer();
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

   protected interface SqueezeServiceAwareThread
   {
      public void runWithService(SqueezeService service) throws Exception;
   }
}
