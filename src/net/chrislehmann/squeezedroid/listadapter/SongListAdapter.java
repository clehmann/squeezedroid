package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.util.ImageLoader;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SongListAdapter extends PagableAdapter {
	protected SqueezeService _service;
	private Item _parentItem;
	protected Integer numItems = 1;

	public SongListAdapter(SqueezeService service, Activity parent, Item parentItem) {
		super(parent);
		_service = service;
		_parentItem = parentItem;
	}

	protected List<? extends Object> createPage(int start, int pageSize) {
		BrowseResult<Song> result = _service.browseSongs(_parentItem, start, pageSize);
		numItems = result.getTotalItems();
		return result.getResutls();
	}

	@Override
	public int getCount() {
		return numItems;
	}
	
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		Song item = (Song) getItem(position);
		if (item == null) {
			view = super.getView(position, convertView, parent);
		} else {

			view = _parent.getLayoutInflater().inflate(R.layout.song_row_layout, null);

			if (item != null) {
				TextView nameText = (TextView) view.findViewById(R.id.song_name_text);
                TextView artistNameText = (TextView) view.findViewById( R.id.song_artist_name_text );
				TextView albumNameText = (TextView) view.findViewById( R.id.song_album_name_text );
				
				ImageView coverImage = (ImageView) view.findViewById(R.id.song_thumbnail);

				if (item.getImageThumbnailUrl() != null) {
					Bitmap image = ImageLoader.getInstance().get(item.getImageThumbnailUrl());
					if (image != null) {
						coverImage.setImageBitmap(image);
					} else {
						ImageLoader.getInstance().load(coverImage, item.getImageThumbnailUrl(), true);
					}
				}
				nameText.setText(item.getName());
				artistNameText.setText( item.getArtist() );
                albumNameText.setText( item.getAlbum() );

			}

		}
		return view;
	}
}
