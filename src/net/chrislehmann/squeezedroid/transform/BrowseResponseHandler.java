package net.chrislehmann.squeezedroid.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.transform.typehandlers.AlbumHandler;
import net.chrislehmann.squeezedroid.transform.typehandlers.ArtistHandler;
import net.chrislehmann.squeezedroid.transform.typehandlers.SongHandler;
import net.chrislehmann.squeezedroid.transform.typehandlers.TypeHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

@SuppressWarnings("serial")
public class BrowseResponseHandler extends DefaultHandler
{
   private List<Item> items = new ArrayList<Item>();
   private TypeHandler currentTypeHandler;
   private String lastText;
   
   private Map<String, TypeHandler> handlerMap = new HashMap<String, TypeHandler>()
   {{
      put( "song", new SongHandler() );
      put( "album", new AlbumHandler() );
      put( "contributor", new ArtistHandler() );
   }};
   
   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
   {
      Log.d( "PARSE", "<" + localName + ">" );
      if( currentTypeHandler != null )
      {
         currentTypeHandler.handleStartTag(uri, localName, qName, attributes);
      }
      lastText = "";
      super.startElement( uri, localName, qName, attributes );
   }
   
   @Override
   public void characters(char[] ch, int start, int length) throws SAXException
   {
      lastText += (new String(ch).substring(start, start + length));
      super.characters( ch, start, length );
   }
   
   
   private TypeHandler getTypeHandler(String localName)
   {
      return handlerMap.get( localName );
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException
   {
      
      
      Log.d("PARSE", lastText );
      Log.d("PARSE", "</" + localName + ">");
      if( currentTypeHandler != null )
      {
         currentTypeHandler.handleEndTag( uri, localName, qName, lastText );
         if( "browse_entry".equalsIgnoreCase( localName ) )
         {
            Item item = currentTypeHandler.getItem();
            if( item != null )
            {
               items.add( item );
            }
         }
      }

      if( "entry_type".equalsIgnoreCase( localName ) )
      {
         currentTypeHandler = getTypeHandler( lastText );
         if( currentTypeHandler != null )
         {
            currentTypeHandler.reset();
         }
      }

      super.endElement( uri, localName, qName );
      
   }

   public List<Item> getItems()
   {
      return items;
   }

   public void setItems(List<Item> items)
   {
      this.items = items;
   }
}
