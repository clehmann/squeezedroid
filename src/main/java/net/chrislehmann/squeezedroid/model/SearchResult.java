package net.chrislehmann.squeezedroid.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results from a search of the queezecenter database
 * @author lehmanc
 *
 */
public class SearchResult
{
   /**
    * List of {@link Artist}s matching the criteria
    */
   private List<Artist> artists = new ArrayList<Artist>();

   /**
    * List of {@link Album}s matching the criteria
    */
   private List<Album> albums = new ArrayList<Album>();

   /**
    * List of {@link Songs}s matching the criteria
    */
   private List<Song> songs = new ArrayList<Song>();

   /**
    * List of {@link Genre}s matching the criteria
    */
   private List<Genre> genres = new ArrayList<Genre>();


   /**
    * Total number of results
    */
   private int totalResults = 0;
   
   /**
    * Total number of artists
    */
   private int totalArtists = 0;

   /**
    * Total number of albums
    */
   private int totalAlbums = 0;
   
   /**
    * Total number of total number of songs
    */
   private int totalSongs = 0;

   /**
    * Total number of generes
    */
   private int totalGenres = 0;

   public List<Artist> getArtists()
   {
      return artists;
   }

   public void setArtists(List<Artist> artists)
   {
      this.artists = artists;
   }

   public List<Album> getAlbums()
   {
      return albums;
   }

   public void setAlbums(List<Album> albums)
   {
      this.albums = albums;
   }

   public List<Song> getSongs()
   {
      return songs;
   }

   public void setSongs(List<Song> songs)
   {
      this.songs = songs;
   }

   public List<Genre> getGenres()
   {
      return genres;
   }

   public void setGenres(List<Genre> genres)
   {
      this.genres = genres;
   }

   public int getTotalResults()
   {
      return totalResults;
   }

   public void setTotalResults(int totalResults)
   {
      this.totalResults = totalResults;
   }

   public int getTotalArtists()
   {
      return totalArtists;
   }

   public void setTotalArtists(int totalArtists)
   {
      this.totalArtists = totalArtists;
   }

   public int getTotalAlbums()
   {
      return totalAlbums;
   }

   public void setTotalAlbums(int totalAlbums)
   {
      this.totalAlbums = totalAlbums;
   }

   public int getTotalSongs()
   {
      return totalSongs;
   }

   public void setTotalSongs(int totalSongs)
   {
      this.totalSongs = totalSongs;
   }

   public int getTotalGenres()
   {
      return totalGenres;
   }

   public void setTotalGenres(int totalGenres)
   {
      this.totalGenres = totalGenres;
   }

}
