package net.chrislehmann.squeezedroid.listadapter;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class SearchResultExpandableListAdapter extends BaseExpandableListAdapter
{
   private static final String SONGS = "Songs";
   private static final String GENRES = "Genres";
   private static final String ALBUMS = "Albums";
   private static final String ARTISTS = "Artists";
   private SearchResult result;
   private List<String> groups = new ArrayList<String>();

   private void addLabelIfListNotEmpty(List<? extends Item> list, String label)
   {
      if ( list != null && !list.isEmpty() )
      {
         groups.add( label );
      }
   }

   public void setResult(SearchResult result)
   {
      groups.clear();
      addLabelIfListNotEmpty( result.getAlbums(), ALBUMS );
      addLabelIfListNotEmpty( result.getArtists(), ARTISTS );
      addLabelIfListNotEmpty( result.getGenres(), GENRES );
      addLabelIfListNotEmpty( result.getSongs(), SONGS );

      this.result = result;
   }

   public Object getChild(int groupPosition, int childPosition)
   {
      String groupName = groups.get( groupPosition );
      return getGroup( groupName ).get( childPosition );
   }

   public long getChildId(int groupPosition, int childPosition)
   {
      return childPosition;
   }


   public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
   {
      Item item = (Item) getChild( groupPosition, childPosition );
      TextView tv = (TextView) convertView;
      if( tv == null )
      {
         tv = new TextView( parent.getContext() );
      }
      tv.setText( item.getName() );
      tv.setTextSize( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, parent.getResources().getDisplayMetrics()) );

      int pixels10 =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10, parent.getResources().getDisplayMetrics());
      int pixels40 =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 40,  parent.getResources().getDisplayMetrics());
      tv.setPadding( pixels40, pixels10, pixels10, pixels10);
      return tv;
   }

   private List<? extends Item> getGroup( String groupName )
   {
      List<? extends Item>  items = null;
      if ( groupName.equals( ARTISTS ) )
      {
         items = result.getArtists();
      }
      else if ( groupName.equals( SONGS ) )
      {
         items = result.getSongs();
      }
      else if ( groupName.equals( GENRES ) )
      {
         items = result.getGenres();
      }
      else if ( groupName.equals( ALBUMS ) )
      {
         items = result.getAlbums();
      }
      return items;
   }
   
   public int getChildrenCount(int groupPosition)
   {
      String groupName = groups.get( groupPosition );
      return getGroup( groupName ).size();
   }

   public Object getGroup(int groupPosition)
   {
      return groups.get( groupPosition );
   }

   public int getGroupCount()
   {
      return groups.size();
   }

   public long getGroupId(int groupPosition)
   {
      return groupPosition;
   }

   public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
   {
      String item = (String) getGroup( groupPosition );
      TextView tv = (TextView) convertView;
      if( tv == null )
      {
         tv = new TextView( parent.getContext() );
      }
      tv.setText( item );
      tv.setTextSize( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, parent.getResources().getDisplayMetrics()) );

      int pixels10 =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10, parent.getResources().getDisplayMetrics());
      int pixels40 =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 40,  parent.getResources().getDisplayMetrics());
      tv.setPadding( pixels40, pixels10, pixels10, pixels10);
      return tv;
   }

   public boolean hasStableIds()
   {
      return false;
   }

   public boolean isChildSelectable(int groupPosition, int childPosition)
   {
      return true;
   }


}