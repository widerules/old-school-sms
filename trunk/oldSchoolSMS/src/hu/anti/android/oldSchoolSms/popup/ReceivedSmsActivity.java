package hu.anti.android.oldSchoolSms.popup;

import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.SmsSendActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
		TextView senderView = (TextView) findViewById(R.id.newSmsDialogSenderView);

		Uri smsToUri = Uri.parse("smsto:" + senderView.getText());

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
	    senderView.setText(intent.getStringExtra(INTENT_SMS_ADDRESS));

	    // set message
	    TextView messageView = (TextView) findViewById(R.id.newSmsDialogTextView);
	    messageView.setText(intent.getStringExtra(INTENT_SMS_BODY));
	}
    }

    @Override
    protected void onPause() {
	super.onPause();
    }

    // @Override
    // protected Dialog onCreateDialog(int id) {
    // // create dialog
    // Dialog dialog = new Dialog(context.getApplicationContext());
    // dialog.setContentView(R.layout.new_sms_dialog);
    //
    // // set sender
    // dialog.setTitle(address);
    // // set message
    // TextView text = (TextView)
    // dialog.findViewById(R.id.newSmsDialogTextView);
    // text.setText(body);
    //
    // return dialog;
    // }
}
