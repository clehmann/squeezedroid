package net.chrislehmann.squeezedroid.listadapter;

import java.util.List;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.SqueezeService.Sort;
import net.chrislehmann.util.ImageLoader;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumListAdapter extends PagableAdapter {
	protected SqueezeService _service;
	private Item _parentItem;
	private Integer numItems = 1;
	private SqueezeService.Sort _sort;

	public AlbumListAdapter(SqueezeService service, Activity parent) {
		super(parent);
		_service = service;
		_sort = Sort.TITLE;
	}

	public AlbumListAdapter(SqueezeService service, Activity parent, Item parentItem, SqueezeService.Sort sort) {
		super(parent);
		_service = service;
		_parentItem = parentItem;
		_sort = sort;
	}

	@Override
	public int getCount() {
		return numItems;
	}

	protected List<? extends Object> createPage(int start, int pageSize) {
		BrowseResult<Album> result = _service.browseAlbums(_parentItem, start, pageSize, _sort);
		numItems = result.getTotalItems();
		return result.getResutls();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;

		Album album = (Album) getItem(position);
		if (album != null) {
		    if( convertView == null || convertView.getId() == R.id.loading_row_layout )
		    {
		       view = _parent.getLayoutInflater().inflate(R.layout.album_row_layout, null);
		    }
		    else
		    {
		       view = convertView;
		    }
		    
			ImageView coverArt = (ImageView) view.findViewById(R.id.album_thumbnail);

			coverArt.setImageResource( R.drawable.default_album_thumb );
			if (album.getImageThumbnailUrl() != null) {
				ImageLoader.getInstance().load(coverArt, album.getImageThumbnailUrl());
			}
			
			TextView albumNameText = (TextView) view.findViewById(R.id.album_name_text);
			albumNameText.setText(album.getName());
			
			TextView artistNameText = (TextView) view.findViewById(R.id.artist_name_text);
			artistNameText.setText(album.getArtist());
		}
		else 
		{
			view = super.getView(position, convertView, parent);
		}
		return view;
	}

}
