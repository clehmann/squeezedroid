package net.chrislehmann.squeezedroid.activity;

import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * ActivitySupport - Adapted from http://developerlife.com/tutorials/?p=302 to work on cupcake
 */
public class ActivitySupport extends Activity
{

   private static final String LOGTAG = "ActivitySupport";
   protected HashMap<Integer, IntentResultCallback> _callbackMap = new HashMap<Integer, IntentResultCallback>();


   /** use this method to launch the sub-Activity, and provide a functor to handle the result - ok or cancel */
   protected void launchSubActivity(Class subActivityClass, IntentResultCallback callback )
   {
      launchSubActivity( subActivityClass, callback, false );
   }

   /** use this method to launch the sub-Activity, and provide a functor to handle the result - ok or cancel */
   protected void launchSubActivity(Class subActivityClass, IntentResultCallback callback, boolean startNewTask )
   {
      Intent i = new Intent( this, subActivityClass );
      if( startNewTask )
      {
         i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK  );
      }

      Random rand = new Random();
      int correlationId = Math.abs( rand.nextInt() );

      if( callback != null )
      {
         _callbackMap.put( correlationId, callback );
         startActivityForResult( i, correlationId  );
      }
      else
      {
         startActivity( i );
      }
   }
   

   /**
    * this is the underlying implementation of the onActivityResult method that handles auto generation of
    * correlationIds and adding/removing callback functors to handle the result
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      Bundle extras = null;
      String result = null;
      if( data != null )
      {
         extras = data.getExtras();
         result = data.getDataString();
      }
      try
      {
         IntentResultCallback callback = _callbackMap.get( requestCode );

         switch ( resultCode )
         {
            case Activity.RESULT_CANCELED :
               callback.resultCancel( result, extras );
               _callbackMap.remove( requestCode );
               break;
            case Activity.RESULT_OK :
               callback.resultOk( data.getDataString(), data.getExtras() );
               _callbackMap.remove( requestCode );
               break;
            default :
               Log.e( LOGTAG, "Couldn't find callback handler for correlationId" );
         }
      }
      catch ( Exception e )
      {
         Log.e( LOGTAG, "Problem processing result from sub-activity", e );
      }

   }

   public static interface IntentResultCallback
   {

      public void resultOk(String resultString, Bundle resultMap);

      public void resultCancel(String resultString, Bundle resultMap);

   }
}