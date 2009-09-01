package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.model.Player;
import android.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlayerListAdapter extends ArrayAdapter<Player> {

	public PlayerListAdapter(Context context, List<Player> objects) {
		super(context, R.layout.simple_list_item_1, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView v = new TextView(getContext());
		Player player = (Player) getItem(position);
		v.setText(player.getName());
		v.setPadding(10, 10, 10, 10);
		v.setTextSize(20);
		return v;
	}
}
