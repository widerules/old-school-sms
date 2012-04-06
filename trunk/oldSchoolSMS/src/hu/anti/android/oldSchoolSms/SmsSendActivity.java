package hu.anti.android.oldSchoolSms;

import hu.anti.android.oldSchoolSms.receiver.AbstractSmsBroadcastReceiver;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
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
		final String body = messageText.getText().toString();

		Context context = SmsSendActivity.this.getApplicationContext();
		if (body == null || body.length() == 0) {
		    Toast.makeText(context, context.getResources().getString(R.string.emptyBody), Toast.LENGTH_SHORT).show();
		    return;
		}

		if (toNumber == null || toNumber.length() == 0) {
		    Toast.makeText(context, context.getResources().getString(R.string.emptyAddress), Toast.LENGTH_SHORT).show();
		    return;
		}

		deleteIfDraft();

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
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
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
		input.setInputType(InputType.TYPE_CLASS_PHONE);
		input.setText(toNumber);

		dialog.setView(input);

		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			String value = input.getText().toString();

			// store the number
			toNumber = value;

			// set text to display
			updateToNumber(value);
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
		// Get the Uri for the data returned by the contact picker
		// This Uri identifies the person picked
		Uri contactData = intent.getData();

		// Query the table
		Cursor contactsCursor = managedQuery(contactData, null, null, null, null);

		// If the cursor is not empty
		if (contactsCursor.moveToFirst()) {

		    // Get the id name and phone number indicator
		    String id = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
		    String name = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
		    String hasPhoneNumber = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));

		    Log.d("OldSchoolSMS", "id " + id + " name: " + name + " hasPhoneNumber: " + hasPhoneNumber);

		    // If the contact has a phone number
		    if (Integer.parseInt(hasPhoneNumber) > 0) {
			Cursor phoneCursor = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);

			final List<String> values = new ArrayList<String>();
			final List<String> items = new ArrayList<String>();

			// Get the phone numbers from the contact
			for (phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {
			    int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE));
			    String phoneLabel = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL));
			    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
			    if (phoneNumber.contains("%"))
				phoneNumber = URLDecoder.decode(phoneNumber);

			    Log.d("OldSchoolSMS", "phoneType: " + phoneType + "(" + phoneLabel + ") phoneNumber: " + phoneNumber);

			    CharSequence typeLabel;
			    if (ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM == phoneType)
				typeLabel = phoneLabel;
			    else
				typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(SmsSendActivity.this.getResources(), phoneType, "?");

			    items.add(typeLabel + ": " + phoneNumber);
			    values.add(phoneNumber);
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// builder.setTitle("Make your selection");
			builder.setItems(items.toArray(new CharSequence[items.size()]), new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
				if (which >= 0) {
				    String number = values.get(which);

				    toNumber = number;
				    updateToNumber(number);
				}
				dialog.dismiss();
			    }
			});

			builder.show();
		    } else {
			Toast.makeText(this, R.string.alertNoPhoneNumber, Toast.LENGTH_LONG).show();

			// select another contact
			startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), PERSON_SELECTION);
		    }
		}
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

	    updateToNumber(number);

	} else if (Intent.ACTION_SEND.equals(action)) {
	    if (data != null) {
		// send/forward
		Sms sms = Sms.getSms(getContentResolver(), data);

		if (sms != null) {
		    if (Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type)) {
			toNumber = sms.address;
			updateToNumber(sms.address);
		    }

		    setText(R.id.messageText, sms.body);
		}
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

	if (smsProcessed) {
	    // remove if exist a draft of it
	    deleteIfDraft();
	    // finish
	    return;
	}

	// get message
	TextView messageText = (TextView) findViewById(R.id.messageText);
	String body = messageText.getText().toString();

	Intent intent = getIntent();
	String action = intent.getAction();
	Uri dataUri = intent.getData();

	if (Intent.ACTION_SEND.equals(action) && dataUri != null) {
	    Sms sms = Sms.getSms(getContentResolver(), dataUri);

	    // update existing draft
	    if (sms != null && Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type)) {

		ContentValues values = new ContentValues();
		values.put(Sms.Fields.ADDRESS, toNumber);
		values.put(Sms.Fields.BODY, body);

		getContentResolver().update(dataUri, values, null, null);

		Log.d("OldSchoolSMS", "onPause updated SMS uri: " + dataUri + " getScheme: [" + dataUri.getScheme() + "]");
		return;
	    }
	}

	if (body != null && body.length() != 0) {
	    // put to database the new draft sms
	    Uri draftUri = NotificationService.putNewSmsToDatabase(getContentResolver(), toNumber, body, Sms.Type.MESSAGE_TYPE_DRAFT, Sms.Status.NONE);
	    Log.d("OldSchoolSMS", "onPause new draft SMS uri: " + draftUri + " getScheme: [" + draftUri.getScheme() + "]");

	    // use new intent for this draft
	    Intent draftIntent = new Intent(Intent.ACTION_SEND, draftUri);
	    setIntent(draftIntent);
	} else
	    // remove if exist a draft of it
	    deleteIfDraft();
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
	deleteMenuItem.setVisible(false);

	Sms sms = getSms();
	if (sms != null) {
	    // enable it only if draft...
	    deleteMenuItem.setVisible(Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type));
	}

	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

	// Handle item selection
	switch (item.getItemId()) {

	case R.id.cancel:
	    smsProcessed = true;
	    finish();
	    return true;

	case R.id.delete:
	    Builder deletAlert = createDeletAlert();
	    deletAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int arg1) {
		    smsProcessed = true;
		    deleteIfDraft();
		    finish();
		}
	    });
	    deletAlert.show();

	    return true;

	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    /************************************************
     * functions
     ************************************************/

    private void deleteIfDraft() {
	Sms sms = getSms();

	if (sms != null && Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type)) {
	    getContentResolver().delete(getSmsUri(), null, null);
	}
    }

    private Sms getSms() {
	Intent intent = getIntent();
	String action = intent.getAction();

	if (Intent.ACTION_SEND.equals(action)) {

	    Uri data = getSmsUri();
	    if (data != null) {
		Sms sms = Sms.getSms(getContentResolver(), data);

		return sms;
	    }
	}

	return null;
    }

    private Uri getSmsUri() {
	Intent intent = getIntent();
	Uri data = intent.getData();

	return data;
    }

    private void updateToNumber(final String address) {
	final TextView toNumberView = (TextView) findViewById(R.id.toNumber);

	AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {

	    @Override
	    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
		String personName = null;

		if (cursor != null && cursor.moveToFirst())
		    personName = cursor.getString(0);
		else
		    // default...
		    personName = "(" + address + ")";

		toNumberView.setText(personName);
	    }
	};
	toNumberView.setText("-");
	Sms.getDisplayName(asyncQueryHandler, address);
    }
}
