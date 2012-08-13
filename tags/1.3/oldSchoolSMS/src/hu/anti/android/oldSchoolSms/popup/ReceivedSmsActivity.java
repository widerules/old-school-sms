package hu.anti.android.oldSchoolSms.popup;

import hu.anti.android.oldSchoolSms.NotificationService;
import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import hu.anti.android.oldSchoolSms.SmsSendActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class ReceivedSmsActivity extends Activity {
    public static String NEW_SMS_ACTION = "NEW_SMS_ACTION";
    public static String INTENT_SMS_BODY = "INTENT_SMS_BODY";
    public static String INTENT_SMS_TIMESTAMP = "INTENT_SMS_TIMESTAMP";
    public static String INTENT_SMS_ADDRESS = "INTENT_SMS_ADDRESS";

    private final Queue<Sms> receivedSms = new LinkedList<Sms>();

    /************************************************
     * Activity
     ************************************************/

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.new_sms_dialog);

	// ////////////////////////////////////////////////////////////
	// close button
	Button closeButton = (Button) findViewById(R.id.newSmsDialogCloseButton);
	closeButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		updateReadStatus();

		// remove displayed
		receivedSms.poll();

		// update data
		updateView();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// delete button
	Button deleteButton = (Button) findViewById(R.id.newSmsDialogDeleteButton);
	deleteButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		// get selected sms
		final Sms sms = receivedSms.element();

		Builder deletAlert = createDeletAlert();
		deletAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialogInterface, int arg1) {
			dialogInterface.dismiss();

			// remove displayed
			try {
			    // get uri
			    final Uri smsUri = sms.findSmsByContent(getContentResolver());

			    // delete
			    if (smsUri != null)
				getContentResolver().delete(smsUri, null, null);

			    receivedSms.remove(sms);

			    // update data
			    updateView();
			} catch (NoSuchElementException e) {
			    Log.w("OldSchoolSMS", "SMS " + sms.toString() + " removed: " + e.getLocalizedMessage());
			}
		    }
		});

		deletAlert.show();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// replay button
	Button replayButton = (Button) findViewById(R.id.newSmsDialogReplayButton);
	replayButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		updateReadStatus();

		// get selected sms
		Sms sms = receivedSms.element();

		// create the uri
		Uri smsToUri = Uri.parse("smsto:" + sms.address);

		// create intent
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(smsToUri);
		intent.setClass(ReceivedSmsActivity.this.getApplicationContext(), SmsSendActivity.class);

		startActivity(intent);
		finish();
	    }
	});
    }

    @Override
    protected void onResume() {
	super.onResume();

	Intent intent = getIntent();
	Log.d("OldSchoolSMS", "ReceivedSmsActivity - onResume " + intent);

	queueSms(intent);

	updateView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	Log.d("OldSchoolSMS", "ReceivedSmsActivity - onNewIntent: " + intent);

	queueSms(intent);

	// updateView();
    }

    private void queueSms(Intent intent) {
	if (intent != null)
	    if (NEW_SMS_ACTION.equals(intent.getAction())) {
		Sms sms = new Sms();
		sms.address = intent.getStringExtra(INTENT_SMS_ADDRESS);
		sms.body = intent.getStringExtra(INTENT_SMS_BODY);
		sms.date = new Date(intent.getLongExtra(INTENT_SMS_TIMESTAMP, 0));

		receivedSms.add(sms);

		// remove intent
		setIntent(null);
	    }
    }

    protected void updateView() {
	// if no more SMS close the dialog
	if (receivedSms.size() == 0) {
	    finish();
	    return;
	}

	// get selected sms
	final Sms sms = receivedSms.element();

	// set timestamp
	TextView timestampView = (TextView) findViewById(R.id.newSmsDialogDateTime);
	timestampView.setText(new SimpleDateFormat("yyyy.MM.dd. HH.mm").format(sms.date));

	// set sender
	final TextView senderView = (TextView) findViewById(R.id.newSmsDialogSenderView);

	AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {

	    @Override
	    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
		String personName = null;

		if (cursor != null && cursor.moveToFirst())
		    personName = cursor.getString(0);
		else
		    // default...
		    personName = "(" + sms.address + ")";

		senderView.setText(personName);
	    }
	};
	senderView.setText(sms.address);
	Sms.getDisplayName(asyncQueryHandler, sms.address);

	// set message
	TextView messageView = (TextView) findViewById(R.id.newSmsDialogTextView);
	messageView.setText(sms.body);
    }

    private void updateReadStatus() {
	CheckBox markAsReadView = (CheckBox) findViewById(R.id.newSmsDialogMarkAsRead);
	boolean markAsRead = markAsReadView.isChecked();

	if (markAsRead) {
	    Intent intent = new Intent(NotificationService.ACTION_MARK_AS_READ_SMS, null, getApplicationContext(), NotificationService.class);

	    Sms sms = receivedSms.element();
	    intent.putExtra(NotificationService.EXTRA_ADDRESS, sms.address);
	    intent.putExtra(NotificationService.EXTRA_BODY, sms.body);

	    getApplicationContext().startService(intent);
	}
    }

    protected AlertDialog.Builder createDeletAlert() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);

	builder.setIcon(android.R.drawable.ic_dialog_alert);
	builder.setTitle(R.string.questionDelete);
	builder.setCancelable(true);
	builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialogInterface, int arg1) {
		dialogInterface.dismiss();
	    }
	});

	return builder;
    }
}
