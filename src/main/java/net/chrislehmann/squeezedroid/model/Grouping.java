package net.chrislehmann.squeezedroid.model;

public enum Grouping
{
   ARTIST("contributor"), 
   ARTIST_ALBUM("contributor%2Calbum"), 
   ARTIST_ALBUM_TRACK("contributor%2Calbum%2Ctrack"), 
   GENRE_ARTIST_ALBUM_TRACK("genre%2Ccontributor%2Calbum%2Ctrack"), 
   ALBUM_TRACK("album%2Ctrack"), 
   YEAR_ALBUM_TRACK("year%2Calbum%2Ctrack"), 
   TRACK("track"), 
   ARTIST_TRACK("contributor%2Ctrack");
   
   private String urlValue;
   
   private Grouping( String urlValue )
   {
      this.urlValue = urlValue;
   }

   public String getUrlValue()
   {
      return urlValue;
   }

}