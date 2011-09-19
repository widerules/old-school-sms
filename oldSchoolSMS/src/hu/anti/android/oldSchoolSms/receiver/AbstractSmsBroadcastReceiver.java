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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public abstract class AbstractSmsBroadcastReceiver extends BroadcastReceiver {
    protected void logIntent(String object, Intent intent) {
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

    protected void showStatusNotification(Context context, Uri uri, String title, String message) {
	// search for sms data
	Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
	if (cursor == null || !cursor.moveToFirst()) {
	    Log.e("OldSchoolSMS", "Recived notification for non existing SMS object: [" + uri + "]");

	    return;
	}

	// read the sms
	Sms sms = Sms.parseSms(cursor);
	int notificationId = sms._id.intValue();

	// format message with name if required...
	String displayName = Sms.getDisplayName(context.getContentResolver(), sms.address);
	message = String.format(message, displayName);

	// display it
	showNotification(context, uri, title, message, notificationId, 0, SmsViewActivity.class);
    }

    protected void showNotification(Context context, Uri uri, String title, String message, int notificationId, int number, Class<?> targetClass) {
	// intent on click
	Intent intent = new Intent(Intent.ACTION_VIEW);
	intent.setData(uri);
	intent.setClass(context, targetClass);

	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

	// initialize the Notification, using the configurations above
	Notification notification = new Notification(R.drawable.icon, message, System.currentTimeMillis());
	notification.setLatestEventInfo(context.getApplicationContext(), title, message, contentIntent);
	// number of information
	notification.number = number;

	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.notify(notificationId, notification);

	Log.d("OldSchoolSMS", "Notification [" + notificationId + "] changed: " + message);
    }
}
