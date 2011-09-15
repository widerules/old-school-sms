package hu.anti.android.oldSchoolSms;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

	// ////////////////////////////////////////////////////////////
	// clear notifications
	NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.cancelAll();

	// ////////////////////////////////////////////////////////////
	Intent intent = getIntent();
	String action = intent.getAction();

	Log.d("OldSchoolSMS", "Received " + action + " with content: " + intent.getData());

	if (Intent.ACTION_VIEW.equals(action)) {
	    // display SMS
	    Sms sms = Sms.getSms(getContentResolver(), intent.getData());

	    setText(R.id.textViewNumber, sms.getDisplayName(getContentResolver()));
	    setText(R.id.textViewDate, sms.date.toLocaleString());
	    setText(R.id.textViewSms, sms.body);

	    if (Sms.Type.MESSAGE_TYPE_SENT.equals(sms.type) || Sms.Type.MESSAGE_TYPE_OUTBOX.equals(sms.type)
		    || Sms.Type.MESSAGE_TYPE_UNDELIVERED.equals(sms.type) || Sms.Type.MESSAGE_TYPE_FAILED.equals(sms.type))
		setText(R.id.statusText, Sms.decodeSmsDeliveryStatus(getResources(), sms.status));
	    else
		setText(R.id.statusText, "");
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
	Uri uri = getIntent().getData();

	// Handle item selection
	switch (item.getItemId()) {

	case R.id.forward:
	    openSms(uri, Intent.ACTION_SEND);
	    return true;

	case R.id.replay:
	    Sms sms = Sms.getSms(getContentResolver(), uri);
	    Uri smsToUri = Uri.parse("smsto:" + sms.address);

	    openSms(smsToUri, Intent.ACTION_SENDTO);
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
