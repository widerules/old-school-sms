package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.popup.ReceivedSmsActivity;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.Pair;

public class NewSmsPopupReceiver extends AbstractSmsBroadcastReceiver {

    public static final String SMS_EXTRA_NAME = "pdus";

    @Override
    public void onReceive(final Context context, Intent intent) {
	Log.d("OldSchoolSMS", "Received new SMS broadcast: " + intent);
	logIntent("NewSmsPopupReceiver", intent);

	// get preferences
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

	boolean notifyOnNewSms = sharedPrefs.getBoolean("notifyOnNewSms", true);
	if (!notifyOnNewSms) {
	    return;
	}

	// trigger the notifier
	Intent newIntent = new Intent(ReceivedSmsActivity.NEW_SMS_ACTION);
	newIntent.setClassName(context, SmsNotificationReceiver.class.getCanonicalName());

	context.sendBroadcast(newIntent);

	boolean popupOnNewSms = sharedPrefs.getBoolean("popupOnNewSms", true);
	if (!popupOnNewSms) {
	    return;
	}

	if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {

	    // Get SMS map from Intent
	    Bundle extras = intent.getExtras();

	    if (extras != null) {
		Log.d("OldSchoolSMS", "Received new SMS broadcast/extras: " + extras);
		if (extras != null) {
		    for (String key : extras.keySet()) {
			Log.d("OldSchoolSMS", "NewSmsPopupReceiver/Bundle extras/" + key + ": " + extras.get(key));
		    }
		}

		// Get received SMS array
		Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

		Map<String, Pair<String, Long>> receivedSms = new HashMap<String, Pair<String, Long>>();

		for (int i = 0; i < smsExtra.length; ++i) {
		    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

		    String address = sms.getOriginatingAddress();
		    String body = sms.getMessageBody().toString();
		    long timestamp = sms.getTimestampMillis();

		    Log.d("OldSchoolSMS", "Received new SMS part at " + timestamp + " from (" + address + ") content: " + body);

		    // store sms text
		    if (receivedSms.containsKey(address))
			body = receivedSms.get(address).first + body;

		    // store sms
		    receivedSms.put(address, new Pair<String, Long>(body, timestamp));
		}

		// display it
		for (Entry<String, Pair<String, Long>> entry : receivedSms.entrySet()) {
		    String address = entry.getKey();
		    String body = entry.getValue().first;
		    Long timestamp = entry.getValue().second;

		    Log.d("OldSchoolSMS", "Received new SMS at [" + timestamp + "/" + new SimpleDateFormat("yyyy.MM.dd. HH.mm").format(timestamp) + "] from ("
			    + address + ") content: " + body);

		    Intent popupIntent = new Intent(ReceivedSmsActivity.NEW_SMS_ACTION, null, context, ReceivedSmsActivity.class);

		    // set data
		    popupIntent.putExtra(ReceivedSmsActivity.INTENT_SMS_BODY, body);
		    popupIntent.putExtra(ReceivedSmsActivity.INTENT_SMS_TIMESTAMP, timestamp);
		    popupIntent.putExtra(ReceivedSmsActivity.INTENT_SMS_ADDRESS, address);

		    popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		    context.startActivity(popupIntent);

		    Log.d("OldSchoolSMS", "Started new popup: " + popupIntent);
		}
	    }
	}
    }

    public static void logIntent(String object, Intent intent) {
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
}
