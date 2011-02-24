package net.chrislehmann.squeezedroid.service;

import java.io.Serializable;

import net.chrislehmann.squeezedroid.model.Item;

public class Application extends Item implements Serializable
{

   private static final long serialVersionUID = 1L;
   
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
