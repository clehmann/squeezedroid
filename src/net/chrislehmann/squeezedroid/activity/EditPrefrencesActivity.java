package net.chrislehmann.squeezedroid.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.chrislehmann.squeezedroid.R;
import android.app.Activity;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

public class EditPrefrencesActivity extends PreferenceActivity
{
   
   private static class ValidatingOnPreferenceChangeListener implements OnPreferenceChangeListener
   {
      private Pattern _pattern;
      private String _errorMessage;
      private Activity _parent;
      
      public ValidatingOnPreferenceChangeListener( Pattern matchingPattern, Activity parent, String errorMessage )
      {
         _pattern = matchingPattern;
         _errorMessage = errorMessage;
         _parent = parent;
      }
      
      public boolean onPreferenceChange(Preference preference, Object newValue)
      {
         boolean matches = true;
         Matcher m = _pattern.matcher( newValue.toString() );
         if( !m.matches() )
         {
            matches = false;
            _parent.runOnUiThread( new Thread()
            {
               public void run(){ Toast.makeText( _parent, _errorMessage, Toast.LENGTH_SHORT );}
            } );
         }
         
         return matches;
      }
   }
   private Pattern numericPattern = Pattern.compile( "[0-9]+" );
   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );
      addPreferencesFromResource( R.xml.preferences );
      
      EditTextPreference preference = (EditTextPreference) findPreference( "server_web_port" );
      preference.setOnPreferenceChangeListener( new ValidatingOnPreferenceChangeListener( numericPattern, this,  "'Web Port' must be numeric" ) );

      preference = (EditTextPreference) findPreference( "server_cli_port" );
      preference.setOnPreferenceChangeListener( new ValidatingOnPreferenceChangeListener( numericPattern, this,  "'Cli Port' must be numeric" ) );
   }
}
