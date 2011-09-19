package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.OldSchoolSMSActivity;
import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class SmsNotificationReceiver extends AbstractSmsBroadcastReceiver {
    public static final String SMS_EXTRA_NAME = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
	Log.d("OldSchoolSMS", "Received SMS count change: " + intent);

	// get preferences
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	boolean notifyOnNewSms = sharedPrefs.getBoolean("notifyOnNewSms", false);

	if (!notifyOnNewSms)
	    return;

	int count = getCount(context);

	if (count == 0) {
	    // remove notification
	    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    mNotificationManager.cancel(1);

	    return;
	}

	Uri uri = Uri.parse(Sms.Uris.SMS_URI_BASE);
	String title = context.getResources().getString(R.string.SMS_RECIVED_TITLE);
	String message = context.getResources().getString(R.string.SMS_RECIVED_COUNT);

	message = String.format(message, count);

	// display it
	showNotification(context, uri, title, message, 1, count, OldSchoolSMSActivity.class);

	Log.d("OldSchoolSMS", "Updated cout to: " + count);
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
}
