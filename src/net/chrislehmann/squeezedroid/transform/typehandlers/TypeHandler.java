package net.chrislehmann.squeezedroid.transform.typehandlers;

import net.chrislehmann.squeezedroid.model.Item;

import org.xml.sax.Attributes;

public interface TypeHandler
{
   public void handleStartTag(String uri, String localName, String qName, Attributes attributes);
   public void handleEndTag(String uri, String localName, String qName, String lastText);
   public Item getItem();
   public void reset();
}


