package net.chrislehmann.squeezedroid.activity;

import android.app.Application;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager;

public class SqueezeDroidApplication extends Application
{

   //private static final String LOGTAG = "SqueezeDroidApplication";
   private ServiceConnectionManager connectionManager = new ServiceConnectionManager();
   private Player selectedPlayer;

   
   @Override
   public void onTerminate()
   {
      connectionManager.disconnect();
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
