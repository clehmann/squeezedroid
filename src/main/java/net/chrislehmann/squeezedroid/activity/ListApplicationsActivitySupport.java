package net.chrislehmann.squeezedroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.service.Application;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.view.NowPlayingInfoPanel;

public abstract class ListApplicationsActivitySupport extends SqueezedroidActivitySupport {
    static final int MENU_DONE = 111;

    private ListView listView;
    private NowPlayingInfoPanel _nowPlayingInfoPanel;

    protected abstract ListAdapter getAdapter(SqueezeService service);

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Item item = (Item) listView.getAdapter().getItem(position);
            if (item instanceof Application) {
                Application selectedApp = (Application) item;
                Intent i = new Intent();
                i.setAction("net.chrislehmann.squeezedroid.action.BrowseApplication");
                i.putExtra(SqueezeDroidConstants.IntentDataKeys.KEY_BROWSEAPPLICATION_APPLICATION, selectedApp);
                startActivityForResult(i, SqueezeDroidConstants.RequestCodes.REQUEST_BROWSE);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_with_status_layout);

        listView = (ListView) findViewById(R.id.list);
        listView.setFastScrollEnabled(true);

        runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
            public void runWithService(SqueezeService service) {
                listView.setAdapter(getAdapter(service));
            }
        });

        listView.setOnItemClickListener(onItemClick);

        _nowPlayingInfoPanel = (NowPlayingInfoPanel) findViewById(R.id.song_info_container);
        if (_nowPlayingInfoPanel != null) {
            _nowPlayingInfoPanel.setParent(this);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SqueezeDroidConstants.ResultCodes.RESULT_DONE) {
            setResult(resultCode);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_DONE, 0, "Done");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case MENU_DONE:
                setResult(SqueezeDroidConstants.ResultCodes.RESULT_DONE);
                finish();
                break;
            default:
                handled = false;
        }
        if (!handled) {
            handled = super.onOptionsItemSelected(item);
        }
        return handled;
    }
}
