package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.OldSchoolSMSActivity;
import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import hu.anti.android.oldSchoolSms.observer.AllSmsObserver;
import hu.anti.android.oldSchoolSms.observer.SmsObserver;
import android.app.NotificationManager;
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

    private static SmsObserver smsObserver;

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
	    Log.d("OldSchoolSMS", "Created new sms observer");

	    // create observer
	    smsObserver = new AllSmsObserver(context.getContentResolver(), new Handler() {
		@Override
		public void handleMessage(Message msg) {
		    super.handleMessage(msg);

		    Log.d("OldSchoolSMS", "Received Message: " + msg);

		    updateNotification(context);
		}
	    });

	    // register observer
	    context.getContentResolver().registerContentObserver(smsObserver.getBaseUri(), true, smsObserver);
	    return;
	}

	int count = getCount(context);

	if (count == 0) {
	    // remove notification
	    removeNotification(context);

	    Log.d("OldSchoolSMS", "Removed SMS notification becasu no unread SMS");
	    return;
	}

	Uri uri = Uri.parse(Sms.Uris.SMS_URI_BASE);
	String title = context.getResources().getString(R.string.SMS_RECIVED_TITLE);
	String message = context.getResources().getString(R.string.SMS_RECIVED_COUNT);

	message = String.format(message, count);

	// display it
	showNotification(context, uri, title, message, 1, count, OldSchoolSMSActivity.class);

	Log.d("OldSchoolSMS", "Updated unread count to: " + count);
    }

    private void removeNotification(final Context context) {
	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.cancel(1);
    }

    private int getCount(Context context) {
	int count = 1;

	Uri uri = Uri.parse(Sms.Uris.SMS_URI_BASE);
	Cursor cursor = context.getContentResolver().query(uri, null,
		"(" + Sms.Fields.TYPE + " = " + Sms.Type.MESSAGE_TYPE_INBOX + " AND " + Sms.Fields.READ + " = 0)", null, null);

	if (cursor != null)
	    count = cursor.getCount();

	return count;
    }

    public static void updateNotification(final Context context) {
	Intent intent = new Intent();
	intent.setClassName(context, SmsNotificationReceiver.class.getCanonicalName());

	context.sendBroadcast(intent);
    }
}
