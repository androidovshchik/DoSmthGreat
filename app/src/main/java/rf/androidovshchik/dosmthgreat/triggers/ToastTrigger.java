package rf.androidovshchik.dosmthgreat.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import rf.androidovshchik.dosmthgreat.utils.AppUtil;

public class ToastTrigger extends BroadcastReceiver {

	public static final String ACTION = "DO_AWESOME_TOAST";

	public static final String EXTRA_MESSAGE = "message";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.hasExtra(EXTRA_MESSAGE)) {
			Toast.makeText(context, AppUtil.getName(context) + ": "  +
				intent.getStringExtra(EXTRA_MESSAGE), Toast.LENGTH_LONG).show();
		}
	}
}
