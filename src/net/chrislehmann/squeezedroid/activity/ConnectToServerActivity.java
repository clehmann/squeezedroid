package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.exception.ApplicationException;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * Activity that connects to the server (showing a progress dialog).  If a connection 
 * cannot be established, a dialog presenting some options (reconnect, show settings, cancel)
 * will be presented to the user.  If no connection can be established, a RESULT_CLOSE_APPLICATION_CHAIN
 * result type will be set, closing the application.
 * 
 * @author lehmanc
 */
public class ConnectToServerActivity extends SqueezedroidActivitySupport
{
   private static final int DIALOG_ERROR_CONNECTING = 0;
   private static final int DIALOG_CONNECTING = 1;

   private String LOGTAG = "ConnectToServerActivity";

   private ProgressDialog _connectingDialog;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );
      if ( !areSettingInitalized() )
      {
         launchSubActivity( EditPrefrencesActivity.class, settingsCallback );
      }
      else
      {
         if ( !_connectionThread.isAlive() )
         {
            _connectionThread.start();
         }
      }
   }

   private Context context = this;

   ConnectionThread _connectionThread = new ConnectionThread();

   /**
    * Thread responsible for initiating the connection to the server.
    * @author lehmanc
    */
   private class ConnectionThread extends Thread
   {
      public void run()
      {
         final Thread thisThread = this;
         SqueezeService service = null;
         try
         {
            runOnUiThread( new Runnable()
            {
               public void run()
               {
                  OnCancelListener onCancelListener = new OnCancelListener()
                  {

                     public void onCancel(DialogInterface dialog)
                     {
                        thisThread.interrupt();
                        showDialog( DIALOG_ERROR_CONNECTING );
                     }
                  };
                  _connectingDialog = ProgressDialog.show( context, "Connecting...", "Connecting to squeezeserver.", true, true );
                  _connectingDialog.setOnCancelListener( onCancelListener );
               }
            } );

            service = getService(false);
            if ( !service.isConnected() )
            {
               //throw new ApplicationException("bad", null);
               service.connect();
            }
            if ( !_connectionThread.isInterrupted() )
            {
               setResult( RESULT_OK );
               finish();
            }
         }
         catch ( ApplicationException e )
         {
            runOnUiThread( new Runnable()
            {

               public void run()
               {

                  Log.d(LOGTAG, "ConnectionThread:" + thisThread + " Is Interupred: " + thisThread.isInterrupted());
                  Log.d(LOGTAG, "Is Finishing: for thread" + thisThread + ": " + isFinishing());

                  if ( !thisThread.isInterrupted() && !isFinishing() )
                  {
                     Log.d(LOGTAG, "ConnectionThread " + thisThread + "(In the if): Is Interupred: " + thisThread.isInterrupted());

                     
                     _connectingDialog.hide();
                     showDialog( DIALOG_ERROR_CONNECTING );
                  }
               }
            } );
         }
      };
   };

   public boolean areSettingInitalized()
   {
      boolean initalized = true;
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
      String serverIp = prefs.getString( "server_ip", "not_initalized" );

      if ( "not_initalized".equals( serverIp ) )
      {
         initalized = false;
      }

      return initalized;
   }

   private IntentResultCallback settingsCallback = new IntentResultCallback()
   {
      
      public void resultOk(String resultString, Bundle resultMap)
      {
         getSqueezeDroidApplication().resetService();
         _connectionThread = new ConnectionThread();
         _connectionThread.start();
      }
      
      public void resultCancel(String resultString, Bundle resultMap)
      {
         resultOk( resultString, resultMap );
      }
   };

   @Override
   protected Dialog onCreateDialog(int id)
   {
      Dialog d = null;
      switch ( id )
      {
         case DIALOG_ERROR_CONNECTING :
            d = createErrorConnectingDialog();
            break;
         case DIALOG_CONNECTING :
            _connectingDialog = ProgressDialog.show( this, "Connecting...", "Connecting to squeezeserver.", true, false );
            d = _connectingDialog;
            break;
      }
      return d;
   }

   private Dialog createErrorConnectingDialog()
   {
      AlertDialog.Builder builder = new AlertDialog.Builder( this );
      builder.setTitle( "Error Connecting" );
      builder.setMessage( "There was an error connecting to the server." );
      builder.setOnCancelListener( new OnCancelListener()
      {
         public void onCancel(DialogInterface dialog)
         {
            closeApplication();
         }
      });
      builder.setPositiveButton( "Reconnect", new OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            _connectionThread = new ConnectionThread();
            _connectionThread.start();
         }
      } );
      builder.setNegativeButton( "Cancel", new OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            closeApplication();
         }
      } );
      builder.setNeutralButton( "Settings", new OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            launchSubActivity( EditPrefrencesActivity.class, settingsCallback );
         }
      } );

      return builder.create();
   }
}
