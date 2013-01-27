package hu.anti.android.oldSchoolSms;

import java.text.SimpleDateFormat;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SmsViewActivity extends AbstractSmsActivity {
    protected boolean contactExists = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.sms_view);

	// ////////////////////////////////////////////////////////////
	// close button
	Button closeButton = (Button) findViewById(R.id.buttonClose);
	closeButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		finish();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.addView(this, layout);
    }

    @Override
    protected void onResume() {
	super.onResume();
	Intent intent = getIntent();
	String action = intent.getAction();
	Uri uri = intent.getData();

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, "Received " + action
		+ " with content: " + uri);

	if (Intent.ACTION_VIEW.equals(action)) {
	    // display SMS
	    final Sms sms = Sms.getSms(getContentResolver(), uri);

	    if (sms == null) {
		// clear notification
		try {
		    NotificationService.removeNotification(this,
			    Integer.parseInt(uri.getLastPathSegment()));
		} catch (NumberFormatException e) {
		    Log.e(AbstractSmsActivity.OLD_SCHOOL_SMS,
			    "SMS id parse error from: " + uri + " message: "
				    + e.getLocalizedMessage());
		}

		return;
	    }

	    final TextView toNumberView = (TextView) findViewById(R.id.textViewNumber);
	    AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(
		    getContentResolver()) {

		@Override
		protected void onQueryComplete(int token, Object cookie,
			Cursor cursor) {
		    String personName = null;

		    if (cursor != null && cursor.moveToFirst()) {
			contactExists = true;
			personName = cursor.getString(0);
		    } else
			// default...
			personName = "(" + sms.address + ")";

		    toNumberView.setText(personName);
		}
	    };
	    if (sms.address == null)
		toNumberView.setText("-");
	    else
		toNumberView.setText(sms.address);
	    Sms.getDisplayName(asyncQueryHandler, sms.address);

	    setText(R.id.textViewDate, SimpleDateFormat.getDateTimeInstance()
		    .format(sms.date));
	    setText(R.id.textViewSms, sms.body);

	    if (Sms.Type.MESSAGE_TYPE_SENT.equals(sms.type)
		    || Sms.Type.MESSAGE_TYPE_OUTBOX.equals(sms.type)
		    || Sms.Type.MESSAGE_TYPE_UNDELIVERED.equals(sms.type)
		    || Sms.Type.MESSAGE_TYPE_FAILED.equals(sms.type)) {
		// sent
		setText(R.id.statusText,
			Sms.decodeSmsDeliveryStatus(getResources(), sms.status));

		// clear notification
		NotificationService
			.removeNotification(this, sms._id.intValue());
	    } else {
		// received
		setText(R.id.statusText, "");

		// update notification
		startService(new Intent(
			NotificationService.ACTION_UPDATE_SMS_NOTIFICATIONS,
			null, this, NotificationService.class));
	    }

	    // update read flag
	    ContentValues values = new ContentValues();
	    values.put(Sms.Fields.READ, Sms.Other.MESSAGE_IS_READ);
	    getContentResolver().update(uri, values, null, null);
	} else {
	    Toast.makeText(getApplicationContext(),
		    "Received not supported action: " + action,
		    Toast.LENGTH_LONG).show();
	}
    }

    /************************************************
     * Activity - OptionsMenu
     ************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.sms, menu);

	return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	menu.findItem(R.id.saveNumber).setEnabled(!contactExists);

	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	final Uri uri = getIntent().getData();

	// Handle item selection
	switch (item.getItemId()) {

	case R.id.resend: {
	    final Sms sms = Sms.getSms(getContentResolver(), uri);
	    if (sms == null)
		return false;

	    // resend only if not a draft
	    if (!Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type)) {
		Builder resendAlert = createResendAlert();
		resendAlert.setPositiveButton(android.R.string.yes,
			new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(
				    DialogInterface dialogInterface, int arg1) {
				Intent intent = new Intent(
					NotificationService.ACTION_SEND_SMS,
					null, SmsViewActivity.this,
					NotificationService.class);
				intent.putExtra(
					NotificationService.EXTRA_ADDRESS,
					sms.address);
				intent.putExtra(NotificationService.EXTRA_BODY,
					sms.body);

				startService(intent);
			    }
			});
		resendAlert.show();
	    }

	    return true;
	}

	case R.id.forward:
	    openSms(uri, Intent.ACTION_SEND);
	    return true;

	case R.id.replay: {
	    Sms sms = Sms.getSms(getContentResolver(), uri);
	    if (sms == null)
		return false;

	    Uri smsToUri = Uri.parse("smsto:" + sms.address);

	    openSms(smsToUri, Intent.ACTION_SENDTO);
	    return true;
	}

	case R.id.delete: {
	    Builder deletAlert = createDeletAlert();
	    deletAlert.setPositiveButton(android.R.string.yes,
		    new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface,
				int arg1) {
			    // execute delete
			    getContentResolver().delete(uri, null, null);
			    dialogInterface.dismiss();
			    SmsViewActivity.this.finish();
			}
		    });
	    deletAlert.show();

	    return true;
	}

	case R.id.copyText: {
	    Sms sms = Sms.getSms(getContentResolver(), uri);
	    if (sms == null)
		return false;

	    if (android.os.Build.VERSION.SDK_INT < 11)
		copyToClipboard(sms);
	    else
		copyToClipboardV11(sms);

	    return true;
	}

	case R.id.call: {
	    final Sms sms = Sms.getSms(getContentResolver(), uri);
	    if (sms == null)
		return false;

	    Intent intent = new Intent(Intent.ACTION_VIEW);
	    intent.setData(Uri.parse("tel:" + sms.address));
	    startActivity(intent);

	    return true;
	}

	case R.id.saveNumber: {
	    final Sms sms = Sms.getSms(getContentResolver(), uri);
	    if (sms == null)
		return false;

	    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
	    intent.putExtra(ContactsContract.Intents.Insert.PHONE, sms.address);
	    intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
	    startActivity(intent);

	    return true;
	}

	case R.id.close:
	    finish();
	    return true;

	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    @SuppressWarnings("deprecation")
    private void copyToClipboard(final Sms sms) {
	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS,
		"copyToClipboard - old version used");

	android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	clipboardManager.setText(sms.body);
    }

    @TargetApi(11)
    private void copyToClipboardV11(final Sms sms) {
	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS,
		"copyToClipboard - API11 version used");

	android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	ClipData clip = ClipData.newPlainText("sms", sms.body);
	clipboardManager.setPrimaryClip(clip);
    }

    /************************************************
     * functions
     ************************************************/

    @Override
    protected void setText(int id, String text) {
	TextView view = (TextView) findViewById(id);
	view.setText(text);
    }
}
