package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.popup.ReceivedSmsActivity;

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

		Map<String, String> receivedSms = new HashMap<String, String>();

		for (int i = 0; i < smsExtra.length; ++i) {
		    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

		    String address = sms.getOriginatingAddress();
		    String body = sms.getMessageBody().toString();

		    Log.d("OldSchoolSMS", "Received new SMS part from (" + address + ") content: " + body);

		    if (receivedSms.containsKey(address))
			receivedSms.put(address, receivedSms.get(address) + body);
		    else
			receivedSms.put(address, body);
		}
		boolean popupOnNewSms = sharedPrefs.getBoolean("popupOnNewSms", true);

		// display it
		if (popupOnNewSms) {
		    for (Entry<String, String> entry : receivedSms.entrySet()) {
			String address = entry.getKey();
			String body = entry.getValue();

			Log.d("OldSchoolSMS", "Received new SMS from (" + address + ") content: " + body);

			Intent newIntent = new Intent(ReceivedSmsActivity.NEW_SMS_ACTION);
			newIntent.setClassName(context, ReceivedSmsActivity.class.getCanonicalName());
			newIntent.putExtra(ReceivedSmsActivity.INTENT_SMS_BODY, body);
			newIntent.putExtra(ReceivedSmsActivity.INTENT_SMS_ADDRESS, address);

			newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(newIntent);

			Log.d("OldSchoolSMS", "Started new popup: " + newIntent);
		    }
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
