package net.chrislehmann.squeezedroid.listadapter;

import java.util.HashMap;
import java.util.Map;

import net.chrislehmann.squeezedroid.model.Item;
import android.app.Activity;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

public abstract class BaseCompositeExpandableListAdapter extends
		BaseExpandableListAdapter {

	protected Activity _parent;
	protected ListAdapter _groupsListAdapter;
	protected Map<Integer, ListAdapter> _albumsCache = new HashMap<Integer, ListAdapter>();
	
	protected DataSetObserver observer = new DataSetObserver() {
			@Override
			public void onChanged() {
				_parent.runOnUiThread(
					new Thread(){	public void run() {notifyDataSetChanged();}}
				);
				super.onChanged();
			}
		};

	public BaseCompositeExpandableListAdapter() {
		super();
	}

	public BaseCompositeExpandableListAdapter(Activity parent, ListAdapter groupsAdapter) {
		_parent = parent;
		_groupsListAdapter = groupsAdapter;
		_groupsListAdapter.registerDataSetObserver(observer);
	}

	public Object getChild(int groupPosition, int childPosition) {
		Item child = null;
		Item parentItem = (Item) _groupsListAdapter.getItem(groupPosition);
		if( parentItem != null )
		{
			ListAdapter songAdapter = getChildListAdapter(groupPosition, parentItem);
			child = (Item) songAdapter.getItem(childPosition);
		}
		return child;
	}

	private ListAdapter getChildListAdapter(int groupPosition, Item parentItem) {
		ListAdapter songAdapter = null;
		if( !_albumsCache.containsKey(groupPosition))
		{
			ListAdapter adapter = createListAdapter( parentItem );
			_albumsCache.put(groupPosition, adapter );
			adapter.registerDataSetObserver( observer );
			
		}
		songAdapter = _albumsCache.get(groupPosition);
		return songAdapter;
	}

	protected abstract ListAdapter createListAdapter(Item parentItem);

	public long getChildId(int groupPosition, int childPosition) {
		
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View child = new LinearLayout(_parent);
				Item parentItem = (Item) _groupsListAdapter.getItem(groupPosition);
				if( parentItem != null )
				{
					ListAdapter songAdapter = getChildListAdapter(groupPosition, parentItem);
					child = songAdapter.getView(childPosition, convertView, parent);
				}
				return child;
			}

	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		int count = 0;
		Item parentItem = (Item) _groupsListAdapter.getItem(groupPosition);
		if( parentItem != null )
		{
			ListAdapter songAdapter = getChildListAdapter(groupPosition, parentItem);
			count = songAdapter.getCount();
		}
		return count;
	}

	public Object getGroup(int groupPosition) {
		return _groupsListAdapter.getItem(groupPosition);
	}

	public int getGroupCount() {
		return _groupsListAdapter.getCount();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
				View view = _groupsListAdapter.getView(groupPosition, convertView, parent);
				view.setPadding(40, 10, 10, 10);
				return view;
			}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}