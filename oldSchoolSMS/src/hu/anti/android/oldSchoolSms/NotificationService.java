package hu.anti.android.oldSchoolSms;

import hu.anti.android.oldSchoolSms.receiver.DeliveredStatusReceiver;
import hu.anti.android.oldSchoolSms.receiver.SentStatusReceiver;

import java.util.ArrayList;
import java.util.Date;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

public class NotificationService extends IntentService {
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    public static final String EXTRA_BODY = "EXTRA_BODY";

    public static final String ACTION_UPDATE_SMS_NOTIFICATIONS = "ACTION_UPDATE_SMS_NOTIFICATIONS";
    public static final String ACTION_SEND_SMS = "ACTION_SEND_SMS";
    public static final String ACTION_MARK_AS_READ_SMS = "ACTION_MARK_AS_READ_SMS";

    public NotificationService() {
	super("OldSchoolSms-NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	Log.d("OldSchoolSMS", "onHandleIntent: [" + intent + "]");

	String action = intent.getAction();
	Bundle extras = intent.getExtras();

	if (ACTION_UPDATE_SMS_NOTIFICATIONS.equals(action)) {
	    updateSmsNotifications();
	} else if (ACTION_SEND_SMS.equals(action)) {
	    String address = extras.getString(EXTRA_ADDRESS);
	    String body = extras.getString(EXTRA_BODY);

	    sendSms(address, body);
	} else if (ACTION_MARK_AS_READ_SMS.equals(action)) {
	    String address = extras.getString(EXTRA_ADDRESS);
	    String body = extras.getString(EXTRA_BODY);

	    markAsRead(address, body);
	}
    }

    /************************************************
     * ACTION_SEND_SMS
     ************************************************/

    protected void sendSms(String address, String body) {
	// put to database the sent sms
	Uri uri = putNewSmsToDatabase(getContentResolver(), address, body, Sms.Type.MESSAGE_TYPE_OUTBOX, Sms.Status.NONE);

	Log.d("OldSchoolSMS", "doSend SMS uri: " + uri + " getScheme: [" + uri.getScheme() + "]");

	// create sent listener
	PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, SentStatusReceiver.getIntent(getApplicationContext(), uri), 0);

	// create delivery listener
	PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0, DeliveredStatusReceiver.getIntent(getApplicationContext(), uri), 0);

	SmsManager smsManager = SmsManager.getDefault();

	// split message
	ArrayList<String> dividedMessage = smsManager.divideMessage(body);

	// fill listener arrays
	ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
	ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
	for (int i = 0; i < dividedMessage.size(); i++) {
	    sentIntents.add(sentPI);
	    deliveryIntents.add(deliveredPI);
	}

	// execute send
	smsManager.sendMultipartTextMessage(address, null, dividedMessage, sentIntents, deliveryIntents);
    }

    public static Uri putNewSmsToDatabase(ContentResolver contentResolver, String address, String body, String type, String status) {
	// Create SMS row
	ContentValues values = new ContentValues();

	values.put(Sms.Fields.ADDRESS, address);
	values.put(Sms.Fields.DATE, "" + new Date().getTime());
	values.put(Sms.Fields.READ, Sms.Other.MESSAGE_IS_READ);
	values.put(Sms.Fields.STATUS, status);
	values.put(Sms.Fields.TYPE, type);
	values.put(Sms.Fields.BODY, body);

	// Push row into the SMS table
	Uri uri = contentResolver.insert(Uri.parse(Sms.Uris.SMS_URI_BASE), values);
	// FIXME reupdate...
	contentResolver.update(uri, values, null, null);

	Cursor cursor = contentResolver.query(uri, null, null, null, null);
	if (cursor != null && cursor.moveToFirst()) {
	    Sms sms = Sms.parseSms(cursor);
	    Log.d("OldSchoolSMS", "Inserted new SMS objet: [" + sms + "]");

	    cursor.close();
	}

	return uri;
    }

    /************************************************
     * ACTION_UPDATE_SMS_NOTIFICATIONS
     ************************************************/

    protected void updateSmsNotifications() {
	Context context = getApplicationContext();

	int count = getUnreadCount(context);
	if (count == 0) {
	    // remove notification
	    removeNotification(context, 1);

	    Log.d("OldSchoolSMS", "Removed SMS notification becasu no unread SMS");
	    return;
	}

	String title = context.getResources().getString(R.string.SMS_RECIVED_TITLE);
	String message = context.getResources().getString(R.string.SMS_RECIVED_COUNT);

	message = String.format(message, count);

	// intent on click
	Intent notificationIntent = new Intent(Intent.ACTION_VIEW);

	Uri smsUri = Sms.findLastUnread(getContentResolver());
	notificationIntent.setData(smsUri);
	notificationIntent.setClass(context, SmsViewActivity.class);

	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

	// initialize the Notification
	Notification notification = new Notification(R.drawable.new_sms, title + ": " + message, System.currentTimeMillis());
	notification.setLatestEventInfo(context.getApplicationContext(), title, message, contentIntent);

	// number of unread SMS
	notification.number = count;

	// fire it
	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.notify(1, notification);

	Log.d("OldSchoolSMS", "New SMS notification changed: " + message);
    }

    public static void removeNotification(final Context context, int id) {
	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.cancel(id);
    }

    private int getUnreadCount(Context context) {
	int count = 0;

	Uri uri = Uri.parse(Sms.Uris.SMS_URI_BASE);
	Cursor cursor = context.getContentResolver().query(uri, null,
		"(" + Sms.Fields.TYPE + " = " + Sms.Type.MESSAGE_TYPE_INBOX + " AND " + Sms.Fields.READ + " = 0)", null, null);

	if (cursor != null) {
	    count = cursor.getCount();
	    cursor.close();
	}

	return count;
    }

    /************************************************
     * ACTION_MARK_AS_READ_SMS
     ************************************************/
    protected void markAsRead(String address, String body) {
	Uri smsUri = Sms.findSmsByContent(getContentResolver(), address, body);

	// update read flag
	ContentValues values = new ContentValues();
	values.put(Sms.Fields.READ, Sms.Other.MESSAGE_IS_READ);

	getContentResolver().update(smsUri, values, null, null);
    }

}
