package net.chrislehmann.squeezedroid.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.squeezedroid.service.ServerStatusHandler;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.view.NowPlayingInfoPanel;

import java.util.List;

public abstract class ItemListActivity extends SqueezedroidActivitySupport {
    static final int MENU_DONE = 111;
    static final int MENU_PLAY_ALL = 112;
    static final int MENU_ENQUE_ALL = 113;
    static final int MENU_PLAY_ALL_NEXT = 114;

    private static final int CONTEXTMENU_PLAY_ITEM = 7070;
    private static final int CONTEXTMENU_ADD_ITEM = 7080;
    private static final int CONTEXTMENU_PLAY_NEXT = 7090;
    private static final int CONTEXTMENU_DOWNLOAD = 8000;

    protected Activity context = this;


    protected abstract Item getParentItem();

    protected ListView listView;

    protected SqueezeService service;

    public ItemListActivity() {
        super();
    }

    protected boolean isItemPlayable(Item item) {
        return item != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.list_with_status_layout);

        listView = (ListView) findViewById(R.id.list);
        listView.setFastScrollEnabled(true);
        getListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
                final Item selectedItem = (Item) listView.getAdapter().getItem(adapterMenuInfo.position);
                if (isItemPlayable(selectedItem)) {
                    menu.add(Menu.NONE, CONTEXTMENU_ADD_ITEM, 0, "Add To Playlist");
                    menu.add(Menu.NONE, CONTEXTMENU_PLAY_ITEM, 1, "Play Now");
                    menu.add(Menu.NONE, CONTEXTMENU_PLAY_NEXT, 1, "Play Next");
                    menu.add(Menu.NONE, CONTEXTMENU_DOWNLOAD, 1, "Download");
                }
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
        if (getParentItem() != null && getParentItem().getId() != null) {
            menu.findItem(R.id.menuItem_itemlistEnqueue).setVisible(true);
            menu.findItem(R.id.menuItem_itemlistPlay).setVisible(true);
            menu.findItem(R.id.menuItem_itemlistPlayNext).setVisible(true);
            menu.findItem(R.id.menuItem_itemlistDownload).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;
        SqueezeService service = getService();
        if (service != null) {
            Item parentItem = getParentItem();
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
                default:
                    handled = false;
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
        if (!handled) {
            handled = super.onOptionsItemSelected(item);
        }
        return handled;
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
    public boolean onContextItemSelected(MenuItem item) {
        SqueezeService service = getService();
        boolean handled = false;

        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        final Item selectedItem = (Item) listView.getAdapter().getItem(menuInfo.position);

        if (selectedItem != null && service != null) {
            handled = true;
            String message = null;
            switch (item.getItemId()) {
                case CONTEXTMENU_ADD_ITEM:
                    service.addItem(getSelectedPlayer(), selectedItem);
                    message = "Added to playlist.";
                    break;
                case CONTEXTMENU_PLAY_ITEM:
                    service.playItem(getSelectedPlayer(), selectedItem);
                    message = "Now playing.";
                    break;
                case CONTEXTMENU_PLAY_NEXT:
                    service.playItemNext(getSelectedPlayer(), selectedItem);
                    message = "Playing next.";
                    break;
                case CONTEXTMENU_DOWNLOAD:
                    addDownloadsForItem(selectedItem);
                    break;

                default:
                    handled = false;
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
        if (!handled) {
            handled = super.onContextItemSelected(item);
        }
        return handled;
    }

    private void addDownloadsForItem(final Item selectedItem) {
        new Thread() {
            public void run() {
                runWithService(new SqueezeServiceAwareThread() {
                    public void runWithService(SqueezeService service) {
                        List<Song> songs = service.getSongsForItem(selectedItem);
                        for (Song song : songs) {
                            addDownload(song.getUrl(), Environment.getExternalStorageDirectory() + "/music/" + song.getLocalPath());

                        }
                    }
                });
            }
        }.start();
    }


    public ListView getListView() {
        return listView;
    }

    private ServerStatusHandler onServiceStatusChanged = new ServerStatusHandler() {
        public void onDisconnect() {
            //Just try and reconnect...
            getSqueezeDroidApplication().resetService();
            getService();
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
        SqueezeService service = getService(false);
        if (service != null) {
            service.unsubscribe(onServiceStatusChanged);
        }
        super.onPause();
    }


}