package net.chrislehmann.squeezedroid.listadapter;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PagableAdapter extends BaseAdapter {
    private Runnable _onFirstPageLoaded;
    private boolean _fistPageLoaded = false;

    protected abstract List<? extends Object> createPage(int i, int pageSize);

    protected int _pageSize = 50;

    protected Activity _parent;

    protected Map<Integer, List<? extends Object>> _pages = new HashMap<Integer, List<? extends Object>>();
    protected int _count = 1;

    protected View loadingView;


    public PagableAdapter(Activity parent) {
        super();
        _parent = parent;
    }

    public int getCount() {
        return _count;
    }


    public void resetPages() {
        _pages.clear();
        _count = 1;
        _fistPageLoaded = false;
        notifyChange();
    }

    public Object getItem(int position) {
        int pageNumber = getPageNumber(position);
        List<? extends Object> page = null;
        if (!_pages.containsKey(pageNumber)) {
            _pages.put(pageNumber, new ArrayList<Item>());
            new UpdaterThread(pageNumber).start();
        } else {
            page = _pages.get(pageNumber);
        }
        int offset = position - (pageNumber * _pageSize);
        Object item = null;
        if (page != null && offset < page.size()) {
            item = page.get(offset);
        }
        return item;

    }


    public void setOnFirstPageLoadedListener(Runnable onLoaded) {

        _onFirstPageLoaded = onLoaded;
    }


    private int getPageNumber(int position) {
        return (int) Math.floor(position / 50);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        //TODO - Load this as a resource
        View view = convertView;
        Item item = (Item) getItem(position);
        if (item != null) {
            TextView tv;
            if (view != null && view instanceof TextView) {
                tv = (TextView) view;
            } else {
                tv = new TextView(parent.getContext());
            }
            tv.setText(item.getName());
            tv.setTextSize(19);
            view = tv;
        } else {
            if (convertView == null || convertView.getId() != R.id.loading_row_layout) {
                view = _parent.getLayoutInflater().inflate(net.chrislehmann.squeezedroid.R.layout.loading_row_layout, null);
            }
        }
        view.setPadding(10, 10, 10, 10);
        return view;
    }

    /**
     * Thread that calls {@link PagableAdapter#createPage(int, int)} and notifies the gui of changes.
     *
     * @author lehmanc
     */
    private class UpdaterThread extends Thread {
        private int pageNumber;

        public UpdaterThread(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        @Override
        public void run() {
            List<? extends Object> page = createPage(pageNumber * _pageSize, _pageSize);
            _pages.put(pageNumber, page);
            _count += page.size();
            if (page.size() < _pageSize) {
                _count -= 1;
            }

            notifyChange();
        }
    }

    protected void notifyChange() {
        _parent.runOnUiThread(new Thread() {
            public void run() {
                notifyDataSetChanged();
                
                if( !_fistPageLoaded && _onFirstPageLoaded != null )
                {
                    _fistPageLoaded = true;
                    _onFirstPageLoaded.run();
                }
            }
        });
    }

}
