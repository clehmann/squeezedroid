package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager;
import net.chrislehmann.util.ImageLoader;
import android.app.Application;

public class SqueezeDroidApplication extends Application
{

   //private static final String LOGTAG = "SqueezeDroidApplication";
   private ServiceConnectionManager connectionManager = new ServiceConnectionManager();
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
      connectionManager.disconnect();
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
      connectionManager.disconnect();
      selectedPlayer = null;
   }

   public ServiceConnectionManager getConnectionManager()
   {
      return connectionManager;
   }

   public void setConnectionManager(ServiceConnectionManager connectionManager)
   {
      this.connectionManager = connectionManager;
   }
   
}
