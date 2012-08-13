package hu.anti.android.oldSchoolSms;

import hu.anti.android.oldSchoolSms.receiver.DeliveredStatusReceiver;
import hu.anti.android.oldSchoolSms.receiver.SentStatusReceiver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class NotificationActivity extends AbstractSmsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// TODO Auto-generated method stub
    }

    @Override
    protected void onResume() {
	super.onResume();

	Intent intent = getIntent();
	String action = intent.getAction();

	Log.d("OldSchoolSMS", "NotificationActivity received " + intent);

	Cursor cursor = getApplicationContext().getContentResolver().query(intent.getData(), null, null, null, null);
	Sms sms;
	if (cursor == null || !cursor.moveToFirst()) {
	    Toast.makeText(getApplicationContext(), "Received not supported action: " + action, Toast.LENGTH_LONG).show();
	    return;
	}
	sms = Sms.parseSms(cursor);

	String message = "";
	if (SentStatusReceiver.SENT_ACTION.equals(action)) {
	    message = "sent: " + sms.status;
	} else if (DeliveredStatusReceiver.DELIVERED_ACTION.equals(action)) {
	    message = "delivered: " + sms.status;
	} else {
	    Toast.makeText(getApplicationContext(), "Received not supported action: " + action, Toast.LENGTH_LONG).show();
	    return;
	}

	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

	finish();
    }

    @Override
    protected void onPause() {
	super.onPause();
	// TODO Auto-generated method stub
    }
}
