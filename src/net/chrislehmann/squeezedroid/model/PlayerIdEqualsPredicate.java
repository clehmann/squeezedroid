package net.chrislehmann.squeezedroid.model;


import org.apache.commons.collections.Predicate;

public class PlayerIdEqualsPredicate implements Predicate
{
   private String playerId;

   public PlayerIdEqualsPredicate(String playerId)
   {
      this.playerId = playerId;
   }

   public boolean evaluate(Object arg0)
   {
      boolean matches = false;
      if ( arg0 instanceof Player )
      {
         Player rhs = (Player) arg0;
         matches = playerId.equals( rhs.getId() );
      }
      return matches;
   }
}