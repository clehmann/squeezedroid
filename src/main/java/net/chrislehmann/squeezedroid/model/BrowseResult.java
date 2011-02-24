package net.chrislehmann.squeezedroid.model;

import java.util.ArrayList;
import java.util.List;

public class BrowseResult<T extends Item> {
	private List<T> resutls = new ArrayList<T>();
	private int totalItems = 0;
	
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
