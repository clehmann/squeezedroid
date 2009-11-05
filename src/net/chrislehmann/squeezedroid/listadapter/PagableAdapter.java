package net.chrislehmann.squeezedroid.listadapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.chrislehmann.squeezedroid.model.Item;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public abstract class PagableAdapter extends BaseAdapter
{
   protected abstract List<? extends Object> createPage(int i, int pageSize);

   protected int pageSize = 50;

   protected Activity _parent;
   
   protected Map<Integer, List<? extends Object> > _pages = new HashMap<Integer, List<? extends Object> >();
   protected int _count = 1;

   public PagableAdapter(Activity parent)
   {
      super();
      _parent = parent;
   }

   public int getCount()
   {
      return _count ;
   }

   public void resetPages()
   {
      _pages.clear();
      _count = 1;
      notifyChange();
   }
   
   public Object getItem(int position)
   {
      int pageNumber = getPageNumber( position );
      List<? extends Object> page = null;
      if( !_pages.containsKey( pageNumber ) )
      {
         _pages.put( pageNumber, new ArrayList<Item>());
         new UpdaterThread( pageNumber ).start();
      }
      else
      {
         page = _pages.get( pageNumber );
      }
      int offset = position - (pageNumber * pageSize);
      Object item = null;
      if( page != null && offset < page.size() )
      {
         item = page.get( offset );
      }
      return item;
      
   }

   private int getPageNumber(int position)
   {
      return (int) Math.floor( position / 50 );
   }

   public long getItemId(int position)
   {
      return position;
   }

   public View getView(int position, View convertView, ViewGroup parent)
   {
      //TODO - Load this as a resource
      View view;
      Item item = (Item) getItem( position );
      if( item != null )
      {
         TextView tv = new TextView( parent.getContext() );
         tv.setText( item.getName() );
         tv.setTextSize( 19 );
         view = tv;
      }
      else
      {
         view = _parent.getLayoutInflater().inflate( net.chrislehmann.squeezedroid.R.layout.loading_row_layout, null );
      }
      view.setPadding( 10, 10, 10, 10 );
      return view;
   }

   /**
    * Thread that calls {@link PagableAdapter#createPage(int, int)} and notifies the gui of changes.
    * @author lehmanc
    *
    */
   private class UpdaterThread extends Thread
   {
      private int pageNumber;
      public UpdaterThread( int pageNumber )
      {
         this.pageNumber = pageNumber;
      }
      
      @Override
      public void run()
      {
         List<? extends Object> page = createPage( pageNumber * pageSize, pageSize  );
         _pages.put( pageNumber, page);
         _count += page.size();
         if( page.size() < pageSize )
         {
            _count -= 1;
         }
         
         notifyChange();
      }
   }
   
   protected void notifyChange()
   {
      _parent.runOnUiThread( new Thread()
      {
         public void run(){ 
            notifyDataSetChanged();
         }
      } );
   }
   
}
