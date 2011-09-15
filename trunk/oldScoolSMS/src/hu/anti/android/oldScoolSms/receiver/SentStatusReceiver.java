package hu.anti.android.oldScoolSms.receiver;

import hu.anti.android.oldScoolSms.OldScoolSMSActivity;
import hu.anti.android.oldScoolSms.R;
import hu.anti.android.oldScoolSms.Sms;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class SentStatusReceiver extends AbstractSmsBroadcastReceiver {
    public static final String SENT_ACTION = "SMS_SENT";

    @Override
    public void onReceive(Context context, Intent intent) {
	logIntent("sent", intent);

	ContentValues values = new ContentValues();
	if (Activity.RESULT_OK == getResultCode()) {
	    values.put(Sms.Fields.STATUS, Sms.Status.PENDING);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_UNDELIVERED);
	} else {
	    values.put(Sms.Fields.STATUS, Sms.Status.FAILED);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_FAILED);
	}

	// update status
	context.getContentResolver().update(intent.getData(), values, null, null);

	String status = Sms.decodeSmsSendStatus(context.getResources(), getResultCode());
	//
	// Cursor cursor = context.getContentResolver().query(intent.getData(),
	// null, null, null, null);
	// if (cursor != null && cursor.moveToFirst()) {
	// String phoneNumber =
	// cursor.getString(cursor.getColumnIndex(Sms.Fields.ADDRESS));
	// String displayName = Sms.getDisplayName(context.getContentResolver(),
	// phoneNumber);
	// status = String.format(status, displayName);
	// }
	//
	// Toast.makeText(context, status, Toast.LENGTH_LONG).show();

	String ns = Context.NOTIFICATION_SERVICE;
	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

	int icon = R.drawable.icon;
	// CharSequence tickerText = "Hello";
	long when = System.currentTimeMillis();
	Notification notification = new Notification(icon, status, when);

	CharSequence contentTitle = "SMS";
	CharSequence contentText = status;
	Intent notificationIntent = new Intent(context, OldScoolSMSActivity.class);
	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

	mNotificationManager.notify(1, notification);
    }

    public static Intent getIntent(Uri uri) {
	Intent intent = new Intent(SentStatusReceiver.SENT_ACTION);
	intent.setDataAndType(uri, "sms/*");

	return intent;
    }
}
