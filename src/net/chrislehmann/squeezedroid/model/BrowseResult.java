package net.chrislehmann.squeezedroid.model;

import java.util.List;

public class BrowseResult<T extends Item> {
	private List<T> resutls;
	private int totalItems;
	
	public List<T> getResutls() {
		return resutls;
	}
	public void setResutls(List<T> resutls) {
		this.resutls = resutls;
	}
	public int getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}
}
