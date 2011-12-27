package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class DeliveredStatusReceiver extends AbstractSmsBroadcastReceiver {
    public static final String DELIVERED_ACTION = "SMS_DELIVERED";

    @Override
    public void onReceive(Context context, Intent intent) {
	logIntent("delivered", intent);

	Uri uri = intent.getData();

	if (uri == null) {
	    Log.d("OldSchoolSMS", "Received empty intent, skipping");
	    return;
	}

	String message;
	ContentValues values = new ContentValues();
	switch (getResultCode()) {
	case Activity.RESULT_OK:
	    values.put(Sms.Fields.STATUS, Sms.Status.COMPLETE);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_SENT);

	    message = context.getResources().getString(R.string.SMS_DELIVERED_RESULT_OK);
	    break;
	case Activity.RESULT_CANCELED:
	    values.put(Sms.Fields.STATUS, Sms.Status.FAILED);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_FAILED);

	    message = context.getResources().getString(R.string.SMS_DELIVERED_RESULT_FAILED);
	    break;

	default:
	    message = "" + getResultCode();
	    break;
	}

	// update status
	context.getContentResolver().update(uri, values, null, null);

	if (Activity.RESULT_OK == getResultCode()) {
	    // get preferences
	    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	    boolean notifyOnDelivery = sharedPrefs.getBoolean("notifyOnDelivery", true);

	    if (!notifyOnDelivery)
		return;
	}

	// notify the user
	showDeliveredStatus(context, uri, message);
    }

    public static Intent getIntent(Context packageContext, Uri uri) {
	Intent intent = new Intent(DeliveredStatusReceiver.DELIVERED_ACTION);
	intent.setData(uri);
	intent.setClass(packageContext, DeliveredStatusReceiver.class);

	return intent;
    }
}
