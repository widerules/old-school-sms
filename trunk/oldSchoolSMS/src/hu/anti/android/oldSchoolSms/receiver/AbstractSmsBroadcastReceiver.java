package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import hu.anti.android.oldSchoolSms.SmsViewActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class AbstractSmsBroadcastReceiver extends BroadcastReceiver {
    public static void logIntent(String object, Intent intent) {
	if (intent == null)
	    return;

	Bundle extras = intent.getExtras();

	Log.d("OldSchoolSMS", object + "/intent: " + intent);
	Log.d("OldSchoolSMS", object + "/intent getAction: " + intent.getAction());
	Log.d("OldSchoolSMS", object + "/intent getData: " + intent.getData());
	Log.d("OldSchoolSMS", object + "/intent getDataString: " + intent.getDataString());
	Log.d("OldSchoolSMS", object + "/intent getExtras: " + (extras == null ? "<>" : extras.size()));
	if (extras != null) {
	    for (String key : extras.keySet()) {
		Log.d("OldSchoolSMS", object + "/intent extras/" + key + ": " + extras.get(key));
	    }
	}
    }

    protected String updateMessageString(Context context, Uri uri, String message) {
	// search for sms data
	Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
	if (cursor == null) {
	    Log.e("OldSchoolSMS", "Recived notification for non existing SMS object: [" + uri + "]");

	    return message;
	} else if (!cursor.moveToFirst()) {
	    Log.e("OldSchoolSMS", "Recived notification for no SMS object: [" + uri + "]");

	    cursor.close();
	    return message;
	}

	// read the sms
	Sms sms = Sms.parseSms(cursor);
	cursor.close();

	// format message with name if required...
	String displayName = Sms.getDisplayName(context.getContentResolver(), sms.address);
	return String.format(message, displayName);
    }

    protected void showSentStatus(Context context, Uri uri) {
	// notify the user
	String message = Sms.decodeSmsSendStatus(context.getResources(), getResultCode());
	String title = context.getResources().getString(R.string.SMS_SENT_TITLE);

	// fill message text with data
	message = updateMessageString(context, uri, message);

	int notificationId = Integer.parseInt(uri.getLastPathSegment());

	int iconId = R.drawable.pending_sms;

	// display it
	showNotification(context, uri, iconId, title, message, notificationId, 0, SmsViewActivity.class, null, null);
    }

    protected void showDeliveredStatus(Context context, Uri uri, String message) {
	// notify the user
	String title = context.getResources().getString(R.string.SMS_DELIVERED_TITLE);

	// fill message text with data
	message = updateMessageString(context, uri, message);

	int notificationId = Integer.parseInt(uri.getLastPathSegment());

	int iconId = R.drawable.icon;

	// get preferences
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

	// get sound
	String deliverySound = sharedPrefs.getString("deliverySound", "DEFAULT_SOUND");
	Uri soundUri = Uri.parse(deliverySound);

	// get vibration
	String deliveryVibratorPattern = sharedPrefs.getString("deliveryVibratorPattern", "333,333,333");
	long[] vibratorPattern = decodeVibratorString(deliveryVibratorPattern);

	// display it
	showNotification(context, uri, iconId, title, message, notificationId, 0, SmsViewActivity.class, vibratorPattern, soundUri);
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

    protected void showNotification(Context context, Uri uri, int iconId, String title, String message, int notificationId, int count, Class<?> targetClass,
	    long[] vibratorPattern, Uri soundUri) {
	// intent on click
	Intent intent = new Intent(Intent.ACTION_VIEW);
	intent.setData(uri);
	intent.setClass(context, targetClass);

	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

	// initialize the Notification, using the configurations above
	Notification notification = new Notification(iconId, title + ": " + message, System.currentTimeMillis());
	notification.setLatestEventInfo(context.getApplicationContext(), title, message, contentIntent);

	// number of information
	notification.number = count;

	// vibration
	notification.vibrate = vibratorPattern;

	// sound
	notification.sound = soundUri;

	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.notify(notificationId, notification);

	Log.d("OldSchoolSMS", "Notification [" + notificationId + "] changed: " + message);
    }
}
