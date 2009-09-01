package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.AlbumListAdapter;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.SqueezeService.Sort;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class BrowseAlbumsActivity extends ListActivity {

	private static final int CONTEXTMENU_PLAY_ITEM = 7070;
	private static final int CONTEXTMENU_ADD_ITEM = 7080;

	private Activity _context = this;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getListView().setFastScrollEnabled(true);

		SqueezeService.Sort sort = getSort(getIntent().getData());
		Item parentItem = getParentItem(getIntent().getData());
		setListAdapter(new AlbumListAdapter(ActivityUtils.getService(this), this, parentItem, sort));
		
		getListView().setOnCreateContextMenuListener( new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE, CONTEXTMENU_ADD_ITEM, 0, "Add To Playlist");
				menu.add(Menu.NONE, CONTEXTMENU_PLAY_ITEM, 1, "Play Now");
			}
		});
	};

	private Sort getSort(Uri data) {
		SqueezeService.Sort sort = Sort.TITLE;

		String sortString = data.getQueryParameter("sort");
		if ("new".equals(sortString)) {
			sort = Sort.NEW;
		}

		return sort;
	}

	private Item getParentItem(Uri data) {
		Item item = null;
		if (data.getPathSegments().size() >= 2) {
			String type = data.getPathSegments().get(0);
			String id = data.getPathSegments().get(1);

			if ("artist".equalsIgnoreCase(type)) {
				item = new Artist();
			} else if ("genre".equalsIgnoreCase(type)) {
				// TODO
			}

			item.setId(id);
		}
		return item;
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		SqueezeService service = ActivityUtils.getService(this);
		boolean handled = false;

		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		final Item selectedItem = (Item) getListAdapter().getItem( menuInfo.position );
		
		if( selectedItem != null && service != null )
		{
			switch (item.getItemId()) {
			case CONTEXTMENU_ADD_ITEM:
				service.addItem(ActivityUtils.getSqueezeDroidApplication(this).getSelectedPlayer(), selectedItem);
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(_context, selectedItem.getName() + " added to playlist.", Toast.LENGTH_LONG);
					}
				});
				handled = true;
				break;
			case CONTEXTMENU_PLAY_ITEM:
				service.playItem(ActivityUtils.getSqueezeDroidApplication(this).getSelectedPlayer(), selectedItem);
				runOnUiThread( new Runnable() {
					public void run() {
						Toast.makeText(_context, "Now playing " + selectedItem.getName(), Toast.LENGTH_LONG);
					}
				});
				handled = true;
				break;
			default:
				break;
			}
			
		}
		if( !handled )
		{
			handled = super.onContextItemSelected(item);
		}
		return handled;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = (Item) getListAdapter().getItem(position);
		Intent i = new Intent();
		i.setAction("net.chrislehmann.squeezedroid.action.BrowseSong");
		i.setData(Uri.parse("squeeze:///album/" + item.getId()));
		startActivity(i);
		super.onListItemClick(l, v, position, id);
	}

}