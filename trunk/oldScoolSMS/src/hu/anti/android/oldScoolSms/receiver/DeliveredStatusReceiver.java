package hu.anti.android.oldScoolSms.receiver;

import hu.anti.android.oldScoolSms.Sms;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

public class DeliveredStatusReceiver extends AbstractSmsBroadcastReceiver {
    public static final String DELIVERED_ACTION = "SMS_DELIVERED";

    @Override
    public void onReceive(Context context, Intent intent) {
	logIntent("delivered", intent);

	String messsage;
	ContentValues values = new ContentValues();
	switch (getResultCode()) {
	case Activity.RESULT_OK:
	    values.put(Sms.Fields.STATUS, Sms.Status.COMPLETE);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_SENT);

	    messsage = "SMS delivered";
	    break;
	case Activity.RESULT_CANCELED:
	    values.put(Sms.Fields.STATUS, Sms.Status.FAILED);
	    values.put(Sms.Fields.TYPE, Sms.Type.MESSAGE_TYPE_FAILED);

	    messsage = "SMS not delivered";
	    break;

	default:
	    messsage = "";
	    break;
	}

	Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);
	if (cursor != null && cursor.moveToFirst()) {
	    String phoneNumber = cursor.getString(cursor.getColumnIndex(Sms.Fields.ADDRESS));
	    String displayName = Sms.getDisplayName(context.getContentResolver(), phoneNumber);
	    messsage = String.format(messsage, displayName);
	}

	Toast.makeText(context, messsage, Toast.LENGTH_SHORT).show();

	context.getContentResolver().update(intent.getData(), values, null, null);
    }

    public static Intent getIntent(Uri uri) {
	Intent intent = new Intent(DeliveredStatusReceiver.DELIVERED_ACTION);
	intent.setDataAndType(uri, "sms/*");

	return intent;
    }
}
