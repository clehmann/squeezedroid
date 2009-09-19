package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.exception.ApplicationException;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ConnectToServerActivity extends SqueezedroidActivitySupport
{
   private static final int DIALOG_ERROR_CONNECTING = 0;
   private static final int DIALOG_CONNECTING = 1;

   private ProgressDialog _connectingDialog;
   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );
      if( !areSettingInitalized() )
      {
         startSettingsActivity();
      }
      else
      {
         if( !_connectionThread.isAlive() )
         {
            _connectionThread.start();
         }
      }
   }

   private void startSettingsActivity()
   {
      Intent intent = new Intent();
      intent.setAction( "net.chrislehmann.squeezedroid.action.EditPreferences" );
      this.startActivityForResult( intent, SqueezeDroidConstants.RequestCodes.REQUEST_SHOW_SETTINGS );
   }
   private Context context = this;
   
	ConnectionThread _connectionThread = new ConnectionThread();

	private class ConnectionThread extends Thread {

		public void run() {
			SqueezeService service = null;
			try {
				runOnUiThread(new Runnable() {
					public void run() {
						OnCancelListener onCancelListener = new OnCancelListener() {
								
								public void onCancel(DialogInterface dialog) {
									_connectionThread.interrupt();
									showDialog(DIALOG_ERROR_CONNECTING);
								}
							};
						_connectingDialog = ProgressDialog.show(context,
								"Connecting...",
								"Connecting to squeezeserver.", true, true);
						_connectingDialog.setOnCancelListener( onCancelListener );
					}
				});

				service = getSqueezeDroidApplication().getService();
				if (!service.isConnected()) {
					//throw new ApplicationException("bad", null);
					service.connect();
				}
				if( !isInterrupted() )
				{
					setResult(RESULT_OK);
					finish();
				}
			} catch (ApplicationException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						_connectingDialog.hide();
						showDialog(DIALOG_ERROR_CONNECTING);
					}
				});
			}
		};
	};
      
   public boolean areSettingInitalized()
   {
      boolean initalized = true;
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      String serverIp = prefs.getString( "server_ip", "not_initalized" );

      if( "not_initalized".equals( serverIp ) )
      {
         initalized = false;
      }
      
      return initalized;
   }
   
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      switch ( requestCode )
      {
         case SqueezeDroidConstants.RequestCodes.REQUEST_SHOW_SETTINGS:
        	 getSqueezeDroidApplication().resetService();
        	_connectionThread = new ConnectionThread();	
        	_connectionThread.start();
            break;
         case SqueezeDroidConstants.RequestCodes.REQUEST_CONNECT:
        	 setResult(resultCode);
        	 finish();
        	break;
         default :
            break;
      }
      
      //super.onActivityResult( requestCode, resultCode, data );
   }
   
   @Override
   protected Dialog onCreateDialog(int id)
   {
      Dialog d = null;
      switch ( id )
      {
         case DIALOG_ERROR_CONNECTING:
            d = createErrorConnectingDialog();
            break;
         case DIALOG_CONNECTING:
            _connectingDialog = ProgressDialog.show( this, "Connecting...", "Connecting to squeezeserver.", true, false );
            d = _connectingDialog;
            break;
      }
      return d;
   }

   private Dialog createErrorConnectingDialog()
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle( "Error Connecting" );
      builder.setMessage( "There was an error connecting to the server." );
      builder.setPositiveButton( "Reconnect", new OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            Intent i = new Intent();
            i.setAction( "net.chrislehmann.squeezedroid.action.Status" );
            startActivityForResult( i, SqueezeDroidConstants.RequestCodes.REQUEST_CONNECT );
         }
      });
      builder.setNegativeButton( "Cancel", new OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            setResult( RESULT_CANCELED );
            finish();
         }
      });
      builder.setNeutralButton( "Settings", new OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            startSettingsActivity();
         }
      });

      return builder.create();
   }
}
