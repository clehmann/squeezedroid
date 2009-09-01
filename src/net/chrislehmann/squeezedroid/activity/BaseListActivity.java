package net.chrislehmann.squeezedroid.activity;

import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import android.app.ListActivity;

public class BaseListActivity extends ListActivity {

	
	protected SqueezeDroidApplication getSqueezeDroidApplication() {
		return ActivityUtils.getSqueezeDroidApplication(this);
	}
	
	protected SqueezeService getService()
	{
		return ActivityUtils.getService(this);
	}
	
	protected Player getSelectedPlayer()
	{
		return getSqueezeDroidApplication().getSelectedPlayer();
	}
}
