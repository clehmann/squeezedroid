package net.chrislehmann.squeezedroid.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import net.chrislehmann.squeezedroid.R;

import java.util.regex.Pattern;

public class EditPrefrencesActivity extends PreferenceActivity
{

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );
      addPreferencesFromResource( R.xml.preferences );
   }
}
