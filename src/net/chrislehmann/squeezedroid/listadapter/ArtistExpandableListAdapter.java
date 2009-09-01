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

//	public int getPositionForSection(int section) {
//		String letter = _sections[section];
//		if( letter == "#")
//		{
//			return 0;
//		}
//		for (int i = 0; i < _groupsListAdapter.getCount(); i++) {
//			Item item = (Item) _groupsListAdapter.getItem(i);
//			if (item != null && item.getName() != null && item.getName().length() > 0) {
//				if (item.getName().substring(0, 1).compareTo(letter) >= 0 ) {
//					return i;
//				}
//			}
//		}
//		return _groupsListAdapter.getCount() - 1;
//	}
//
//	public int getSectionForPosition(int position) {
//
//		Item item = (Item) _groupsListAdapter.getItem(position);
//		int sectionPosition = _sections.length - 1;
//		if (item != null && item.getName() != null && item.getName().length() > 0) {
//
//			sectionPosition = Arrays.binarySearch(_sections, item.getName().charAt(0));
//		}
//		return sectionPosition;
//	}
//
//	private String[] _sections = new String[] { "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
//
//	public Object[] getSections() {
//		return _sections;
//	}

}
