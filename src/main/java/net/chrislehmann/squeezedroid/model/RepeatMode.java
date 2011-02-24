package net.chrislehmann.squeezedroid.model;

import java.util.HashMap;
import java.util.Map;


/**
 * Enum representing the possible repeat modes that a {@link Player} can be in
 */
@SuppressWarnings("serial")
public
enum RepeatMode {
   NONE(0), //Don't repeat
   SONG(1), //Repeat the current song
   ALL(2); //Repeat the current playlist
   
   /**
    * Code that the Squeezebox Server CLI Uses to represent this state
    */
   int id;
   
   private RepeatMode( int id )
   {
      this.id = id;
   };

   /**
    * Static {@link Map} that can be used convert from a integer code to the acual {@link RepeatMode} 
    */
   public static Map<String, RepeatMode> intToRepeatModeMap = new HashMap<String, RepeatMode>(){{
      put( "0", RepeatMode.NONE );
      put( "1", RepeatMode.SONG );
      put( "2", RepeatMode.ALL );
   }};

   public int getId()
   {
      return id;
   }
}