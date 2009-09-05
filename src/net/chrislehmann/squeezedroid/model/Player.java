package net.chrislehmann.squeezedroid.model;

/**
 * @author lehmanc
 *
 */
public class Player
{
   
   private String id;
   private String name;

   public Player()
   {
      super();
   }

   public Player(String id)
   {
      super();
      this.id = id;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

}
