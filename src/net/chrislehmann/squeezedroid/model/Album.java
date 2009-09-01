package net.chrislehmann.squeezedroid.model;

public class Album extends Item {
	private String artist;
	private String coverUrl;
	private String coverThumbnailUrl;
	
	public Album() {
		super();
	}

	public Album( String id) {
		super(id);
	}

	public String getCoverThumbnailUrl() {
		return coverThumbnailUrl;
	}

	public void setCoverThumbnailUrl(String coverThumbnailUrl) {
		this.coverThumbnailUrl = coverThumbnailUrl;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

}
