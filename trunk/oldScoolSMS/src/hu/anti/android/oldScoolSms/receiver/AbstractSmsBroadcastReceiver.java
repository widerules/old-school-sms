package hu.anti.android.oldScoolSms.receiver;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public abstract class AbstractSmsBroadcastReceiver extends BroadcastReceiver {
    protected void logIntent(String object, Intent intent) {
	Bundle extras = intent.getExtras();

	Log.d("OldScoolSMS", object + "/intent: " + intent);
	Log.d("OldScoolSMS", object + "/intent getAction: " + intent.getAction());
	Log.d("OldScoolSMS", object + "/intent getData: " + intent.getData());
	Log.d("OldScoolSMS", object + "/intent getDataString: " + intent.getDataString());
	Log.d("OldScoolSMS", object + "/intent getExtras: " + (extras == null ? "<>" : extras.size()));
	if (extras != null) {
	    for (String key : extras.keySet()) {
		Log.d("OldScoolSMS", object + "/intent extras/" + key + ": " + extras.get(key));
	    }
	}
    }
}
