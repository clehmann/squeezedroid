package net.chrislehmann.squeezedroid.activity;



public class SqueezedroidActivitySupport extends ActivitySupport
{
   protected SqueezeDroidApplication getSqueezeDroidApplication()
   {
      return (SqueezeDroidApplication) getApplication();
   }
}
