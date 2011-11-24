package hu.anti.android.oldSchoolSms;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

public abstract class AbstractSmsActivity extends Activity {

    public AbstractSmsActivity() {
	super();
    }

    protected void showError(Exception e) {
	try {
	    final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

	    alertDialog.setTitle("Exception");
	    alertDialog.setMessage(e.getLocalizedMessage());
	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		    alertDialog.dismiss();
		    return;
		}
	    });

	    alertDialog.show();
	} catch (Exception e1) {
	    Log.e("OldSchoolSMS", e.getLocalizedMessage());
	    Log.e("OldSchoolSMS", "Error", e);
	}
    }

    protected void openSms(Uri uri, String action) {
	Sms sms = null;

	// if existing SMS
	if (uri != null && uri.toString().startsWith(Sms.Uris.SMS_URI_BASE)) {
	    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
	    if (cursor.moveToFirst())
		sms = Sms.parseSms(cursor);
	}

	Intent intent = new Intent(action);
	intent.setData(uri);

	// setClass
	if (sms == null || Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type) || !action.equals(Intent.ACTION_VIEW)) {
	    intent.setClass(getApplicationContext(), SmsSendActivity.class);
	} else {
	    intent.setAction(Intent.ACTION_VIEW);
	    intent.setClass(getApplicationContext(), SmsViewActivity.class);
	}

	startActivity(intent);
    }

    protected void setText(int id, String text) {
	TextView view = (TextView) findViewById(id);
	view.setText(text);
    }

    protected Uri putNewSmsToDatabase(ContentResolver contentResolver, String address, String body, String type, String status) {
	// Create SMS row
	ContentValues values = new ContentValues();

	values.put(Sms.Fields.ADDRESS, address);
	values.put(Sms.Fields.DATE, "" + new Date().getTime());
	values.put(Sms.Fields.READ, Sms.Other.MESSAGE_IS_READ);
	values.put(Sms.Fields.STATUS, status);
	values.put(Sms.Fields.TYPE, type);
	values.put(Sms.Fields.BODY, body);

	// Push row into the SMS table
	Uri uri = contentResolver.insert(Uri.parse(Sms.Uris.SMS_URI_BASE), values);
	// FIXME reupdate...
	contentResolver.update(uri, values, null, null);

	Cursor cursor = getContentResolver().query(uri, null, null, null, null);
	if (cursor != null && cursor.moveToFirst()) {
	    Sms sms = Sms.parseSms(cursor);
	    Log.d("OldSchoolSMS", "Inserted new SMS objet: [" + sms + "]");
	}

	return uri;
    }
}