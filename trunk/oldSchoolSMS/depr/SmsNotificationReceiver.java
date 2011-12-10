package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.OldSchoolSMSActivity;
import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import hu.anti.android.oldSchoolSms.observer.AbstractSmsObserver;
import hu.anti.android.oldSchoolSms.observer.AllSmsObserver;
import hu.anti.android.oldSchoolSms.popup.ReceivedSmsActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class SmsNotificationReceiver extends AbstractSmsBroadcastReceiver {
    public static final String SMS_EXTRA_NAME = "pdus";

    private static AbstractSmsObserver smsObserver;

    private final Uri smsUri;

    public SmsNotificationReceiver() {
	smsUri = Uri.parse(Sms.Uris.SMS_URI_BASE);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
	Log.d("OldSchoolSMS", "Received SMS count change: " + intent);

	// get preferences
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

	boolean notifyOnNewSms = sharedPrefs.getBoolean("notifyOnNewSms", false);
	if (!notifyOnNewSms) {
	    // remove notification
	    removeNotification(context);
	    return;
	}

	if (smsObserver == null) {
	    startSmSObserver(context);
	    return;
	}

	int count = getUnreadCount(context);
	if (count == 0) {
	    // remove notification
	    removeNotification(context);

	    Log.d("OldSchoolSMS", "Removed SMS notification becasu no unread SMS");
	    return;
	}

	String title = context.getResources().getString(R.string.SMS_RECIVED_TITLE);
	String message = context.getResources().getString(R.string.SMS_RECIVED_COUNT);

	message = String.format(message, count);

	// intent on click
	Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
	notificationIntent.setData(smsUri);
	notificationIntent.setClass(context, OldSchoolSMSActivity.class);

	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

	// initialize the Notification
	Notification notification = new Notification(R.drawable.new_sms, title + ": " + message, System.currentTimeMillis());
	notification.setLatestEventInfo(context.getApplicationContext(), title, message, contentIntent);

	// number of unread SMS
	notification.number = count;

	if (ReceivedSmsActivity.NEW_SMS_ACTION.equals(intent.getAction())) {
	    // notification ring tone
	    String notificationSound = sharedPrefs.getString("notificationSound", "DEFAULT_SOUND");
	    notification.sound = Uri.parse(notificationSound);

	    // LED
	    notification.defaults |= Notification.DEFAULT_LIGHTS;
	}

	// fire it
	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.notify(1, notification);

	Log.d("OldSchoolSMS", "New SMS notification changed: " + message);
    }

    private void removeNotification(final Context context) {
	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.cancel(1);
    }

    private int getUnreadCount(Context context) {
	int count = 1;

	Uri uri = Uri.parse(Sms.Uris.SMS_URI_BASE);
	Cursor cursor = context.getContentResolver().query(uri, null,
		"(" + Sms.Fields.TYPE + " = " + Sms.Type.MESSAGE_TYPE_INBOX + " AND " + Sms.Fields.READ + " = 0)", null, null);

	if (cursor != null)
	    count = cursor.getCount();

	return count;
    }

    public static void startSmSObserver(final Context context) {
	Log.d("OldSchoolSMS", "Created new sms observer");

	// create observer
	smsObserver = new AllSmsObserver(context.getContentResolver(), new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		super.handleMessage(msg);

		Log.d("OldSchoolSMS", "SmsNotificationReceiver/AllSmsObserver received Message: " + msg);

		Intent intent = new Intent();
		intent.setClassName(context, SmsNotificationReceiver.class.getCanonicalName());

		context.sendBroadcast(intent);
	    }
	});

	// register observer
	context.getContentResolver().registerContentObserver(smsObserver.getBaseUri(), true, smsObserver);
    }
}
