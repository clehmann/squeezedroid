package net.chrislehmann.squeezedroid.model;

import java.util.HashMap;
import java.util.Map;


/**
 * Enum representing the possible Shuffle states a {@link Player} can be put in
 */
@SuppressWarnings("serial")
public
enum ShuffleMode {
   
   NONE(0), //No Shuffle enabled
   SONG(1), //Shuffle by songs
   ALBUM(2); //Shuffle by albums
   
   /**
    * Code that the Squeezebox Server CLI Uses to represent this state
    */
   int id;
   
   private ShuffleMode( int id )
   {
      this.id = id;
   };
   
   /**
    * Static {@link Map} that can be used convert from a integer code to the acual {@link ShuffleMode} 
    */
   public static Map<String, ShuffleMode> intToShuffleModeMap = new HashMap<String, ShuffleMode>(){{
      put( "0", ShuffleMode.NONE );
      put( "1", ShuffleMode.SONG );
      put( "2", ShuffleMode.ALBUM );
   }};

   public int getId()
   {
      return id;
   }
}