package net.chrislehmann.squeezedroid.model;

import java.io.Serializable;

import net.chrislehmann.squeezedroid.service.Application;

/**
 * Represents an entry in an app menu.  This might be playable or just a 'container'
 * type of object.  Use the {@link ApplicationMenuItem#hasItems} and {@link ApplicationMenuItem#playable}
 * fields to determine if it is.
 */
public class ApplicationMenuItem extends Item implements Serializable
{
   private static final long serialVersionUID = 1L;
   private boolean hasItems;
   private boolean playable;
   private Application application;
   private String type;
   
   public boolean isHasItems()
   {
      return hasItems;
   }
   public void setHasItems(boolean hasItems)
   {
      this.hasItems = hasItems;
   }
   public boolean isPlayable()
   {
      return playable;
   }
   public void setPlayable(boolean playable)
   {
      this.playable = playable;
   }
   public Application getApplication()
   {
      return application;
   }
   public void setApplication(Application application)
   {
      this.application = application;
   }
   public String getType()
   {
      return type;
   }
   public void setType(String type)
   {
      this.type = type;
   }
}
