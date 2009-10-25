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

   private static final String LOGTAG = "SqueezeDroidApplication";
   private SqueezeService service;
   private Player selectedPlayer;
   
   @Override
   public void onLowMemory()
   {
      ImageLoader.getInstance().clearCache();
      super.onLowMemory();
   }

   
   @Override
   public void onTerminate()
   {
      if ( service != null )
      {
         service.disconnect();
      }
      super.onTerminate();
   }

   public SqueezeService getService()
   {
      if ( service == null )
      {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
         String serverIp = prefs.getString( "server_ip", "bonk" );
         String serverWebPort = prefs.getString( "server_web_port", "9000" );
         String serverCliPort = prefs.getString( "server_cli_port", "9090" );

         service = new CliSqueezeService( serverIp, Integer.parseInt( serverCliPort ), Integer.parseInt( serverWebPort ) );
      }

      return service;
   }

   public void setService(SqueezeService service)
   {
      this.service = service;
   }

   public Player getSelectedPlayer()
   {
      return selectedPlayer;
   }

   public void setSelectedPlayer(Player selectedPlayer)
   {
      this.selectedPlayer = selectedPlayer;
   }

   public void resetService()
   {
      if ( service != null && service.isConnected() )
      {
         try
         {
            service.disconnect();
         }
         catch ( Exception e )
         {
            Log.e( LOGTAG, "Error disconnecting from service", e );
         }
      }
      service = null;
      selectedPlayer = null;
   }
}
