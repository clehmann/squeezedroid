package net.chrislehmann.squeezedroid.transform.typehandlers;

import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.Item;

import org.xml.sax.Attributes;

public class ArtistHandler implements TypeHandler
{

   Artist currentAlbum;
   
   public Item getItem()
   {
      return currentAlbum;
   }

   public void handleEndTag(String uri, String localName, String qName, String text)
   {
      if( "entry_id".equalsIgnoreCase( localName ) )
      {
         currentAlbum.setId( text );
      } 
      else if ( "entry_name".equalsIgnoreCase( localName ) )
      {
         currentAlbum.setName( text );
      }
   }

   public void handleStartTag(String uri, String localName, String qName, Attributes attributes)
   {
   }

   public void reset()
   {
      currentAlbum = new Artist();
   }

}
