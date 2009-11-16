package net.chrislehmann.squeezedroid.activity;

import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Base Activity to provide some simple helper methods to simplify the starting of subactivities.  Rather than overriding
 * {@link Activity#onActivityResult}, you can simply call {@link #launchSubActivity(Class, IntentResultCallback)} with a 
 * {@link IntentResultCallback} to be executed when the child activity returns.
 * 
 * Adapted from http://developerlife.com/tutorials/?p=302 to work on cupcake
 */
public class ActivitySupport extends Activity
{

   private static final String LOGTAG = "ActivitySupport";
   protected static final int RESULT_CLOSE_APPLICATION_CHAIN = 666;

   protected boolean closing = false;

   /**
    * Interface containing callbacks that will be executed when an {@link Activity} result is returned
    * @author lehmanc
    */
   public static interface IntentResultCallback
   {
      /**
       * Will be called then {@link Activity#RESULT_OK} is returned
       */
      public void resultOk(String resultString, Bundle resultMap);

      /**
       * Will be called then {@link Activity#RESULT_CANCELED} is returned
       */
      public void resultCancel(String resultString, Bundle resultMap);
   }
   
   protected HashMap<Integer, IntentResultCallback> _callbackMap = new HashMap<Integer, IntentResultCallback>();

   /**
    * Launch a Activity with {@link Class} subActivityClass, and execute the {@link IntentResultCallback#resultOk(String, Bundle)} or 
    * {@link IntentResultCallback#resultCancel(String, Bundle)(String, Bundle)} method depending on the result
    * @param subActivityClass The {@link Class} of the {@link Activity} to start
    * @param callback the {@link IntentResultCallback} to execute when the activity is finished
    */
   public void launchSubActivity(Class<? extends Activity> subActivityClass, IntentResultCallback callback )
   {
      Intent i = new Intent( this, subActivityClass );
      launchSubActivity( i, callback );
   }
   
   /**
    * Launch a Activity {@link Intent} intent, and execute the {@link IntentResultCallback#resultOk(String, Bundle)} or 
    * {@link IntentResultCallback#resultCancel(String, Bundle)(String, Bundle)} method depending on the result
    * @param subActivityClass The {@link Class} of the {@link Activity} to start
    * @param callback the {@link IntentResultCallback} to execute when the activity is finished
    */
   public void launchSubActivity(Intent i, IntentResultCallback callback )
   {
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
    * Sets the result
    */
   public void closeApplication()
   {
      closing = true;
      setResult( RESULT_CLOSE_APPLICATION_CHAIN );
      finish();
   }

   /**
    * this is the underlying implementation of the onActivityResult method that handles auto generation of
    * correlationIds and adding/removing callback functors to handle the result
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      
      //If we get the RESULT_CLOSE_APPLICATION_CHAIN, set our result to that and finish.
      //  This should bubble all the way up to the top of the application activity stack
      if( resultCode == RESULT_CLOSE_APPLICATION_CHAIN )
      {
         setResult( RESULT_CLOSE_APPLICATION_CHAIN );
         finish();
         return;
      }
      
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

         if( callback != null )
         {
            switch ( resultCode )
            {
               case Activity.RESULT_CANCELED :
                  callback.resultCancel( result, extras );
                  _callbackMap.remove( requestCode );
                  break;
               case Activity.RESULT_OK :
                  callback.resultOk( result, extras );
                  _callbackMap.remove( requestCode );
                  break;
               default :
                  Log.e( LOGTAG, "Couldn't find callback handler for correlationId " + requestCode + " and result code " + resultCode);
            }
         }
         else
         {
            Log.e( LOGTAG, "No callback found for correlationId: " + requestCode );
         }
      }
      catch ( Exception e )
      {
         Log.e( LOGTAG, "Problem processing result from sub-activity", e );
      }

   }
}