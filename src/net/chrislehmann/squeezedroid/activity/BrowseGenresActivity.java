package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.listadapter.GenreListAdapter;
import net.chrislehmann.squeezedroid.model.Item;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class BrowseGenresActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter(new GenreListAdapter(((SqueezeDroidApplication) getApplication()).getService(), this, null));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = (Item) getListAdapter().getItem(position);
		Intent i = new Intent();
		i.setAction("net.chrislehmann.squeezedroid.action.BrowseArtist");
		i.setData(Uri.parse("squeeze:///genre/" + item.getId()));
		startActivity(i);

		super.onListItemClick(l, v, position, id);
	}

}