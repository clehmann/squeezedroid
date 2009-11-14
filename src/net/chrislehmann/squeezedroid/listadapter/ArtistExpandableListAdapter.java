package net.chrislehmann.squeezedroid.listadapter;

import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.SqueezeService.Sort;
import android.app.Activity;
import android.widget.ListAdapter;

public class ArtistExpandableListAdapter extends BaseCompositeExpandableListAdapter {

	private SqueezeService _service;

	public ArtistExpandableListAdapter(SqueezeService service, Activity parent, Item parentItem) {
		super(parent, new ArtistListAdapter(service, parent, parentItem));
		_service = service;

	}

	protected ListAdapter createListAdapter(Item parentItem) {
		return new AlbumListAdapter(_service, _parent, parentItem, Sort.TITLE);
	}

}
