package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.popup.ReceivedSmsActivity;
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
		// Get received SMS array
		Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

		for (int i = 0; i < smsExtra.length; ++i) {
		    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

		    String body = sms.getMessageBody().toString();
		    String address = sms.getOriginatingAddress();

		    Log.d("OldSchoolSMS", "Received new SMS broadcast from (" + address + ") content: " + body);

		    boolean popupOnNewSms = sharedPrefs.getBoolean("popupOnNewSms", true);

		    // display it
		    if (popupOnNewSms) {
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
}
