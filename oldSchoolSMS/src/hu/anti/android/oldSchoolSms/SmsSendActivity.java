package hu.anti.android.oldSchoolSms;

import hu.anti.android.oldSchoolSms.receiver.DeliveredStatusReceiver;
import hu.anti.android.oldSchoolSms.receiver.SentStatusReceiver;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SmsSendActivity extends AbstractSmsActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.sms_send);

	// ////////////////////////////////////////////////////////////
	// send button
	Button sendButton = (Button) findViewById(R.id.sendButton);
	sendButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		doSend();
		finish();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// send button
	Button personSelectButton = (Button) findViewById(R.id.personSelectButton);
	personSelectButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
		startActivityForResult(intent, 1);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.addView(this, layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	switch (requestCode) {
	case 1:
	    if (resultCode == Activity.RESULT_OK) {
		Uri contactData = data.getData();
		Cursor cursor = managedQuery(contactData, null, null, null, null);

		if (cursor.moveToFirst()) {
		    int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

		    if (columnIndex > -1) {
			String number = cursor.getString(columnIndex);

			setText(R.id.toNumber, number);
		    }
		}
	    }
	    break;

	default:
	    super.onActivityResult(requestCode, resultCode, data);
	    break;
	}
    }

    @Override
    protected void onResume() {
	super.onResume();

	Intent intent = getIntent();
	String action = intent.getAction();
	Uri data = intent.getData();

	Log.d("OldSchoolSMS", "Received " + action + " with content: " + data);

	if (Intent.ACTION_SENDTO.equals(action)) {
	    // send to/replay
	    String scheme = intent.getScheme();
	    // "smsto:" form
	    String number = intent.getDataString().substring(scheme.length() + 1);

	    setText(R.id.toNumber, number);
	} else if (Intent.ACTION_SEND.equals(action)) {
	    if (data != null) {
		// send/forward
		Sms sms = Sms.getSms(getContentResolver(), data);

		setText(R.id.messageText, sms.body);
	    }
	} else {
	    Toast.makeText(getApplicationContext(), "Received not supported action: " + action, Toast.LENGTH_LONG).show();
	}
    }

    /************************************************
     * functions
     ************************************************/

    protected void doSend() {
	try {
	    SmsManager smsManager = SmsManager.getDefault();

	    // get message
	    TextView messageText = (TextView) findViewById(R.id.messageText);
	    String body = messageText.getText().toString();

	    // get address
	    TextView toNumber = (TextView) findViewById(R.id.toNumber);
	    String address = toNumber.getText().toString();

	    // put to database
	    Uri uri = putNewSmsToDatabase(getContentResolver(), address, body, Sms.Type.MESSAGE_TYPE_OUTBOX, Sms.Status.NONE);

	    Log.d("OldSchoolSMS", "doSend SMS uri: " + uri + " getScheme: [" + uri.getScheme() + "]");

	    // create sent listener
	    PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, SentStatusReceiver.getIntent(getApplicationContext(), uri), 0);

	    // create delivery listener
	    PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0, DeliveredStatusReceiver.getIntent(getApplicationContext(), uri),
		    0);

	    // split message
	    ArrayList<String> dividedMessage = smsManager.divideMessage(body);

	    // fill listener arrays
	    ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
	    ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
	    for (int i = 0; i < dividedMessage.size(); i++) {
		sentIntents.add(sentPI);
		deliveryIntents.add(deliveredPI);
	    }

	    // execute send
	    smsManager.sendMultipartTextMessage(address, null, dividedMessage, sentIntents, deliveryIntents);
	} catch (Exception e) {
	    showError(e);
	}
    }
}
