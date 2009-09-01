package net.chrislehmann.squeezedroid.transform.typehandlers;

import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Song;

import org.xml.sax.Attributes;

public class SongHandler implements TypeHandler
{

   Song currentSong;
   
   public Item getItem()
   {
      return currentSong;
   }

   public void handleEndTag(String uri, String localName, String qName, String text)
   {
      if( "song_id".equalsIgnoreCase( localName ) )
      {
         currentSong.setId( text );
      } 
      else if ( "title".equalsIgnoreCase( localName ) )
      {
         currentSong.setName( text );
      }
      else if ( "artist".equalsIgnoreCase( localName ) )
      {
         currentSong.setArtist( text );
      }      
      else if ( "artist_id".equalsIgnoreCase( localName ) )
      {
         currentSong.setArtistId( text );
      }      
      else if ( "year".equalsIgnoreCase( localName ) )
      {
         currentSong.setYear( text );
      }  
   }

   public void handleStartTag(String uri, String localName, String qName, Attributes attributes)
   {
   }

   public void reset()
   {
      currentSong = new Song();
   }

}
