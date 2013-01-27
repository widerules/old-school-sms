package hu.anti.android.oldSchoolSms;

import hu.anti.android.oldSchoolSms.observer.AbstractSmsObserver;
import hu.anti.android.oldSchoolSms.observer.AllSmsObserver;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private AbstractSmsObserver smsObserver;

    /************************************************
     * Activity
     ************************************************/
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	// ////////////////////////////////////////////////////////////
	// new sms button
	View mainNewSms = this.findViewById(R.id.mainNewSms);
	mainNewSms.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setClass(getApplicationContext(), SmsSendActivity.class);
		startActivity(intent);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// ALL button
	View mainAll = this.findViewById(R.id.mainAll);
	mainAll.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		setSmsBox("ALL");
		openSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// INBOX button
	View mainInbox = this.findViewById(R.id.mainInbox);
	mainInbox.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		setSmsBox("INBOX");
		openSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// SENT button
	View mainSent = this.findViewById(R.id.mainSent);
	mainSent.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		setSmsBox("SENT");
		openSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// DRAFT button
	View mainDraft = this.findViewById(R.id.mainDraft);
	mainDraft.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		setSmsBox("DRAFT");
		openSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// preferences button
	View mainPreferences = this.findViewById(R.id.mainPreferences);
	mainPreferences.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		startActivity(new Intent(MainActivity.this,
			SmsPreferenceActivity.class));
	    }
	});

	// ////////////////////////////////////////////////////////////
	// create SMS list observer
	smsObserver = new AllSmsObserver(getContentResolver(), new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		super.handleMessage(msg);

		updateButtons();
	    }
	});
    }

    @Override
    protected void onResume() {
	super.onResume();

	// ////////////////////////////////////////////////////////////
	// add ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.addView(this, layout);

	// ////////////////////////////////////////////////////////////
	// updateNotifications
	startService(new Intent(
		NotificationService.ACTION_UPDATE_SMS_NOTIFICATIONS, null,
		this, NotificationService.class));

	// sms list observer
	getContentResolver().registerContentObserver(smsObserver.getBaseUri(),
		true, smsObserver);

	updateButtons();
    }

    @Override
    protected void onPause() {
	super.onPause();

	// ////////////////////////////////////////////////////////////
	// remove ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.removeView(this, layout);

	// SMS list observer
	getContentResolver().unregisterContentObserver(smsObserver);
    }

    /************************************************
     * functions
     ************************************************/

    private void updateButtons() {
	// create cursor
	int count;

	TextView mainAll = (TextView) this.findViewById(R.id.mainAll);
	count = getCount(this, "");
	mainAll.setText(getString(R.string.BoxAll)
		+ (count == 0 ? "" : " (" + count + ")"));

	TextView mainInbox = (TextView) this.findViewById(R.id.mainInbox);
	count = getCount(this, "inbox");
	mainInbox.setText(getString(R.string.BoxInbox)
		+ (count == 0 ? "" : " (" + count + "/"
			+ NotificationService.getUnreadCount(this) + ")"));

	TextView mainSent = (TextView) this.findViewById(R.id.mainSent);
	count = getCount(this, "sent");
	mainSent.setText(getString(R.string.BoxSent)
		+ (count == 0 ? "" : " (" + count + ")"));

	TextView mainDraft = (TextView) this.findViewById(R.id.mainDraft);
	count = getCount(this, "draft");
	mainDraft.setText(getString(R.string.BoxDraft)
		+ (count == 0 ? "" : " (" + count + ")"));
    }

    private int getCount(Context context, String box) {
	ContentResolver contentResolver = context.getContentResolver();
	Uri uri = Uri.parse(Sms.Uris.SMS_URI_BASE + box);
	Cursor cursor = contentResolver.query(uri, null, null, null, null);

	if (cursor == null)
	    return 0;

	int count = cursor.getCount();
	cursor.close();

	Log.d("OldSchoolSMS", "Count of elements in " + box + " is: " + count);

	return count;
    }

    private void setSmsBox(String smsBox) {
	// ////////////////////////////////////////////////////////////
	// store preferences
	SharedPreferences sharedPrefs = PreferenceManager
		.getDefaultSharedPreferences(getBaseContext());
	Editor editor = sharedPrefs.edit();

	// SMS box
	editor.putString("smsBox", smsBox);

	// commit
	editor.commit();

    }

    private void openSmsList() {
	Intent intent = new Intent();
	intent.setClass(MainActivity.this, SmsListActivity.class);
	startActivity(intent);
    }
}
