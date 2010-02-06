package net.chrislehmann.squeezedroid.service;

import net.chrislehmann.squeezedroid.model.Item;

public class Application extends Item
{

   /**
    * The 'cmd' string for the app.  This is what is used in the CLI to 
    * specify commands for that app
    */
   private String cmd;

   public String getCmd()
   {
      return cmd;
   }

   public void setCmd(String cmd)
   {
      this.cmd = cmd;
   }
   
}
