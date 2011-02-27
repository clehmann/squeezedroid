package net.chrislehmann.squeezedroid.activity;

import android.widget.ListAdapter;
import net.chrislehmann.squeezedroid.listadapter.RadioStationsListAdapter;
import net.chrislehmann.squeezedroid.service.SqueezeService;

public class ListRadioStationsActivity extends ListApplicationsActivitySupport {

    @Override
    protected ListAdapter getAdapter( SqueezeService service) {
        return new RadioStationsListAdapter(service, this);
    }
}