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

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

}
