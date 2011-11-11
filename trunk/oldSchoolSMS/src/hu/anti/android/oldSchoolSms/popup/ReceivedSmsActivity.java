package hu.anti.android.oldSchoolSms.popup;

import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.Sms;
import hu.anti.android.oldSchoolSms.SmsSendActivity;
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
    public static String INTENT_SMS_ADDRESS = "INTENT_SMS_ADDRESS";
    private String toNumber;

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
		setIntent(null);
		finish();
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
		Uri smsToUri = Uri.parse("smsto:" + toNumber);

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

	if (intent == null)
	    return;

	Log.d("OldSchoolSMS", "Received " + intent);

	if (NEW_SMS_ACTION.equals(intent.getAction())) {
	    // set sender
	    TextView senderView = (TextView) findViewById(R.id.newSmsDialogSenderView);
	    toNumber = intent.getStringExtra(INTENT_SMS_ADDRESS);

	    senderView.setText(Sms.getDisplayName(getContentResolver(), toNumber));

	    // set message
	    TextView messageView = (TextView) findViewById(R.id.newSmsDialogTextView);
	    messageView.setText(intent.getStringExtra(INTENT_SMS_BODY));

	    // notify
	    vibrate();
	}
    }

    @Override
    protected void onPause() {
	super.onPause();
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
