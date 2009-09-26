package net.chrislehmann.squeezedroid.model;

public class Song extends Item {
	private String artist;
	private String artistId;
	private String album;
	private String albumId;
	private int durationInSeconds;
	private boolean isRadioStation;
	

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	private String year;

	public String getArtistId() {
		return artistId;
	}

	public void setArtistId(String artistId) {
		this.artistId = artistId;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

   public int getDurationInSeconds()
   {
      return durationInSeconds;
   }

   public void setDurationInSeconds(int durationInSeconds)
   {
      this.durationInSeconds = durationInSeconds;
   }

   public boolean isRadioStation()
   {
      return isRadioStation;
   }

   public void setRadioStation(boolean isRadioStation)
   {
      this.isRadioStation = isRadioStation;
   }
}
