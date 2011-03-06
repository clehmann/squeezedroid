package net.chrislehmann.squeezedroid.activity;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.listadapter.PlayListAdapter;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.view.NowPlayingInfoPanel;

public class PlayListActivity extends SqueezedroidActivitySupport {

    private static final int CONTEXTMENU_REMOVE_ITEM = 421;
    private static final int CONTEXTMENU_REMOVE_ALBUM = 422;
    private static final int CONTEXTMENU_REMOVE_ARTIST = 423;
    private static final int CONTEXTMENU_GROUP_REMOVE = 100;

    protected ListView listView;
    private NowPlayingInfoPanel _nowPlayingInfoPanel;
    private SqueezedroidActivitySupport _context = this;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_with_status_layout);
        listView = (ListView) findViewById(R.id.list);
        listView.setOnCreateContextMenuListener(onCreateContextMenu);
        listView.setOnItemClickListener(onItemClick);


        runWithService(new SqueezeServiceAwareThread() {
            public void runWithService(final SqueezeService service) {
                final PlayListAdapter playListAdapter = new PlayListAdapter(service, _context, getSelectedPlayer());
                playListAdapter.setOnFirstPageLoadedListener(new Runnable() {
                    public void run() {
                        PlayerStatus status = service.getPlayerStatus(getSelectedPlayer());
                        listView.setSelection(status.getCurrentIndex());
                    }
                });

                runOnUiThread(new Runnable() {
                    public void run() {
                        listView.setAdapter(playListAdapter);
                    }
                });
            }
        });


        _nowPlayingInfoPanel = (NowPlayingInfoPanel) findViewById(R.id.song_info_container);
        if (_nowPlayingInfoPanel != null) {
            _nowPlayingInfoPanel.setParent(this);
        }
    }


    OnCreateContextMenuListener onCreateContextMenu = new OnCreateContextMenuListener() {

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.add(CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ITEM, 0, "Remove song");
            menu.add(CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ALBUM, 1, "Remove artist");
            menu.add(CONTEXTMENU_GROUP_REMOVE, CONTEXTMENU_REMOVE_ARTIST, 2, "Remove album");
        }
    };

    private OnItemClickListener onItemClick = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.jump(getSelectedPlayer(), String.valueOf(position));
                }
            });
        }
    };


    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        boolean handled = false;
        final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        final Song song = (Song) listView.getItemAtPosition(menuInfo.position);

        runWithService(new SqueezeServiceAwareThread() {
            public void runWithService(SqueezeService service) {
                switch (item.getItemId()) {
                    case CONTEXTMENU_REMOVE_ITEM:
                        service.removeItem(getSelectedPlayer(), menuInfo.position);
                        break;
                    case CONTEXTMENU_REMOVE_ARTIST:
                        service.removeAllItemsByArtist(getSelectedPlayer(), song.getArtistId());
                        break;
                    case CONTEXTMENU_REMOVE_ALBUM:
                        service.removeAllItemsInAlbum(getSelectedPlayer(), song.getAlbumId());
                        break;
                    default:
                        break;
                }
            }
        });

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_playlist, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_playlistAdd:
                launchSubActivity(BrowseRootActivity.class, null);
                return true;
            case R.id.menuItem_playlistClear:
                runWithService(new SqueezeServiceAwareThread() {
                    public void runWithService(SqueezeService service) {
                        service.clearPlaylist(getSelectedPlayer());
                    }
                });
                break;
            case R.id.menuItem_playlistDone:
                finish();
                break;
            case R.id.menuItem_playlistNowPlaying:
                runWithService(new SqueezeServiceAwareThread() {
                    public void runWithService(SqueezeService service) {
                        PlayerStatus status = service.getPlayerStatus(getSelectedPlayer());
                        listView.setSelection(status.getCurrentIndex());
                    }
                });
                break;
        }
        return false;
    }

}