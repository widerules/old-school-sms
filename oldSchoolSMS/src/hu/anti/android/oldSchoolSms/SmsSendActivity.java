package hu.anti.android.oldSchoolSms;

import hu.anti.android.oldSchoolSms.receiver.AbstractSmsBroadcastReceiver;

import java.net.URLDecoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SmsSendActivity extends AbstractSmsActivity {

    private static final int PERSON_SELECTION = 1;
    private boolean dataFilled = false;
    private boolean smsProcessed = false;

    private String toNumber = null;

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
		// get message
		TextView messageText = (TextView) findViewById(R.id.messageText);
		String body = messageText.getText().toString();

		if (body == null || body.length() == 0) {
		    Toast.makeText(SmsSendActivity.this.getApplicationContext(), "Empty SMS body!", Toast.LENGTH_SHORT).show();
		    return;
		}

		// get address
		// TextView toNumber = (TextView) findViewById(R.id.toNumber);
		// String address = toNumber.getText().toString();

		if (toNumber == null || toNumber.length() == 0) {
		    Toast.makeText(SmsSendActivity.this.getApplicationContext(), "Empty SMS address!", Toast.LENGTH_SHORT).show();
		    return;
		}

		deleteDraft();

		// send it
		Intent popupIntent = new Intent(NotificationService.ACTION_SEND_SMS, null, SmsSendActivity.this, NotificationService.class);
		popupIntent.putExtra(NotificationService.EXTRA_ADDRESS, toNumber);
		popupIntent.putExtra(NotificationService.EXTRA_BODY, body);

		startService(popupIntent);

		// mark sent status
		smsProcessed = true;

		// close activity
		finish();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// select button
	Button personSelectButton = (Button) findViewById(R.id.personSelectButton);
	personSelectButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		// ask for number
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
		startActivityForResult(intent, PERSON_SELECTION);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// toNumber
	final TextView toNumberView = (TextView) findViewById(R.id.toNumber);
	toNumberView.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(SmsSendActivity.this);

		// Set an EditText view to get user input
		final EditText input = new EditText(SmsSendActivity.this);
		input.setSingleLine();
		input.setText(toNumber);

		dialog.setView(input);

		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			String value = input.getText().toString();

			// store the number
			toNumber = value;

			// set text to display
			toNumberView.setText(Sms.getDisplayName(getContentResolver(), value));
		    }
		});

		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			dialog.dismiss();
		    }
		});

		dialog.show();
	    }
	});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	AbstractSmsBroadcastReceiver.logIntent("sms send activity", intent);

	switch (requestCode) {
	case PERSON_SELECTION:
	    if (resultCode == Activity.RESULT_OK) {
		Uri contactData = intent.getData();
		Cursor cursor = getContentResolver().query(contactData, null, null, null, null);

		if (cursor.moveToFirst()) {
		    int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

		    if (columnIndex > -1) {
			// url decode...
			String number = cursor.getString(columnIndex);
			if (number.contains("%"))
			    number = URLDecoder.decode(number);

			toNumber = number;

			setText(R.id.toNumber, Sms.getDisplayName(getContentResolver(), number));
		    }
		}

		if (cursor != null)
		    cursor.close();
	    }
	    break;

	default:
	    super.onActivityResult(requestCode, resultCode, intent);
	    break;
	}
    }

    @Override
    protected void onResume() {
	super.onResume();

	// ////////////////////////////////////////////////////////////
	// ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.addView(this, layout);

	if (dataFilled)
	    return;

	dataFilled = true;

	Intent intent = getIntent();
	String action = intent.getAction();
	Uri data = intent.getData();

	Log.d("OldSchoolSMS", "Received " + action + " with content: " + data);
	AbstractSmsBroadcastReceiver.logIntent(this.getClass().getCanonicalName(), intent);

	if (Intent.ACTION_SENDTO.equals(action)) {
	    // send to/replay
	    String scheme = intent.getScheme();
	    // "smsto:" form
	    String number = intent.getDataString().substring(scheme.length() + 1);

	    // url decode it...
	    if (number.contains("%"))
		number = URLDecoder.decode(number);

	    toNumber = number;

	    setText(R.id.toNumber, Sms.getDisplayName(getContentResolver(), number));
	} else if (Intent.ACTION_SEND.equals(action)) {
	    if (data != null) {
		// send/forward
		Sms sms = Sms.getSms(getContentResolver(), data);

		if (Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type)) {
		    toNumber = sms.address;
		    setText(R.id.toNumber, Sms.getDisplayName(getContentResolver(), sms.address));
		}

		setText(R.id.messageText, sms.body);
	    }
	} else {
	    Toast.makeText(getApplicationContext(), "Received not supported action: " + action, Toast.LENGTH_LONG).show();
	}
    }

    @Override
    protected void onPause() {
	super.onPause();

	// ////////////////////////////////////////////////////////////
	// ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.removeView(this, layout);

	if (!smsProcessed) {
	    // get message
	    TextView messageText = (TextView) findViewById(R.id.messageText);
	    String body = messageText.getText().toString();

	    if (body != null && body.length() != 0) {

		Intent intent = getIntent();
		String action = intent.getAction();
		Uri data = intent.getData();

		if (Intent.ACTION_SEND.equals(action) && data != null) {
		    Sms sms = Sms.getSms(getContentResolver(), data);

		    // update existing draft
		    if (Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type)) {

			ContentValues values = new ContentValues();
			values.put(Sms.Fields.ADDRESS, toNumber);
			values.put(Sms.Fields.BODY, body);

			getContentResolver().update(data, values, null, null);

			Log.d("OldSchoolSMS", "onPause updated SMS uri: " + data + " getScheme: [" + data.getScheme() + "]");
			return;
		    }
		}

		// put to database the new draft sms
		Uri draftUri = NotificationService.putNewSmsToDatabase(getContentResolver(), toNumber, body, Sms.Type.MESSAGE_TYPE_DRAFT, Sms.Status.NONE);
		Log.d("OldSchoolSMS", "onPause SMS uri: " + draftUri + " getScheme: [" + draftUri.getScheme() + "]");

		// use new intent for this draft
		Intent draftIntent = new Intent(Intent.ACTION_SEND, draftUri);
		setIntent(draftIntent);
	    }
	}
    }

    /************************************************
     * Activity - OptionsMenu
     ************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.sms_send, menu);

	return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	MenuItem deleteMenuItem = menu.findItem(R.id.delete);

	// default disable
	deleteMenuItem.setEnabled(false);

	Uri uri = getIntent().getData();
	if (uri != null) {
	    Sms sms = Sms.getSms(getContentResolver(), uri);
	    if (sms != null)
		// enable it only if draft...
		deleteMenuItem.setEnabled(Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type));
	}

	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Uri uri = getIntent().getData();

	// Handle item selection
	switch (item.getItemId()) {

	case R.id.cancel:
	    smsProcessed = true;
	    finish();
	    return true;

	case R.id.delete:
	    smsProcessed = true;
	    deleteDraft();
	    finish();
	    return true;

	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    /************************************************
     * functions
     ************************************************/

    private void deleteDraft() {
	// handle draft
	Intent intent = getIntent();
	String action = intent.getAction();
	Uri data = intent.getData();
	// check draft source...
	if (Intent.ACTION_SEND.equals(action) && data != null) {
	    Sms sms = Sms.getSms(getContentResolver(), data);

	    if (Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type)) {
		// if from a draft, first delete it
		getContentResolver().delete(data, null, null);
	    }
	}
    }
}
