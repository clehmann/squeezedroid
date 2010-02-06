package net.chrislehmann.squeezedroid.model;

import java.io.Serializable;

public class ApplicationItem extends Item implements Serializable
{
   private static final long serialVersionUID = 1L;
   private boolean hasItems;
   private boolean playable;
   
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
}
