package net.chrislehmann.squeezedroid.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.Genre;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Playlist;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.ServerStatusHandler;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.view.NowPlayingInfoPanel;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for all @{Activity}s that allow the user to browse a list of @{Item}s.
 */

public abstract class ItemListActivity extends SqueezedroidActivitySupport {

    protected Activity context = this;

    protected ListView listView;

    protected abstract Item getParentItem();

    public ItemListActivity() {
        super();
    }

    protected boolean isItemPlayable(Item item) {
        return item != null;
    }


    List<Class<? extends Item>> DOWNLOADABLE_TYPES = Arrays.asList(Song.class, Album.class, Genre.class, Playlist.class);

    protected boolean isItemDownloadable(Item item) {
        return item != null && DOWNLOADABLE_TYPES.contains(item.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.list_with_status_layout);

        listView = (ListView) findViewById(R.id.list);
        listView.setFastScrollEnabled(true);
        getListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                context.onCreateContextMenu(menu, v, menuInfo);
            }
        });

        NowPlayingInfoPanel nowPlayingInfoPanel = (NowPlayingInfoPanel) findViewById(R.id.song_info_container);
        if (nowPlayingInfoPanel != null) {
            nowPlayingInfoPanel.setParent(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_itemlist, menu);
        final Item parentItem = getParentItem();
        if (parentItem != null && parentItem.getId() != null) {
            boolean itemPlayable = isItemPlayable(parentItem);
            menu.findItem(R.id.menuItem_itemlistEnqueue).setVisible(itemPlayable);
            menu.findItem(R.id.menuItem_itemlistPlay).setVisible(itemPlayable);
            menu.findItem(R.id.menuItem_itemlistPlayNext).setVisible(itemPlayable);
            menu.findItem(R.id.menuItem_itemlistDownload).setVisible(isItemDownloadable(parentItem));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean handled = true;
        final Item parentItem = getParentItem();
        runWithService(new SqueezeServiceAwareThread() {
            public void runWithService(SqueezeService service) {
                String message = null;
                switch (item.getItemId()) {
                    case R.id.menuItem_itemlistDone:
                        setResult(SqueezeDroidConstants.ResultCodes.RESULT_DONE);
                        finish();
                        break;
                    case R.id.menuItem_itemlistEnqueue:
                        service.addItem(getSelectedPlayer(), parentItem);
                        message = "Added to playlist.";
                        break;
                    case R.id.menuItem_itemlistPlay:
                        service.playItem(getSelectedPlayer(), parentItem);
                        message = "Now playing.";
                        break;
                    case R.id.menuItem_itemlistPlayNext:
                        service.playItemNext(getSelectedPlayer(), parentItem);
                        message = "Playing next.";
                        break;
                    case R.id.menuItem_itemlistDownload:
                        addDownloadsForItem(parentItem);
                        break;
                }
                if (message != null) {
                    //I hate java...
                    final String messageForClosure = message;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, messageForClosure, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SqueezeDroidConstants.ResultCodes.RESULT_DONE) {
            setResult(resultCode);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        boolean handled = false;

        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        final Item selectedItem = (Item) listView.getAdapter().getItem(menuInfo.position);

        if (selectedItem != null) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    String message = null;
                    switch (item.getItemId()) {
                        case R.id.contextMenuItem_itemlistEnqueue:
                            service.addItem(getSelectedPlayer(), selectedItem);
                            message = "Added to playlist.";
                            break;
                        case R.id.contextMenuItem_itemlistPlay:
                            service.playItem(getSelectedPlayer(), selectedItem);
                            message = "Now playing.";
                            break;
                        case R.id.contextMenuItem_itemlistPlayNext:
                            service.playItemNext(getSelectedPlayer(), selectedItem);
                            message = "Playing next.";
                            break;
                        case R.id.contextMenuItem_itemlistDownload:
                            addDownloadsForItem(selectedItem);
                            break;
                    }
                    if (message != null) {
                        //I hate java...
                        final String messageForClosure = message;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(context, messageForClosure, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });
        }
        return handled;
    }

    public ListView getListView() {
        return listView;
    }

    private ServerStatusHandler onServiceStatusChanged = new ServerStatusHandler() {
        public void onDisconnect() {
            //Just try and reconnect...
            getSqueezeDroidApplication().resetService();
            forceConnect();
        }
    };


    @Override
    protected void onResume() {
        if (!closing) {
            runWithService(new SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.subscribe(onServiceStatusChanged);
                }
            });
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        runWithService(
                new SqueezeServiceAwareThread() {
                    public void runWithService(SqueezeService service) {
                        service.unsubscribe(onServiceStatusChanged);
                    }
                }, false
        );

        super.onPause();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        final Item selectedItem = (Item) listView.getAdapter().getItem(adapterMenuInfo.position);
        if (isItemPlayable(selectedItem)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.contextmenu_itemlist, menu);
            menu.findItem(R.id.contextMenuItem_itemlistDownload).setVisible(isItemDownloadable(selectedItem));
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }


}