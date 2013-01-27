package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.NotificationService;
import hu.anti.android.oldSchoolSms.Preferences;
import hu.anti.android.oldSchoolSms.observer.AbstractSmsObserver;
import hu.anti.android.oldSchoolSms.observer.AllSmsObserver;
import hu.anti.android.oldSchoolSms.popup.ReceivedSmsActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.Pair;

public class NewSmsReceiver extends AbstractSmsBroadcastReceiver {

    public static final String SMS_EXTRA_NAME = "pdus";

    private static AbstractSmsObserver smsObserver;

    @Override
    public void onReceive(final Context context, Intent intent) {
	Log.d("OldSchoolSMS", "NewSmsReceiver: Received new SMS broadcast: "
		+ intent);
	logIntent("NewSmsReceiver", intent);

	// get preferences
	Preferences preferences = new Preferences(
		context.getApplicationContext());

	boolean notifyOnNewSms = preferences.getNotifyOnNewSms();
	if (!notifyOnNewSms) {
	    // remove notification
	    NotificationService.removeNotification(context, 1);

	    // unregister observer
	    if (smsObserver != null)
		context.getContentResolver().unregisterContentObserver(
			smsObserver);

	    // finish...
	    return;
	}

	if (smsObserver == null) {
	    // create the observer
	    startSmSObserver(context);
	}

	// process action
	String action = intent.getAction();
	if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
	    // initialize the Notification
	    Notification notification = new Notification();

	    // notification ring tone
	    String notificationSound = preferences.getNotificationSound();
	    notification.sound = Uri.parse(notificationSound);

	    // LED
	    notification.defaults |= Notification.DEFAULT_LIGHTS;

	    // fire it
	    NotificationManager mNotificationManager = (NotificationManager) context
		    .getSystemService(Context.NOTIFICATION_SERVICE);
	    mNotificationManager.notify(1, notification);

	    // vibrate
	    long[] pattern = preferences.getVibratorPattern();

	    // Start the vibration
	    Vibrator vibrator = (Vibrator) context
		    .getSystemService(Context.VIBRATOR_SERVICE);
	    vibrator.vibrate(pattern, -1);

	    boolean popupOnNewSms = preferences.getPopupOnNewSms();
	    // popup the new SMS
	    if (popupOnNewSms) {
		// Get SMS map from Intent
		Bundle extras = intent.getExtras();

		if (extras != null) {
		    logExtras(extras);

		    // Get received SMS array
		    Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

		    Map<String, Pair<String, Long>> receivedSms = new HashMap<String, Pair<String, Long>>();

		    for (int i = 0; i < smsExtra.length; ++i) {
			SmsMessage sms = SmsMessage
				.createFromPdu((byte[]) smsExtra[i]);

			String address = sms.getOriginatingAddress();
			String body = sms.getMessageBody().toString();
			long timestamp = sms.getTimestampMillis();

			Log.d("OldSchoolSMS", "Received new SMS part at "
				+ timestamp + " from (" + address
				+ ") content: " + body);

			// store sms text
			if (receivedSms.containsKey(address))
			    body = receivedSms.get(address).first + body;

			// store sms
			receivedSms.put(address, new Pair<String, Long>(body,
				timestamp));
		    }

		    for (Entry<String, Pair<String, Long>> entry : receivedSms
			    .entrySet()) {
			String address = entry.getKey();
			String body = entry.getValue().first;
			Long timestamp = entry.getValue().second;

			popupSms(context, address, body, timestamp);
		    }
		}
	    }
	} else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
	    // update notification count
	    context.startService(new Intent(
		    NotificationService.ACTION_UPDATE_SMS_NOTIFICATIONS, null,
		    context, NotificationService.class));
	}
    }

    protected void popupSms(final Context context, String address, String body,
	    Long timestamp) {
	Intent popupIntent = new Intent(ReceivedSmsActivity.NEW_SMS_ACTION);
	popupIntent.setClassName(context,
		ReceivedSmsActivity.class.getCanonicalName());

	// set data
	popupIntent.putExtra(ReceivedSmsActivity.INTENT_SMS_BODY, body);
	popupIntent.putExtra(ReceivedSmsActivity.INTENT_SMS_TIMESTAMP,
		timestamp);
	popupIntent.putExtra(ReceivedSmsActivity.INTENT_SMS_ADDRESS, address);

	popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

	context.startActivity(popupIntent);

	Log.d("OldSchoolSMS", "Started new popup: " + popupIntent);
    }

    public static void startSmSObserver(final Context context) {
	Log.d("OldSchoolSMS", "NewSmsReceiver: Created new sms observer");

	// create observer
	smsObserver = new AllSmsObserver(context.getContentResolver(),
		new Handler() {
		    @Override
		    public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Log.d("OldSchoolSMS",
				"NewSmsReceiver: NewSmsReceiver/AllSmsObserver received Message: "
					+ msg);

			// update notification count
			context.startService(new Intent(
				NotificationService.ACTION_UPDATE_SMS_NOTIFICATIONS,
				null, context, NotificationService.class));
		    }
		});

	// register observer
	context.getContentResolver().registerContentObserver(
		smsObserver.getBaseUri(), true, smsObserver);
    }

    private void logExtras(Bundle extras) {
	// log
	Log.d("OldSchoolSMS",
		"NewSmsReceiver: Received new SMS broadcast/extras: " + extras);
	if (extras != null) {
	    for (String key : extras.keySet()) {
		Log.d("OldSchoolSMS", "NewSmsPopupReceiver/Bundle extras/"
			+ key + ": " + extras.get(key));
	    }
	}
    }
}
