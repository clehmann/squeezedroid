package net.chrislehmann.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class SerializationUtils
{
   public static <T> List<T> unserializeList( Pattern pattern, String input, Unserializer<T> unserializer )
   {
      List<T> items = new ArrayList<T>();
      
      Matcher matcher = pattern.matcher( input );

      while( matcher.find() )
      {
         items.add( unserializer.unserialize( matcher ) );
      }
      
      return items;
   }

   public static <T> T unserialize( Pattern pattern, String input, Unserializer<T> unserializer )
   {
      T object = null;
      Matcher matcher = pattern.matcher( input );
      if( matcher.find() )
      {
         object = unserializer.unserialize( matcher );
      }
      
      return object;
   }
   

   public static String decode(String string)
   {
      
      String decodedString = null;
      if( string != null  )
      {
         decodedString = string.replace( "%20", " " );
         decodedString = decodedString.replace( "%2C", "," );
         decodedString = decodedString.replace( "%26", "&" );
         decodedString = decodedString.replace( "%3A", ":" );
         decodedString = decodedString.replace( "%24", "$" );
         decodedString = decodedString.replace( "%26", "&" );
         decodedString = decodedString.replace( "%2B", "+" );
         decodedString = decodedString.replace( "%2F", "/" );
         decodedString = decodedString.replace( "%3A", ":" );
         decodedString = decodedString.replace( "%3B", ";" );
         decodedString = decodedString.replace( "%3F", "?" );
         decodedString = decodedString.replace( "%40", "@" );
      }
      return decodedString;
   }

   public interface Unserializer<T>
   {
      T unserialize( Matcher matcher );
   }
}
