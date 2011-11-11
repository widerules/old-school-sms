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

public class DeliveredStatusReceiver extends AbstractSmsBroadcastReceiver {
    public static final String DELIVERED_ACTION = "SMS_DELIVERED";

    @Override
    public void onReceive(Context context, Intent intent) {
	logIntent("delivered", intent);

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
	context.getContentResolver().update(intent.getData(), values, null, null);

	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	if (Activity.RESULT_OK == getResultCode()) {
	    // get preferences
	    boolean notifyOnDelivery = sharedPrefs.getBoolean("notifyOnDelivery", true);

	    if (!notifyOnDelivery)
		return;
	}

	// get sound
	String deliverySound = sharedPrefs.getString("deliverySound", "DEFAULT_SOUND");
	Uri soundUri = Uri.parse(deliverySound);

	// get vibration
	String deliveryVibratorPattern = sharedPrefs.getString("deliveryVibratorPattern", "333,333,333");
	long[] vibratorPattern = decodeVibratorString(deliveryVibratorPattern);

	// notify the user
	String title = context.getResources().getString(R.string.SMS_DELIVERED_TITLE);
	showStatusNotification(context, intent.getData(), title, message, vibratorPattern, soundUri);
    }

    public static Intent getIntent(Context packageContext, Uri uri) {
	Intent intent = new Intent(DeliveredStatusReceiver.DELIVERED_ACTION);
	intent.setData(uri);
	intent.setClass(packageContext, DeliveredStatusReceiver.class);

	return intent;
    }

    private long[] decodeVibratorString(String vibratorPattern) {
	// extract pattern
	String[] split = vibratorPattern.split("[,.;/\\- ]");

	// create vibration pattern holder
	long[] pattern = new long[(split.length + 1)];

	// set starting wait to 0ms
	pattern[0] = 0;
	// decode pattern string
	for (int i = 0; i < split.length; i++) {
	    pattern[i + 1] = Long.parseLong(split[i]);
	}

	return pattern;
    }
}
