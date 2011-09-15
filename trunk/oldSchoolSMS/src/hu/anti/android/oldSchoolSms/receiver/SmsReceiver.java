package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.OldSchoolSMSActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    public static final String SMS_EXTRA_NAME = "pdus";
    private final OldSchoolSMSActivity oldSchoolSMSActivity;
    private boolean notifyOnNewSms = false;

    public SmsReceiver(OldSchoolSMSActivity oldSchoolSMSActivity) {
	super();

	this.oldSchoolSMSActivity = oldSchoolSMSActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
	Log.d("OldSchoolSMS", "Received SMS broadcast");

	if (notifyOnNewSms) {
	    // Get SMS map from Intent
	    Bundle extras = intent.getExtras();

	    String messages = "";

	    if (extras != null) {
		// Get received SMS array
		Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

		for (int i = 0; i < smsExtra.length; ++i) {
		    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

		    String body = sms.getMessageBody().toString();
		    String address = sms.getOriginatingAddress();

		    messages += "SMS: " + address + " :\n";
		    messages += body + "\n";
		}

		// Display SMS message
		Toast.makeText(context, messages, Toast.LENGTH_SHORT).show();

		Log.d("OldSchoolSMS", "Broadcust content: " + messages);
	    }

	    // update list
	    oldSchoolSMSActivity.updateSmsList();
	}
    }

    public void setNotifyOnNewSms(boolean notifyOnNewSms) {
	this.notifyOnNewSms = notifyOnNewSms;
    }
}
