package net.chrislehmann.squeezedroid.activity;

import android.widget.ListAdapter;
import net.chrislehmann.squeezedroid.listadapter.ApplicationListAdapter;
import net.chrislehmann.squeezedroid.service.SqueezeService;

public class ListApplicationsActivity extends ListApplicationsActivitySupport {


    @Override
    protected ListAdapter getAdapter(SqueezeService service) {
        return new ApplicationListAdapter(service, this);
    }
}