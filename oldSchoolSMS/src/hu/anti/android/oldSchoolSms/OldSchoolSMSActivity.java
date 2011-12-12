package hu.anti.android.oldSchoolSms;

import hu.anti.android.oldSchoolSms.observer.AbstractSmsObserver;
import hu.anti.android.oldSchoolSms.observer.AllSmsObserver;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class OldSchoolSMSActivity extends AbstractSmsActivity {

    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_UPDATE_FINISHED = "UPDATE_FINISHED";

    // the displayed list's content
    protected ArrayList<Sms> smsList = new ArrayList<Sms>();

    // the displayed pages index
    private int pageIndexCurrent = 0;

    private class Preferences {

	protected boolean debugModeSmsList = true;

	protected int pageSize = 50;

    }

    private final Preferences preferences = new Preferences();

    // the max number of elements
    private int pageIndexMax = 0;

    private ProgressDialog dialog = null;
    private AbstractSmsObserver smsObserver;

    private BroadcastReceiver listUpdatedReceiver;

    /************************************************
     * Activity
     ************************************************/

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_PROGRESS);

	setContentView(R.layout.main);

	// ////////////////////////////////////////////////////////////
	// previouse button
	View mainPreviouse = this.findViewById(R.id.mainPreviouse);
	mainPreviouse.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		if (pageIndexCurrent > 0)
		    pageIndexCurrent--;

		updateSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// next button
	View mainNext = this.findViewById(R.id.mainNext);
	mainNext.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		if (pageIndexCurrent < pageIndexMax)
		    pageIndexCurrent++;

		updateSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// SMS list
	ListView smsListView = (ListView) findViewById(R.id.SMSList);
	// sms list adapter
	SmsAdapter smsAdapter = new SmsAdapter(this, new ArrayList<Sms>());

	// sms list
	smsListView.setAdapter(smsAdapter);
	registerForContextMenu(smsListView);

	((ListView) this.findViewById(R.id.SMSList)).setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> paramAdapterView, View view, int pos, long id) {
		if (smsList.size() <= pos) {
		    Log.w("OldSchoolSMS", "position [" + pos + "] too large for sms list (" + smsList.size() + ")");
		    return;
		}

		Sms sms = smsList.get(pos);

		if (preferences.debugModeSmsList) {
		    String content = "";

		    // Sms sms = Sms.getSms(getContentResolver(),
		    // smsList
		    // .get(pos)._id);
		    // content = sms.toString();

		    Cursor cursor = getContentResolver().query(Uri.parse("content://sms/" + sms._id), null, null, null, null);

		    if (cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getColumnCount(); i++) {
			    content += cursor.getColumnName(i) + "=" + cursor.getString(i) + "\n";
			}
		    } else {
			for (int i = 0; i < cursor.getColumnCount(); i++) {
			    content += cursor.getColumnName(i) + "\n";
			}
		    }

		    if (cursor != null)
			cursor.close();

		    Toast.makeText(OldSchoolSMSActivity.this, content, Toast.LENGTH_LONG).show();

		} else {
		    Uri uri = ContentUris.withAppendedId(Uri.parse(Sms.Uris.SMS_URI_BASE), sms._id);

		    openSms(uri, Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type) ? Intent.ACTION_SEND : Intent.ACTION_VIEW);
		}
	    }
	});

	// box selector spinner
	Spinner spinner = (Spinner) findViewById(R.id.boxSpinner);

	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		// String selected = parent.getItemAtPosition(pos).toString();
		// Toast.makeText(parent.getContext(), selected +
		// " is selected.", Toast.LENGTH_SHORT).show();

		pageIndexCurrent = 0;

		updateSmsList();
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> parent) {
		updateSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// create SMS list observer
	smsObserver = new AllSmsObserver(getContentResolver(), new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		super.handleMessage(msg);

		// update list
		updateSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// SMS list updated receiver
	listUpdatedReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context paramContext, Intent paramIntent) {
		Log.d("OldSchoolSMS", "Received " + paramIntent);

		// move to end the progress bar
		setProgress(10000);
		setProgressBarVisibility(false);

		// start indeterminate progress
		setProgressBarIndeterminateVisibility(true);
		setProgressBarIndeterminate(true);

		ListView smsListView = (ListView) findViewById(R.id.SMSList);
		@SuppressWarnings("unchecked")
		ArrayAdapter<Sms> arrayAdapter = ((ArrayAdapter<Sms>) smsListView.getAdapter());

		arrayAdapter.clear();

		for (int i = 0; i < smsList.size(); i++) {
		    arrayAdapter.add(smsList.get(i));
		}

		// stop indeterminate progress
		setProgressBarIndeterminate(false);
		setProgressBarIndeterminateVisibility(false);
	    }
	};
    }

    @Override
    protected void onResume() {
	super.onResume();

	// ////////////////////////////////////////////////////////////
	// add ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.addView(this, layout);

	// get shared preferences
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

	// ////////////////////////////////////////////////////////////
	// updateNotifications
	startService(new Intent(NotificationService.ACTION_UPDATE_SMS_NOTIFICATIONS, null, this, NotificationService.class));

	// ////////////////////////////////////////////////////////////
	// update spiner

	// update showMessageContentInList
	boolean showMessageContentInList = sharedPrefs.getBoolean("showMessageContentInList", true);
	ListView smsListView = (ListView) findViewById(R.id.SMSList);
	((SmsAdapter) smsListView.getAdapter()).setShowMessageContentInList(showMessageContentInList);

	// get extendedBoxList
	boolean extendedBoxList = sharedPrefs.getBoolean("extendedBoxList", false);
	// create adapter
	ArrayAdapter<CharSequence> boxAdapter;
	if (extendedBoxList)
	    boxAdapter = ArrayAdapter.createFromResource(this, R.array.boxes_extended_array, android.R.layout.simple_spinner_item);
	else
	    boxAdapter = ArrayAdapter.createFromResource(this, R.array.boxes_array, android.R.layout.simple_spinner_item);
	boxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	// set to spinner
	Spinner spinner = (Spinner) findViewById(R.id.boxSpinner);
	spinner.setAdapter(boxAdapter);

	// get last selected box
	String smsBox = sharedPrefs.getString("smsBox", Sms.Uris.SMS_URI_BASE);
	// reselect last selected
	for (int position = 0; position < boxAdapter.getCount(); position++) {
	    // find last selected
	    if (smsBox.equals(boxAdapter.getItem(position))) {
		spinner.setSelection(position);

		boxAdapter.notifyDataSetChanged();
		break;
	    }
	}

	// if nothing is selected, select the first
	if (spinner.getSelectedItemId() == Spinner.INVALID_ROW_ID) {
	    spinner.setSelection(0);
	    boxAdapter.notifyDataSetChanged();
	}

	// ////////////////////////////////////////////////////////////
	// update preferences
	// page size
	preferences.pageSize = Integer.parseInt(sharedPrefs.getString("pageSize", "50"));
	// debugModeSmsList
	preferences.debugModeSmsList = sharedPrefs.getBoolean("debugModeSmsList", false) && sharedPrefs.getBoolean("debug", false);

	// ////////////////////////////////////////////////////////////
	// register receivers
	// SMS list updated
	registerReceiver(listUpdatedReceiver, new IntentFilter(ACTION_UPDATE_FINISHED));
	// sms list observer
	getContentResolver().registerContentObserver(smsObserver.getBaseUri(), true, smsObserver);
    }

    @Override
    protected void onPause() {
	super.onPause();

	// ////////////////////////////////////////////////////////////
	// remove ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.removeView(this, layout);

	// ////////////////////////////////////////////////////////////
	// unregister receivers
	// SMS list updated receiver
	unregisterReceiver(listUpdatedReceiver);
	// SMS list observer
	getContentResolver().unregisterContentObserver(smsObserver);

	// ////////////////////////////////////////////////////////////
	// store preferences
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	Editor editor = sharedPrefs.edit();

	// SMS box
	Spinner spinner = (Spinner) findViewById(R.id.boxSpinner);
	editor.putString("smsBox", spinner.getSelectedItem().toString());

	// commit
	editor.commit();
    }

    /************************************************
     * Activity - OptionsMenu
     ************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle item selection
	switch (item.getItemId()) {
	case R.id.newSms:
	    openSms(null, Intent.ACTION_SEND);
	    return true;

	case R.id.multiDelete:
	    Toast.makeText(getApplicationContext(), "TODO", Toast.LENGTH_SHORT).show();
	    return true;

	case R.id.preferences:
	    startActivity(new Intent(this, SmsPreferenceActivity.class));
	    return true;

	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    /************************************************
     * Activity - ContextMenu
     ************************************************/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

	Uri uri = ContentUris.withAppendedId(Uri.parse(Sms.Uris.SMS_URI_BASE), smsList.get(info.position)._id);

	switch (item.getItemId()) {
	case R.id.view: {
	    Cursor cursor = getContentResolver().query(uri, null, null, null, null);

	    if (!cursor.moveToFirst())
		return false;

	    Sms sms = Sms.parseSms(cursor);
	    if (cursor != null)
		cursor.close();

	    openSms(uri, Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type) ? Intent.ACTION_SEND : Intent.ACTION_VIEW);

	    return true;
	}
	case R.id.delete:
	    getContentResolver().delete(uri, null, null);
	    return true;

	case R.id.forward:
	    openSms(uri, Intent.ACTION_SEND);
	    return true;

	case R.id.replay: {
	    Sms sms = Sms.getSms(getContentResolver(), uri);
	    Uri smsToUri = Uri.parse("smsto:" + sms.address);

	    openSms(smsToUri, Intent.ACTION_SENDTO);
	    return true;
	}
	case R.id.resend: {
	    Cursor cursor = getContentResolver().query(uri, null, null, null, null);

	    if (!cursor.moveToFirst())
		return false;

	    Sms sms = Sms.parseSms(cursor);
	    if (cursor != null)
		cursor.close();

	    Intent popupIntent = new Intent(NotificationService.ACTION_SEND_SMS, null, this, NotificationService.class);
	    popupIntent.putExtra(NotificationService.EXTRA_ADDRESS, sms.address);
	    popupIntent.putExtra(NotificationService.EXTRA_BODY, sms.body);

	    startService(popupIntent);

	    return true;
	}

	default:
	    return super.onContextItemSelected(item);
	}
    }

    /************************************************
     * Activity - dialog
     ************************************************/

    @Override
    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case 0:
	    dialog = new ProgressDialog(this, ProgressDialog.STYLE_HORIZONTAL);
	    dialog.setIndeterminate(false);

	    return dialog;

	default:
	    return super.onCreateDialog(id);
	}
    }

    /************************************************
     * ...
     ************************************************/

    /************************************************
     * functions
     ************************************************/
    public void updateSmsList() {
	// showDialog(0);
	setProgressBarVisibility(true);
	setProgress(0);

	new Thread() {
	    @Override
	    synchronized public void run() {

		Cursor cursor = null;
		try {
		    Log.d("OldSchoolSMS", "Thread started");

		    // remove existing data
		    smsList.clear();

		    // get box uri
		    Spinner spinner = (Spinner) findViewById(R.id.boxSpinner);
		    Uri uri = Uri.parse(Sms.Uris.URIS.get(spinner.getSelectedItemPosition()));

		    // create cursor
		    ContentResolver contentResolver = getContentResolver();
		    cursor = contentResolver.query(uri, null, null, null, Sms.Fields.DATE + " desc");

		    // set max size
		    int count = cursor == null ? 0 : cursor.getCount();

		    pageIndexMax = (count - 1) / preferences.pageSize;

		    // update page index if too large
		    if (pageIndexCurrent > pageIndexMax)
			pageIndexCurrent = pageIndexMax;

		    Log.d("OldSchoolSMS", "Updateing SMS list: " + count);

		    // check result
		    if (cursor != null && cursor.moveToFirst()) {
			int startPosition = preferences.pageSize * pageIndexCurrent;
			int maxPosition = count - startPosition < preferences.pageSize ? count - startPosition : preferences.pageSize;

			// move to page
			cursor.moveToPosition(startPosition);

			// read data
			do {
			    // get the SMS
			    Sms sms = Sms.parseSms(cursor);
			    // store
			    smsList.add(sms);

			    try {
				OldSchoolSMSActivity.this.setProgress(smsList.size() * 10000 / (maxPosition + 1));
			    } catch (Exception e) {
				Log.e("OldSchoolSMS", e.getLocalizedMessage());
			    }
			} while (smsList.size() < preferences.pageSize && cursor.moveToNext());
		    }
		} catch (Exception e) {
		    showError(e);
		}

		if (cursor != null)
		    cursor.close();

		Intent intent = new Intent(ACTION_UPDATE_FINISHED);
		sendBroadcast(intent);

		Log.d("OldSchoolSMS", "Thread finished");
	    }
	}.start();
    }
}