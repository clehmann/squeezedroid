package net.chrislehmann.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerializationUtils
{
   public static <T> List<T> unserializeList(Pattern pattern, String input, Unserializer<T> unserializer)
   {
      List<T> items = new ArrayList<T>();

      Matcher matcher = pattern.matcher( input );

      while ( matcher.find() )
      {
         items.add( unserializer.unserialize( matcher ) );
      }

      return items;
   }

   public static <T> T unserialize(Pattern pattern, String input, Unserializer<T> unserializer)
   {
      T object = null;
      Matcher matcher = pattern.matcher( input );
      if ( matcher.find() )
      {
         object = unserializer.unserialize( matcher );
      }

      return object;
   }


   /**
    * Lifted from http://www.w3.org/International/O-URL-code.html
    * @param s String to decode
    * @return a decoded string
    */
   public static String decode(String s)
   {
      StringBuffer sbuf = new StringBuffer();
      if ( s != null )
      {

         int l = s.length();
         int ch = -1;
         int b, sumb = 0;
         for ( int i = 0, more = -1; i < l; i++ )
         {
            /* Get next byte b from URL segment s */
            switch ( ch = s.charAt( i ) )
            {
               case '%' :
                  ch = s.charAt( ++i );
                  int hb = (Character.isDigit( (char) ch ) ? ch - '0' : 10 + Character.toLowerCase( (char) ch ) - 'a') & 0xF;
                  ch = s.charAt( ++i );
                  int lb = (Character.isDigit( (char) ch ) ? ch - '0' : 10 + Character.toLowerCase( (char) ch ) - 'a') & 0xF;
                  b = (hb << 4) | lb;
                  break;
               case '+' :
                  b = ' ';
                  break;
               default :
                  b = ch;
            }
            /* Decode byte b as UTF-8, sumb collects incomplete chars */
            if ( (b & 0xc0) == 0x80 )
            { // 10xxxxxx (continuation byte)
               sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to sumb
               if ( --more == 0 )
                  sbuf.append( (char) sumb ); // Add char to sbuf
            }
            else if ( (b & 0x80) == 0x00 )
            { // 0xxxxxxx (yields 7 bits)
               sbuf.append( (char) b ); // Store in sbuf
            }
            else if ( (b & 0xe0) == 0xc0 )
            { // 110xxxxx (yields 5 bits)
               sumb = b & 0x1f;
               more = 1; // Expect 1 more byte
            }
            else if ( (b & 0xf0) == 0xe0 )
            { // 1110xxxx (yields 4 bits)
               sumb = b & 0x0f;
               more = 2; // Expect 2 more bytes
            }
            else if ( (b & 0xf8) == 0xf0 )
            { // 11110xxx (yields 3 bits)
               sumb = b & 0x07;
               more = 3; // Expect 3 more bytes
            }
            else if ( (b & 0xfc) == 0xf8 )
            { // 111110xx (yields 2 bits)
               sumb = b & 0x03;
               more = 4; // Expect 4 more bytes
            }
            else
            /*if ((b & 0xfe) == 0xfc)*/{ // 1111110x (yields 1 bit)
               sumb = b & 0x01;
               more = 5; // Expect 5 more bytes
            }
            /* We don't test if the UTF-8 encoding is well-formed */
         }
      }

      return sbuf.toString();
   }

   public interface Unserializer<T>
   {
      T unserialize(Matcher matcher);
   }
}
