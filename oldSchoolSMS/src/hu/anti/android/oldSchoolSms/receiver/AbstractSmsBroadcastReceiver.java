package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.AbstractSmsActivity;
import hu.anti.android.oldSchoolSms.Preferences;
import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import hu.anti.android.oldSchoolSms.SmsViewActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public abstract class AbstractSmsBroadcastReceiver extends BroadcastReceiver {
    public static void logIntent(String object, Intent intent) {
	if (intent == null)
	    return;

	Bundle extras = intent.getExtras();

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, object + "/intent: " + intent);
	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, object
		+ "/intent getAction: " + intent.getAction());
	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, object + "/intent getData: "
		+ intent.getData());
	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, object
		+ "/intent getDataString: " + intent.getDataString());
	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS,
		object + "/intent getExtras: "
			+ (extras == null ? "<>" : extras.size()));
	if (extras != null) {
	    for (String key : extras.keySet()) {
		Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, object
			+ "/intent extras/" + key + ": " + extras.get(key));
	    }
	}
    }

    protected void showSentStatus(Context context, Uri uri, int iconId) {
	// notify the user
	String message = Sms.decodeSmsSendStatus(context.getResources(),
		getResultCode());
	String title = context.getResources()
		.getString(R.string.SMS_SENT_TITLE);

	int notificationId = Integer.parseInt(uri.getLastPathSegment());

	// display it
	showNotification(context, uri, iconId, title, message, notificationId,
		0, SmsViewActivity.class, null, null);
    }

    protected void showDeliveredStatus(Context context, Uri uri,
	    String message, int iconId) {
	// notify the user
	String title = context.getResources().getString(
		R.string.SMS_DELIVERED_TITLE);

	int notificationId = Integer.parseInt(uri.getLastPathSegment());

	// get preferences
	Preferences preferences = new Preferences(
		context.getApplicationContext());

	// get sound
	String deliverySound = preferences.getDeliverySound();
	Uri soundUri = Uri.parse(deliverySound);

	// get vibration
	long[] vibratorPattern = preferences.getDeliveryVibratorPattern();

	// display it
	showNotification(context, uri, iconId, title, message, notificationId,
		0, SmsViewActivity.class, vibratorPattern, soundUri);
    }

    protected void showNotification(final Context context, Uri uri,
	    final int iconId, final String title, final String message,
	    final int notificationId, final int count, Class<?> targetClass,
	    final long[] vibratorPattern, final Uri soundUri) {
	// intent on click
	Intent intent = new Intent(Intent.ACTION_VIEW);
	intent.setData(uri);
	intent.setClass(context, targetClass);

	final PendingIntent contentIntent = PendingIntent.getActivity(context,
		0, intent, 0);

	final Sms sms = findSms(context, uri);

	if (sms != null) {

	    AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(
		    context.getContentResolver()) {

		@Override
		protected void onQueryComplete(int token, Object cookie,
			Cursor cursor) {
		    String personName = null;

		    if (cursor != null && cursor.moveToFirst())
			personName = cursor.getString(0);
		    else
			// default...
			personName = "(" + sms.address + ")";

		    displayNotification(context, iconId, title,
			    String.format(message, personName), notificationId,
			    count, vibratorPattern, soundUri, contentIntent);
		}
	    };
	    Sms.getDisplayName(asyncQueryHandler, sms.address);
	    return;
	}

	displayNotification(context, iconId, title, message, notificationId,
		count, vibratorPattern, soundUri, contentIntent);
    }

    private void displayNotification(Context context, int iconId, String title,
	    String message, int notificationId, int count,
	    long[] vibratorPattern, Uri soundUri, PendingIntent contentIntent) {
	// initialize the Notification, using the configurations above
	Notification notification = new Notification();
	notification.icon = iconId;
	notification.tickerText = title + ": " + message;
	notification.when = System.currentTimeMillis();

	// FIXME
	notification.setLatestEventInfo(context.getApplicationContext(), title,
		message, contentIntent);
	notification.contentIntent = contentIntent;

	// number of information
	notification.number = count;

	// vibration
	notification.vibrate = vibratorPattern;

	// sound
	notification.sound = soundUri;

	NotificationManager mNotificationManager = (NotificationManager) context
		.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.notify(notificationId, notification);

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, "Notification ["
		+ notificationId + "] changed: " + message);
    }

    private Sms findSms(Context context, Uri uri) {
	// search for sms data
	Sms sms;
	Cursor cursor = context.getContentResolver().query(uri, null, null,
		null, null);
	if (cursor == null) {
	    Log.e(AbstractSmsActivity.OLD_SCHOOL_SMS,
		    "Recived notification for non existing SMS object: [" + uri
			    + "]");

	    sms = null;
	} else if (!cursor.moveToFirst()) {
	    Log.e(AbstractSmsActivity.OLD_SCHOOL_SMS,
		    "Recived notification for no SMS object: [" + uri + "]");

	    cursor.close();
	    sms = null;
	} else {
	    // read the sms
	    sms = Sms.parseSms(cursor);
	}
	cursor.close();
	return sms;
    }
}
