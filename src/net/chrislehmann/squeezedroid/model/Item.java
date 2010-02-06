package net.chrislehmann.squeezedroid.model;

import java.io.Serializable;

public class Item implements Serializable {
   private static final long serialVersionUID = 1L;
   protected String name;
	protected String id;
	protected String imageUrl;
	protected String imageThumbnailUrl;
	
	public Item() {
		super();
	}

	public Item(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageThumbnailUrl() {
		return imageThumbnailUrl;
	}

	public void setImageThumbnailUrl(String imageThumbnailUrl) {
		this.imageThumbnailUrl = imageThumbnailUrl;
	}

}
