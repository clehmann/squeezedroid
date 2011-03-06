package net.chrislehmann.squeezedroid.listadapter;

import android.app.Activity;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;

import java.util.List;

public class ArtistListAdapter extends PagableAdapter{
	protected SqueezeService _service;
	private Item _parentItem;
	private Integer _numItems = 1;

	public ArtistListAdapter(SqueezeService service, Activity parent) {
		super(parent);
		_service = service;
	}

	public ArtistListAdapter(SqueezeService service, Activity parent, Item parentItem) {
		super(parent);
		_service = service;
		_parentItem = parentItem;
	}

	@Override
	public int getCount() {
		return _numItems;
	}

	protected List<? extends Object> createPage(int start, int pageSize) {
		BrowseResult<Artist> result = _service.browseArtists(_parentItem, start, pageSize);
		_numItems = result.getTotalItems();
		return result.getResutls();
	}

}
