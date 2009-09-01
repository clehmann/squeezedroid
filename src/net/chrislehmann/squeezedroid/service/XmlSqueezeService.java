package net.chrislehmann.squeezedroid.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.Grouping;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.transform.BrowseResponseHandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class XmlSqueezeService
{
   private String host = "192.168.3.108";
   private String protocol = "http";
   private String port = "9001";

   private HttpClient client = new DefaultHttpClient();
   
   private HttpResponse makeRequest( String uri, HashMap<String, String> params )
   {
      if( uri.startsWith( "/" ) == false )
      {
         uri = "/" + uri;  
      }
      
      HttpResponse response;
      HttpGet request = new HttpGet();
      try
      {
         String url = protocol + "://" + host + ":" + port + uri;
         if( params != null && params.size() > 0 )
         {
            url += "?";
            for ( Entry<String, String> entry : params.entrySet() )
            {
               url += entry.getKey() + "=" + entry.getValue() + "&";
            }
         }
         request.setURI(new URI( url ));
         response = client.execute(request);
      }
      catch ( Exception e )
      {
         throw new RuntimeException("Cannot make http request", e);
      }
      
      return response;
   }
   
   private List<? extends Item> parseResponse( HttpResponse response )
   {
      BrowseResponseHandler handler = new BrowseResponseHandler();
      try
      {
       SAXParserFactory spf = SAXParserFactory.newInstance();
       SAXParser sp = spf.newSAXParser();
       XMLReader xr = sp.getXMLReader();
       xr.setContentHandler(handler);
       xr.parse(new InputSource( response.getEntity().getContent() ));
      }
      catch ( Exception e )
      {
         throw new RuntimeException( "Error unmarshalling xml.", e );
      }
      return handler.getItems();
   }

//      http://music.chrislehmann.net/xml/browsedb.html?hierarchy=album,track&level=0&player=01%3A00%3A00%3A00%3A00%3A01
   
   
   
   /* (non-Javadoc)
    * @see net.chrislehmann.squeezedroid.service.SqueezeService#browseArtists(net.chrislehmann.squeezedroid.model.Item, int, int)
    */
   @SuppressWarnings("unchecked")
   public List<Artist> browseArtists( Item parent,  int start, int numberOfItems )
   {
      HashMap<String, String> params = new HashMap<String, String>();
      params.put( "start", String.valueOf( start ) );
      params.put( "numberOfItems", String.valueOf( numberOfItems ));

      if( parent == null )
      {
         params.put( "hierarchy", Grouping.ARTIST_ALBUM_TRACK.getUrlValue() );
         params.put( "level", "0" );
      }
      
      HttpResponse response = makeRequest( "/xml/browsedb.xml", params );
      return (List<Artist>) parseResponse( response );
   }

   /* (non-Javadoc)
    * @see net.chrislehmann.squeezedroid.service.SqueezeService#browseAlbums(net.chrislehmann.squeezedroid.model.Item, int, int)
    */
   @SuppressWarnings("unchecked")
   public List<Album> browseAlbums( Item parent, int start, int numberOfItems )
   {
      HashMap<String, String> params = new HashMap<String, String>();
      params.put( "start", String.valueOf( start ) );
      params.put( "numberOfItems", String.valueOf( numberOfItems ) );

      if( parent == null )
      {
         params.put( "hierarchy", Grouping.TRACK.getUrlValue() );
         params.put( "level", "0" );
      }
      else if( parent instanceof Artist )
      {
         params.put( "hierarchy", Grouping.ARTIST_ALBUM_TRACK.getUrlValue() );
         params.put( "level", "1" );
         params.put( "contributor.id", parent.getId() );
      }
      
      HttpResponse response = makeRequest( "/xml/browsedb.xml", params );
      return (List<Album>) parseResponse( response );
   }

   
   /* (non-Javadoc)
    * @see net.chrislehmann.squeezedroid.service.SqueezeService#browseSongs(net.chrislehmann.squeezedroid.model.Item, int, int)
    */
   @SuppressWarnings("unchecked")
   public List<Song> browseSongs( Item parent,  int start, int numberOfItems )
   {
      HashMap<String, String> params = new HashMap<String, String>();
      params.put( "start", String.valueOf( start ) );
      params.put( "numberOfItems", String.valueOf( numberOfItems ));

      if( parent == null )
      {
         params.put( "hierarchy", Grouping.TRACK.getUrlValue() );
         params.put( "level", "0" );
      }
      else if( parent instanceof Artist )
      {
         params.put( "hierarchy", Grouping.ARTIST_ALBUM_TRACK.getUrlValue() );
         params.put( "level", "2" );
         params.put( "contributor.id", parent.getId() );
         
      }
      else if( parent instanceof Album )
      {
         params.put( "hierarchy", Grouping.ALBUM_TRACK.getUrlValue() );
         params.put( "level", "1" );
         params.put( "album.id", parent.getId() );
         
      }
      
      HttpResponse response = makeRequest( "/xml/browsedb.xml", params );
      return (List<Song>) parseResponse( response );
   }

   public String getHost()
   {
      return host;
   }

   public void setHost(String host)
   {
      this.host = host;
   }

   public String getProtocol()
   {
      return protocol;
   }

   public void setProtocol(String protocol)
   {
      this.protocol = protocol;
   }

   public String getPort()
   {
      return port;
   }

   public void setPort(String port)
   {
      this.port = port;
   }

   public void connect()
   {
      // TODO Auto-generated method stub
      
   }

   public void disconnect()
   {
      // TODO Auto-generated method stub
      
   }

   public List<Player> getPlayers()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<Song> getCurrentPlaylist(Player player, Integer start, Integer numberOfItems)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public PlayerStatus getPlayerStatus(Player player, Integer start, Integer numberOfItems)
   {
      // TODO Auto-generated method stub
      return null;
   }
}