package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.AbstractSmsActivity;
import hu.anti.android.oldSchoolSms.Preferences;
import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class SentStatusReceiver extends AbstractSmsBroadcastReceiver {
    public static final String SENT_ACTION = "SMS_SENT";

    @Override
    public void onReceive(Context context, Intent intent) {
	logIntent("sent", intent);

	Uri uri = intent.getData();

	if (uri == null) {
	    Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS,
		    "Received empty intent, skipping");
	    return;
	}

	ContentValues values = new ContentValues();
	int iconId;

	if (Activity.RESULT_OK == getResultCode()) {
	    values.put(Sms.Fields.STATUS, Sms.Status.PENDING);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_UNDELIVERED);

	    iconId = R.drawable.pending_sms;
	} else {
	    values.put(Sms.Fields.STATUS, Sms.Status.FAILED);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_FAILED);

	    iconId = android.R.drawable.ic_dialog_alert;
	}

	// update status
	context.getContentResolver().update(uri, values, null, null);

	if (Activity.RESULT_OK == getResultCode()) {
	    // get preferences
	    Preferences preferences = new Preferences(
		    context.getApplicationContext());
	    boolean notifyOnSuccessfulSend = preferences
		    .getNotifyOnSuccessfulSend();

	    if (!notifyOnSuccessfulSend)
		return;
	}

	showSentStatus(context, uri, iconId);
    }

    public static Intent getIntent(Context packageContext, Uri uri) {
	Intent intent = new Intent(SentStatusReceiver.SENT_ACTION);
	intent.setData(uri);
	intent.setClass(packageContext, SentStatusReceiver.class);

	return intent;
    }
}
