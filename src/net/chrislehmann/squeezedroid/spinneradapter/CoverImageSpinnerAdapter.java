package net.chrislehmann.squeezedroid.spinneradapter;

import java.util.List;

import net.chrislehmann.squeezedroid.listadapter.PagableAdapter;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.util.ImageLoader;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;

public class CoverImageSpinnerAdapter extends PagableAdapter implements SpinnerAdapter{


	private Player _player;
	private SqueezeService _service;

	public CoverImageSpinnerAdapter(SqueezeService service, Player activePlayer, Activity parent) {

		super( parent );
		_service = service;
		_player = activePlayer;
	}


	public View getView(int position, View convertView, ViewGroup parent) {
		Song song = (Song) getItem(position);
		ImageView view = new ImageView( _parent );
		
		//view.setImageResource(R.drawable.cover_50x50_p);
		view.setMaxHeight(200);
		view.setMaxWidth(200);
		view.setMinimumHeight(200);
		view.setMinimumWidth(200);

		if( song != null)
		{
			Bitmap image = ImageLoader.getInstance().get(song.getImageUrl());
			if( image == null)
			{
				ImageLoader.getInstance().load(view, song.getImageUrl(), true);
			}
			else
			{
				view.setImageBitmap(image);
			}
		}
		
		return view;
	}

	@Override
	protected List<? extends Object> createPage(int start, int pageSize) {
		return _service.getCurrentPlaylist(_player, start, pageSize).getResutls();
	}

}
