package net.chrislehmann.squeezedroid.activity;


public class ActivityUtils
{
//   /**
//    * Gets the SqueezeService and makes sure it is still connected.  If not connected, this fill start the {@link ConnectToServerActivity} 
//    * and return null.  Your code should take this into account.
//    * @return
//    */
//   public static SqueezeService getService(Activity context, boolean connect)
//   {
//      SqueezeService service = getSqueezeDroidApplication(context).getService();
//      if( connect && ( service == null || !service.isConnected() ) )
//      {
//         service = null;
//         Intent intent = new Intent();
//         intent.setAction( SqueezeDroidConstants.Actions.ACTION_CONNECT );
//         context.startActivityForResult( intent, SqueezeDroidConstants.RequestCodes.REQUEST_CONNECT );
//      }
//      return service;
//   }
//   
//   public static SqueezeService getService(Activity context)
//   {
//	   return getService(context, true);
//   }
//
//   /**
//    * Helper method to simply get the application and cast it to a {@link SqueezeDroidApplication}
//    * @param context
//    * @return
//    */
//   public static SqueezeDroidApplication getSqueezeDroidApplication(Activity context)
//   {
//      return (SqueezeDroidApplication) context.getApplication();
//   }
   
}
