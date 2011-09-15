package hu.anti.android.oldScoolSms.receiver;

import hu.anti.android.oldScoolSms.OldScoolSMSActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    public static final String SMS_EXTRA_NAME = "pdus";
    private final OldScoolSMSActivity oldScoolSMSActivity;
    private boolean notifyOnNewSms = false;

    public SmsReceiver(OldScoolSMSActivity oldScoolSMSActivity) {
	super();

	this.oldScoolSMSActivity = oldScoolSMSActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
	Log.d("OldScoolSMS", "Received SMS broadcast");

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

		Log.d("OldScoolSMS", "Broadcust content: " + messages);
	    }

	    // update list
	    oldScoolSMSActivity.updateSmsList();
	}
    }

    public void setNotifyOnNewSms(boolean notifyOnNewSms) {
	this.notifyOnNewSms = notifyOnNewSms;
    }
}
