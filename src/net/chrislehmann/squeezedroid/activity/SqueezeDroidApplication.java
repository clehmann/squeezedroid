package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.CliSqueezeService;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.util.ImageLoader;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SqueezeDroidApplication extends Application
{

   private SqueezeService _service;
   private Player selectedPlayer;

   @Override
	public void onLowMemory() {
	   ImageLoader.getInstance().clearCache();
	   super.onLowMemory();
	}
   
   @Override
   public void onTerminate()
   {
      if( _service != null )
      {
         _service.disconnect();
      }
      super.onTerminate();
   }
   
   public SqueezeService getService()
   {
      if( _service == null )
      {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         String serverIp = prefs.getString( "server_ip", "bonk" );
         String serverWebPort = prefs.getString( "server_web_port", "9000" );
         String serverCliPort = prefs.getString( "server_cli_port", "9090" );

         SqueezeService service = new CliSqueezeService( serverIp, Integer.parseInt( serverCliPort ), Integer.parseInt( serverWebPort ) );
         _service = service;
      }

      return _service;
   }

   public void setService(SqueezeService service)
   {
      this._service = service;
   }

   public Player getSelectedPlayer()
   {
      return selectedPlayer;
   }

   public void setSelectedPlayer(Player selectedPlayer)
   {
      this.selectedPlayer = selectedPlayer;
   }

	public void resetService() {
		if (_service != null && _service.isConnected()) {
			try{
				_service.disconnect();
			} catch (Exception e) {
				Log.e("SQUEEZE", "Error disconnecting from service", e);
			}
		}
		_service = null;
	}
}
