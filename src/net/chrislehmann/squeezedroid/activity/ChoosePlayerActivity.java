package net.chrislehmann.squeezedroid.activity;

import java.util.ArrayList;
import java.util.List;

import net.chrislehmann.squeezedroid.listadapter.PlayerListAdapter;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChoosePlayerActivity extends ListActivity
{
   private Activity context = this;
   
   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );
      
      createList();
   }

   private void createList()
   {
      SqueezeService service = ActivityUtils.getService( context );
      List<Player> players = new ArrayList<Player>();
      if( service != null )
      {
         players = service.getPlayers();
      }
      
      ArrayAdapter<Player> playersAdapter = new PlayerListAdapter( this, players, this );
      setListAdapter( playersAdapter );
      playersAdapter.notifyDataSetChanged();
   }
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id)
   {
      super.onListItemClick( l, v, position, id );
      Player player = (Player) l.getItemAtPosition( position );
      Intent intent = new Intent();
      intent.setData( Uri.parse( "squeeze://players/" + player.getId() ) );
      intent.putExtra( SqueezeDroidConstants.IntentDataKeys.KEY_SELECTED_PLAYER, player );
      setResult( RESULT_OK, intent );
      finish();
   }
   
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
		switch (requestCode) {
		case SqueezeDroidConstants.RequestCodes.REQUEST_CONNECT:
			if( RESULT_CANCELED == resultCode )
			{
				setResult(RESULT_CANCELED);
				finish();
			}
			else
			{
				createList();
			}
			break;
		default:
			break;
		}
	   
   }
   
}
