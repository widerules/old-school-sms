package hu.anti.android.oldSchoolSms.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class NewSmsReceiver extends AbstractSmsBroadcastReceiver {

    public static final String SMS_EXTRA_NAME = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
	Log.d("OldSchoolSMS", "Received new SMS broadcast: " + intent);

	// get preferences
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	boolean notifyOnNewSms = sharedPrefs.getBoolean("notifyOnNewSms", false);

	if (!notifyOnNewSms)
	    return;

	if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {

	    // Get SMS map from Intent
	    Bundle extras = intent.getExtras();

	    if (extras != null) {
		Log.d("OldSchoolSMS", "Received new SMS broadcast/extras: " + extras);
		// Get received SMS array
		Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

		for (int i = 0; i < smsExtra.length; ++i) {
		    String messages = "";

		    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

		    String body = sms.getMessageBody().toString();
		    String address = sms.getOriginatingAddress();

		    messages += "SMS: " + address + " :\n";
		    messages += body + "\n";

		    Log.d("OldSchoolSMS", "Received new SMS broadcast content: " + messages);

		    // display it
		    Toast.makeText(context, messages, Toast.LENGTH_LONG);
		    // TODO
		}
	    }
	}
    }
}
