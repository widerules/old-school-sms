package hu.anti.android.oldScoolSms;

import hu.anti.android.oldScoolSms.receiver.SmsReceiver;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
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
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class OldScoolSMSActivity extends AbstractSmsActivity {

    // the displayed list's content
    protected ArrayList<Sms> smsList = new ArrayList<Sms>();

    // the displayed pages index
    private int pageIndex = 0;

    private class Preferences {

	protected boolean debugModeSmsList = true;

	protected int pageSize = 50;

    }

    private final Preferences preferences = new Preferences();

    // the max number of elements
    private int pageIndexMax = 0;

    private SmsReceiver smsReceiver;

    /************************************************
     * Activity
     ************************************************/

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	// ////////////////////////////////////////////////////////////
	// ad mob
	LinearLayout layout = (LinearLayout) findViewById(R.id.AdMob);
	AdMob.addView(this, layout);

	// ////////////////////////////////////////////////////////////
	// previouse button
	View mainPreviouse = this.findViewById(R.id.mainPreviouse);
	mainPreviouse.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		if (pageIndex > 0)
		    pageIndex--;

		updateSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// next button
	View mainNext = this.findViewById(R.id.mainNext);
	mainNext.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		if (pageIndex < pageIndexMax)
		    pageIndex++;

		updateSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// SMS list
	ListView smsListView = (ListView) findViewById(R.id.SMSList);
	// sms list adapter
	SmsAdapter smsAdapter = new SmsAdapter(this, smsList);

	// sms list
	smsListView.setAdapter(smsAdapter);
	registerForContextMenu(smsListView);

	((ListView) this.findViewById(R.id.SMSList)).setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> paramAdapterView, View view, int pos, long id) {
		Uri uri = ContentUris.withAppendedId(Uri.parse(Sms.Uris.SMS_URI_BASE), smsList.get(pos)._id);

		if (preferences.debugModeSmsList) {
		    String content = "";

		    // Sms sms = Sms.getSms(getContentResolver(),
		    // smsList
		    // .get(pos)._id);
		    // content = sms.toString();

		    Cursor cursor = getContentResolver().query(Uri.parse("content://sms/" + smsList.get(pos)._id), null, null, null, null);

		    if (cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getColumnCount(); i++) {
			    content += cursor.getColumnName(i) + "=" + cursor.getString(i) + "\n";
			}
		    } else {
			for (int i = 0; i < cursor.getColumnCount(); i++) {
			    content += cursor.getColumnName(i) + "\n";
			}
		    }

		    Toast.makeText(OldScoolSMSActivity.this, content, Toast.LENGTH_LONG).show();

		} else {
		    openSms(uri, Intent.ACTION_VIEW);
		}
	    }
	});

	// box selector spinner
	Spinner spinner = (Spinner) findViewById(R.id.boxSpinner);

	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    @Override
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		String selected = parent.getItemAtPosition(pos).toString();
		// Toast.makeText(parent.getContext(), selected +
		// " is selected.", Toast.LENGTH_SHORT).show();

		pageIndex = 0;

		updateSmsList();
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> parent) {
		updateSmsList();
	    }
	});

	// ////////////////////////////////////////////////////////////
	// create sms receiver
	smsReceiver = new SmsReceiver(this);

	// ////////////////////////////////////////////////////////////
	// create observer
	SMSObserver smsObserver = new SMSObserver(getContentResolver(), new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		super.handleMessage(msg);

		// update list
		updateSmsList();
	    }
	});
	// register observer
	getContentResolver().registerContentObserver(Uri.parse(Sms.Uris.SMS_URI_BASE), true, smsObserver);
    }

    @Override
    protected void onResume() {
	super.onResume();

	// ////////////////////////////////////////////////////////////
	// get preferences
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

	// ////////////////////////////////////////////////////////////
	// debugModeSmsList
	preferences.debugModeSmsList = sharedPrefs.getBoolean("debugModeSmsList", true);

	// ////////////////////////////////////////////////////////////
	// showMessageContentInList
	boolean showMessageContentInList = sharedPrefs.getBoolean("showMessageContentInList", true);
	ListView smsListView = (ListView) findViewById(R.id.SMSList);
	((SmsAdapter) smsListView.getAdapter()).setShowMessageContentInList(showMessageContentInList);

	// ////////////////////////////////////////////////////////////
	// box selector spinner
	Spinner spinner = (Spinner) findViewById(R.id.boxSpinner);

	// ////////////////////////////////////////////////////////////
	// extendedBoxList
	boolean extendedBoxList = sharedPrefs.getBoolean("extendedBoxList", false);
	ArrayAdapter<CharSequence> boxAdapter;
	if (extendedBoxList)
	    boxAdapter = ArrayAdapter.createFromResource(this, R.array.boxes_extended_array, android.R.layout.simple_spinner_item);
	else
	    boxAdapter = ArrayAdapter.createFromResource(this, R.array.boxes_array, android.R.layout.simple_spinner_item);

	boxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	spinner.setAdapter(boxAdapter);

	// ////////////////////////////////////////////////////////////
	// smsBox
	String smsBox = sharedPrefs.getString("smsBox", Sms.Uris.SMS_URI_BASE);

	SpinnerAdapter spinnerAdapter = spinner.getAdapter();
	for (int position = 0; position < spinnerAdapter.getCount(); position++) {
	    if (smsBox.equals(spinnerAdapter.getItem(position))) {
		spinner.setSelection(position);

		boxAdapter.notifyDataSetChanged();
		break;
	    }
	}

	// ////////////////////////////////////////////////////////////
	// page size
	preferences.pageSize = Integer.parseInt(sharedPrefs.getString("pageSize", "50"));

	// ////////////////////////////////////////////////////////////
	// new SMS receiver
	registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
	smsReceiver.setNotifyOnNewSms(sharedPrefs.getBoolean("notifyOnNewSms", true));

	// ////////////////////////////////////////////////////////////
	// update list
	updateSmsList();
    }

    @Override
    protected void onPause() {
	super.onPause();

	// ////////////////////////////////////////////////////////////
	// unregeister new SMS receiver
	unregisterReceiver(smsReceiver);

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
	case R.id.preferences:
	    startActivity(new Intent(this, SmsPreferences.class));
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
	case R.id.view:
	    openSms(uri, Intent.ACTION_VIEW);
	    return true;

	case R.id.delete:
	    // TODO
	    return true;

	case R.id.forward:
	    openSms(uri, Intent.ACTION_SEND);
	    return true;

	case R.id.replay:
	    Sms sms = Sms.getSms(getContentResolver(), uri);
	    Uri smsToUri = Uri.parse("smsto:" + sms.address);

	    openSms(smsToUri, Intent.ACTION_SENDTO);
	    return true;

	default:
	    return super.onContextItemSelected(item);
	}
    }

    /************************************************
     * ...
     ************************************************/

    /************************************************
     * functions
     ************************************************/

    public void updateSmsList() {
	try {
	    // remove existing data
	    smsList.clear();

	    // get box uri
	    Spinner spinner = (Spinner) findViewById(R.id.boxSpinner);
	    Uri uri = Uri.parse(Sms.Uris.URIS.get(spinner.getSelectedItemPosition()));

	    // create cursor
	    ContentResolver contentResolver = getContentResolver();
	    Cursor cursor = contentResolver.query(uri, null, null, null, Sms.Fields.DATE + " desc");

	    // set max size
	    int count = cursor == null ? 0 : cursor.getCount();

	    pageIndexMax = (count - 1) / preferences.pageSize;

	    // update page index if too large
	    if (pageIndex > pageIndexMax)
		pageIndex = pageIndexMax;

	    Log.d("OldScoolSMS", "Updateing SMS list: " + count);

	    // check result
	    if (cursor != null && cursor.moveToFirst()) {

		// move to page
		cursor.moveToPosition(preferences.pageSize * pageIndex);

		// read data
		do {
		    // get the SMS
		    Sms sms = Sms.parseSms(cursor);
		    // store
		    smsList.add(sms);
		} while (smsList.size() < preferences.pageSize && cursor.moveToNext());
	    }

	    // notify...
	    ListView smsListView = (ListView) findViewById(R.id.SMSList);
	    @SuppressWarnings("unchecked")
	    ArrayAdapter arrayAdapter = ((ArrayAdapter) smsListView.getAdapter());
	    arrayAdapter.notifyDataSetChanged();
	} catch (Exception e) {
	    showError(e);
	}
    }
}