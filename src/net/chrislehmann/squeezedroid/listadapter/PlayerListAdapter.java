package net.chrislehmann.squeezedroid.listadapter;

import java.util.Collection;
import java.util.List;

import net.chrislehmann.squeezedroid.model.Player;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlayerListAdapter extends ArrayAdapter<Player>
{

   private Activity _parent;

   public PlayerListAdapter(Context context, List<Player> objects, Activity parent)
   {
      super( context, R.layout.simple_list_item_1, objects );
      _parent = parent;
   }

   @SuppressWarnings("unchecked")
   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {

      Player player = getItem( position );
      
      View view = _parent.getLayoutInflater().inflate( net.chrislehmann.squeezedroid.R.layout.player_row_layout , null );
      TextView nameView = (TextView) view.findViewById( net.chrislehmann.squeezedroid.R.id.player_name_text );
      nameView.setText( player.getName() );
      
      if( player.getSyncronizedPlayers().size() > 0 )
      {
         TextView syncListView = (TextView) view.findViewById( net.chrislehmann.squeezedroid.R.id.synced_players_text );
         String synclistViewText = "Syncronized with: ";
         Collection syncedPlayerNames = CollectionUtils.collect( player.getSyncronizedPlayers(), new Transformer()
         {
            public Object transform(Object arg0)
            {
               return ((Player) arg0).getName();
            }
         });
         synclistViewText += StringUtils.join( syncedPlayerNames, ", " );
         syncListView.setText( synclistViewText );
      }
      return view;
      
   }
}
