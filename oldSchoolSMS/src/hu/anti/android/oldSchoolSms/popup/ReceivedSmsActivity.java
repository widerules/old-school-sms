package hu.anti.android.oldSchoolSms.popup;

import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import hu.anti.android.oldSchoolSms.SmsSendActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ReceivedSmsActivity extends Activity {
    public static String NEW_SMS_ACTION = "NEW_SMS_ACTION";
    public static String INTENT_SMS_BODY = "INTENT_SMS_BODY";
    public static String INTENT_SMS_TIMESTAMP = "INTENT_SMS_TIMESTAMP";
    public static String INTENT_SMS_ADDRESS = "INTENT_SMS_ADDRESS";

    private final List<Sms> receivedSms = new ArrayList<Sms>();
    private int displayedSms = 0;

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
		// remove displayed
		receivedSms.remove(displayedSms);
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
		finish();
	    }
	});
	deleteButton.setVisibility(View.INVISIBLE);

	// ////////////////////////////////////////////////////////////
	// replay button
	Button replayButton = (Button) findViewById(R.id.newSmsDialogReplayButton);
	replayButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		// get selected sms
		Sms sms = receivedSms.get(displayedSms);

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

	if (intent != null)
	    if (NEW_SMS_ACTION.equals(intent.getAction())) {
		Sms sms = extractIntent(intent);

		receivedSms.add(sms);
		displayedSms = 0;

		// notify
		vibrate();

		// remove intent
		setIntent(null);
	    }

	updateView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	Log.d("OldSchoolSMS", "ReceivedSmsActivity - onNewIntent: " + intent);

	if (intent != null)
	    if (NEW_SMS_ACTION.equals(intent.getAction())) {
		Sms sms = extractIntent(intent);

		receivedSms.add(sms);

		// notify
		vibrate();

		// remove intent
		setIntent(null);
	    }

	// updateView();
    }

    protected Sms extractIntent(Intent intent) {
	Sms sms = new Sms();
	sms.address = intent.getStringExtra(INTENT_SMS_ADDRESS);
	sms.body = intent.getStringExtra(INTENT_SMS_BODY);
	sms.date = new Date(intent.getLongExtra(INTENT_SMS_TIMESTAMP, 0));

	return sms;
    }

    protected void updateView() {
	// update to last
	if (displayedSms > receivedSms.size() - 1)
	    displayedSms = receivedSms.size() - 1;

	// if no more SMS close the dialog
	if (displayedSms == -1) {
	    finish();
	    return;
	}

	// get selected sms
	Sms sms = receivedSms.get(displayedSms);

	// set timestamp
	TextView timestampView = (TextView) findViewById(R.id.newSmsDialogDateTime);
	timestampView.setText(new SimpleDateFormat("yyyy.MM.dd. HH.mm").format(sms.date));

	// set sender
	TextView senderView = (TextView) findViewById(R.id.newSmsDialogSenderView);
	senderView.setText(sms.getDisplayName(getContentResolver()));

	// set message
	TextView messageView = (TextView) findViewById(R.id.newSmsDialogTextView);
	messageView.setText(sms.body);
    }

    private void vibrate() {
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	String vibratorPattern = sharedPrefs.getString("vibratorPattern", "333,333,333");

	// extract pattern
	String[] split = vibratorPattern.split("[,.;/\\- ]");
	// create vibration pattern holder
	long[] pattern = new long[(split.length + 1)];
	// set starting wait to 0ms
	pattern[0] = 0;
	// decode pattern string
	for (int i = 0; i < split.length; i++) {
	    pattern[i + 1] = Long.parseLong(split[i]);
	}

	// Start the vibration
	Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	vibrator.vibrate(pattern, -1);
    }
}
