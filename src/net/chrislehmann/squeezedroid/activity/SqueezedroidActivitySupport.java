package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;
import android.content.Intent;


public class SqueezedroidActivitySupport extends Activity
{
   protected SqueezeDroidApplication getSqueezeDroidApplication()
   {
      return (SqueezeDroidApplication) getApplication();
   }
}
