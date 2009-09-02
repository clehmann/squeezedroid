package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.PlayListAdapter;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.os.Bundle;
import android.text.Layout;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PlayListActivity extends BaseListActivity {

	private static final int CONTEXTMENU_REMOVE_ITEM = 421;
	private static final int CONTEXTMENU_REMOVE_ALBUM = 422;
	private static final int CONTEXTMENU_REMOVE_ARTIST = 423;
	private static final int CONTEXTMENU_GROUP_REMOVE = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getListView().setOnCreateContextMenuListener(onCreateContextMenu);
		SqueezeService service = getService();
		if (service != null) {
			setListAdapter(new PlayListAdapter(service, this, getSelectedPlayer()));
			PlayerStatus status = service.getPlayerStatus(getSelectedPlayer());
			if( status != null && status.getCurrentIndex() <= getListView().getCount() )
			{
				getListView().setSelection(status.getCurrentIndex());
			}
		}
	}


	OnCreateContextMenuListener onCreateContextMenu = new OnCreateContextMenuListener() {

		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			menu.add(CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ITEM, 0, "Remove song");
			menu.add(CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ALBUM, 1, "Remove artist");
			menu.add(CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ARTIST, 2, "Remove album");
		}
	};
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		SqueezeService service = getService();
		if (service != null) {
			service.jump(getSelectedPlayer(), String.valueOf(position));
		}
		super.onListItemClick(l, v, position, id);
	}

	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean handled = false;
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		Song song = (Song) getListView().getItemAtPosition(menuInfo.position);

		SqueezeService service = getService();
		if (service != null) {

			switch (item.getItemId()) {
			case CONTEXTMENU_REMOVE_ITEM:
				service.removeItem(getSelectedPlayer(), menuInfo.position);
				handled = true;
				break;
			case CONTEXTMENU_REMOVE_ARTIST:
				service.removeAllItemsByArtist(getSelectedPlayer(), song.getArtistId());
				handled = true;
				break;
			case CONTEXTMENU_REMOVE_ALBUM:
				service.removeAllItemsInAlbum(getSelectedPlayer(), song.getAlbumId());
				handled = true;
				break;
			default:
				break;
			}

		}
		if (!handled) {
			handled = super.onContextItemSelected(item);
		}
		return handled;
	}
}