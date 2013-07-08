package hu.anti.android.oldSchoolSms;

import hu.anti.android.oldSchoolSms.SmsListActivity.SmsBoxType;
import hu.anti.android.oldSchoolSms.observer.AbstractSmsObserver;
import hu.anti.android.oldSchoolSms.observer.AllSmsObserver;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final static class AllSmsObserverHandler extends Handler {
	MainActivity activity = null;

	public AllSmsObserverHandler(MainActivity activity) {
	    this.activity = activity;
	}

	@Override
	public void handleMessage(Message msg) {
	    super.handleMessage(msg);

	    activity.updateButtons();
	}
    }

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
		openSmsList(SmsBoxType.ALL);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// INBOX button
	View mainInbox = this.findViewById(R.id.mainInbox);
	mainInbox.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		openSmsList(SmsBoxType.INBOX);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// SENT button
	View mainSent = this.findViewById(R.id.mainSent);
	mainSent.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		openSmsList(SmsBoxType.SENT);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// DRAFT button
	View mainDraft = this.findViewById(R.id.mainDraft);
	mainDraft.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		openSmsList(SmsBoxType.DRAFT);
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
	smsObserver = new AllSmsObserver(getContentResolver(),
		new AllSmsObserverHandler(this));

	// ////////////////////////////////////////////////////////////
	// version string
	TextView mainVersion = (TextView) this.findViewById(R.id.mainVersion);
	mainVersion.setText("(v " + this.getString(R.string.versionName) + ")");

	// ////////////////////////////////////////////////////////////
	// add ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.addView(this, layout);
    }

    @Override
    protected void onStart() {
	super.onStart();

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, this.getClass()
		.getSimpleName() + " starting");
	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, "onStart Intent: ["
		+ getIntent() + "]");

	// @Override
	// protected void onResume() {
	// super.onResume();

	// ////////////////////////////////////////////////////////////
	// updateNotifications
	startService(new Intent(
		NotificationService.ACTION_UPDATE_SMS_NOTIFICATIONS, null,
		this, NotificationService.class));

	// sms list observer
	getContentResolver().registerContentObserver(smsObserver.getBaseUri(),
		true, smsObserver);

	updateButtons();

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, this.getClass()
		.getSimpleName() + " started");
    }

    @Override
    protected void onRestart() {
	super.onRestart();

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, "onRestart Intent: ["
		+ getIntent() + "]");
    }

    @Override
    protected void onResume() {
	super.onResume();

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, "onResume Intent: ["
		+ getIntent() + "]");
    }

    @Override
    protected void onStop() {
	super.onStop();

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, this.getClass()
		.getSimpleName() + " stopping");

	// @Override
	// protected void onPause() {
	// super.onPause();

	// SMS list observer
	getContentResolver().unregisterContentObserver(smsObserver);

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, this.getClass()
		.getSimpleName() + " stoped");
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();

	// ////////////////////////////////////////////////////////////
	// remove ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.removeView(this, layout);
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

	Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, "Count of elements in " + box
		+ " is: " + count);

	return count;
    }

    private void openSmsList(SmsListActivity.SmsBoxType smsBox) {
	Intent intent = new Intent();

	intent.setClass(MainActivity.this, SmsListActivity.class);
	// intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
	// & Intent.FLAG_ACTIVITY_CLEAR_TASK);
	intent.putExtra(SmsListActivity.SMS_BOX, smsBox.name());

	startActivity(intent);
    }
}
