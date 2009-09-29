package net.chrislehmann.squeezedroid.activity;

import java.util.ArrayList;
import java.util.List;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.listadapter.PlayerListAdapter;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ChoosePlayerActivity extends SqueezedroidActivitySupport
{

   private ListView listView;
   private Activity context = this;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );
      setContentView( R.layout.list_layout );
      listView = (ListView) findViewById( R.id.list );
      listView.setOnItemClickListener( onItemClicked );
      createList();
   }

   private void createList()
   {
      runWithService( new SqueezeServiceAwareThread()
      {
         public void runWithService(SqueezeService service) throws Exception
         {
            final List<Player> players = service.getPlayers();
            ArrayAdapter<Player> playersAdapter = new PlayerListAdapter( context, players, context );
            listView.setAdapter( playersAdapter );
            playersAdapter.notifyDataSetChanged();
         }
      } );
   }

   private OnItemClickListener onItemClicked = new OnItemClickListener()
   {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         Player player = (Player) listView.getItemAtPosition( position );
         Intent intent = new Intent();
         intent.setData( Uri.parse( "squeeze://players/" + player.getId() ) );
         intent.putExtra( SqueezeDroidConstants.IntentDataKeys.KEY_SELECTED_PLAYER, player );
         setResult( RESULT_OK, intent );
         finish();
      }
   };
}
