package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Genre;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;

public class GenreListAdapter extends PagableAdapter {
	protected SqueezeService _service;
	private Item _parentItem;
	private Integer _numItems = 1;

	public GenreListAdapter(SqueezeService service, Activity parent, Item parentItem) {
		super(parent);
		_service = service;
		_parentItem = parentItem;
	}

	@Override
	public int getCount() {
		return _numItems;
	}

	protected List<? extends Object> createPage(int start, int pageSize) {
		BrowseResult<Genre> browseResult = _service.browseGenres(_parentItem, start, pageSize);
		_numItems = browseResult.getTotalItems();
		return browseResult.getResutls();
	}
}
