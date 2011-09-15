package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import android.app.Activity;
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

	// notify the user
	String message = Sms.decodeSmsSendStatus(context.getResources(), getResultCode());
	showNotification(context, intent.getData(), context.getResources().getString(R.string.SMS_SENT_TITLE), message);
    }

    public static Intent getIntent(Uri uri) {
	Intent intent = new Intent(SentStatusReceiver.SENT_ACTION);
	intent.setDataAndType(uri, "sms/*");

	return intent;
    }
}
