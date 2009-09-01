package net.chrislehmann.squeezedroid.transform.typehandlers;

import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Song;

import org.xml.sax.Attributes;

public class AlbumHandler implements TypeHandler
{

   Album currentAlbum;
   
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
      currentAlbum = new Album();
   }

}
