package hu.anti.android.oldSchoolSms.receiver;

import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DeliveredStatusReceiver extends AbstractSmsBroadcastReceiver {
    public static final String DELIVERED_ACTION = "SMS_DELIVERED";

    @Override
    public void onReceive(Context context, Intent intent) {
	logIntent("delivered", intent);

	String message;
	ContentValues values = new ContentValues();
	switch (getResultCode()) {
	case Activity.RESULT_OK:
	    values.put(Sms.Fields.STATUS, Sms.Status.COMPLETE);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_SENT);

	    message = context.getResources().getString(R.string.SMS_DELIVERED_RESULT_OK);
	    break;
	case Activity.RESULT_CANCELED:
	    values.put(Sms.Fields.STATUS, Sms.Status.FAILED);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_FAILED);

	    message = context.getResources().getString(R.string.SMS_DELIVERED_RESULT_FAILED);
	    break;

	default:
	    message = "" + getResultCode();
	    break;
	}

	// update status
	context.getContentResolver().update(intent.getData(), values, null, null);

	// notify the user
	showStatusNotification(context, intent.getData(), context.getResources().getString(R.string.SMS_DELIVERED_TITLE), message);
    }

    public static Intent getIntent(Context packageContext, Uri uri) {
	Intent intent = new Intent(DeliveredStatusReceiver.DELIVERED_ACTION);
	intent.setData(uri);
	intent.setClass(packageContext, DeliveredStatusReceiver.class);

	return intent;
    }
}
