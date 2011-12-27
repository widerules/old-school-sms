package hu.anti.android.oldSchoolSms;

import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
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

	Log.d("OldSchoolSMS", "Received " + action + " with content: " + uri);

	if (Intent.ACTION_VIEW.equals(action)) {
	    // display SMS
	    Sms sms = Sms.getSms(getContentResolver(), uri);

	    if (sms == null) {
		// clear notification
		try {
		    NotificationService.removeNotification(this, Integer.parseInt(uri.getLastPathSegment()));
		} catch (NumberFormatException e) {
		    Log.e("OldSchoolSMS", "SMS id parse error from: " + uri + " message: " + e.getLocalizedMessage());
		}

		return;
	    }

	    setText(R.id.textViewNumber, sms.getDisplayName(getContentResolver()));
	    setText(R.id.textViewDate, sms.date.toLocaleString());
	    setText(R.id.textViewSms, sms.body);

	    if (Sms.Type.MESSAGE_TYPE_SENT.equals(sms.type) || Sms.Type.MESSAGE_TYPE_OUTBOX.equals(sms.type)
		    || Sms.Type.MESSAGE_TYPE_UNDELIVERED.equals(sms.type) || Sms.Type.MESSAGE_TYPE_FAILED.equals(sms.type)) {
		// sent
		setText(R.id.statusText, Sms.decodeSmsDeliveryStatus(getResources(), sms.status));

		// clear notification
		NotificationService.removeNotification(this, sms._id.intValue());
	    } else {
		// received
		setText(R.id.statusText, "");

		// update notification
		startService(new Intent(NotificationService.ACTION_UPDATE_SMS_NOTIFICATIONS, null, this, NotificationService.class));
	    }

	    // update read flag
	    ContentValues values = new ContentValues();
	    values.put(Sms.Fields.READ, Sms.Other.MESSAGE_IS_READ);
	    getContentResolver().update(uri, values, null, null);
	} else {
	    Toast.makeText(getApplicationContext(), "Received not supported action: " + action, Toast.LENGTH_LONG).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
	final Uri uri = getIntent().getData();

	// Handle item selection
	switch (item.getItemId()) {

	case R.id.resend: {
	    // FIXME
	    Sms sms = Sms.getSms(getContentResolver(), uri);
	    if (sms == null)
		return false;

	    Intent intent = new Intent(NotificationService.ACTION_SEND_SMS, null, this, NotificationService.class);
	    intent.putExtra(NotificationService.EXTRA_ADDRESS, sms.address);
	    intent.putExtra(NotificationService.EXTRA_BODY, sms.body);

	    startService(intent);

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

	case R.id.delete:
	    Builder deletAlert = createDeletAlert(uri);
	    deletAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int arg1) {
		    // execute delete
		    getContentResolver().delete(uri, null, null);
		    dialogInterface.dismiss();
		    SmsViewActivity.this.finish();
		}
	    });
	    deletAlert.show();

	    return true;

	case R.id.copyText:
	    Sms sms = Sms.getSms(getContentResolver(), uri);
	    if (sms == null)
		return false;

	    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	    clipboardManager.setText(sms.body);

	    return true;

	case R.id.close:
	    finish();
	    return true;

	default:
	    return super.onOptionsItemSelected(item);
	}
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
