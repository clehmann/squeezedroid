package net.chrislehmann.squeezedroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.listadapter.PlayerListAdapter;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerIdEqualsPredicate;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.NotPredicate;

import java.util.List;


/**
 * Activity that presents a list of players to the user and allows them 
 * to choose one.  If chosen, the the selected player will put 
 * into the {@link SqueezeDroidConstants.IntentDataKeys#KEY_SELECTED_PLAYER}
 * extra.
 * 
 * The following extras can be set to control what appears on the list:
 * {@link SqueezeDroidConstants.IntentDataKeys#KEY_PLAYERLIST_INCLUDE_SELECTED_PLAYER} - Boolean - Defaults to true
 *    If set to false, the currently selected player will not be included in the list.
 * {@link SqueezeDroidConstants.IntentDataKeys#KEY_PLAYERLIST_REMOVE_DUPLICATE_PLAYERS} - Boolean - Defaults to false
 *    If set to true, if a player will not be included if any other players are in the 
 *    list that is is syncronized to.
 * {@link SqueezeDroidConstants.IntentDataKeys#KEY_PLAYERLIST_EMPTY_PLAYER_NAME} - String
 *    If set, the user will allowed to pick from an option with the value in this extra
 *    that will cause the returned player to be null.
 *         
 * @author lehmanc
 *
 */
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
   }

   @Override
   protected void onResume()
   {
      createList();
      super.onResume();
   }
   
   private void createList()
   {
      runWithService(new SqueezeServiceAwareThread() {
          public void runWithService(SqueezeService service) {
              boolean removeDuplicatePlayers = getIntent().getBooleanExtra(SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_REMOVE_DUPLICATE_PLAYERS, false);
              final List<Player> players = service.getPlayers(removeDuplicatePlayers);

              //Remove the currently selected player if KEY_INCLUDE_SELECTED_PLAYER is set to fa;se
              boolean includeSelectedPlayer = getIntent().getBooleanExtra(SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_INCLUDE_SELECTED_PLAYER, true);
              if (!includeSelectedPlayer) {
                  String selectedPlayer = getSelectedPlayer();
                  if (selectedPlayer != null ) {
                      CollectionUtils.filter(players, new NotPredicate(new PlayerIdEqualsPredicate(selectedPlayer)));
                  }
              }

              //If the caller specifies an empty_key_name, add an extra player to use at the 'null' player
              String emptyPlayerName = getIntent().getStringExtra(SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_EMPTY_PLAYER_NAME);
              if (emptyPlayerName != null) {
                  Player emptyPlayer = new Player();
                  emptyPlayer.setName(emptyPlayerName);
                  players.add(emptyPlayer);
              }
              runOnUiThread(new Runnable() {
                  public void run() {
                      ArrayAdapter<Player> playersAdapter = new PlayerListAdapter(context, players, context);
                      listView.setAdapter(playersAdapter);
                      playersAdapter.notifyDataSetChanged();
                  }
              });
          }
      });
   }

   private OnItemClickListener onItemClicked = new OnItemClickListener()
   {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         Player player = (Player) listView.getItemAtPosition( position );
         
         //See if the user has selected the empty player
         String emptyPlayerName = getIntent().getStringExtra( SqueezeDroidConstants.IntentDataKeys.KEY_PLAYERLIST_EMPTY_PLAYER_NAME );
         if( emptyPlayerName != null && emptyPlayerName.equals( player.getName() ))
         {
            player = null;
         }
         
         Intent intent = new Intent();
         intent.putExtra( SqueezeDroidConstants.IntentDataKeys.KEY_SELECTED_PLAYER, player.getId() );
         setResult( RESULT_OK, intent );
         finish();
      }
   };
}
