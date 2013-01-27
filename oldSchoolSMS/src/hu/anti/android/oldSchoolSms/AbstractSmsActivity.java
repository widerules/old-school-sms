package hu.anti.android.oldSchoolSms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

public abstract class AbstractSmsActivity extends Activity {

    public static final String OLD_SCHOOL_SMS = "OldSchoolSMS";

    public AbstractSmsActivity() {
	super();
    }

    protected void showError(Exception e) {
	try {
	    final AlertDialog alertDialog = new AlertDialog.Builder(this)
		    .create();

	    alertDialog.setTitle("Exception");
	    alertDialog.setMessage(e.getLocalizedMessage());
	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		    alertDialog.dismiss();
		    return;
		}
	    });

	    alertDialog.show();
	} catch (Exception e1) {
	    Log.e(OLD_SCHOOL_SMS, e.getLocalizedMessage());
	    Log.e(OLD_SCHOOL_SMS, "Error", e);
	}
    }

    protected void openSms(Uri uri, String action) {
	Sms sms = null;

	// if existing SMS
	if (uri != null && uri.toString().startsWith(Sms.Uris.SMS_URI_BASE)) {
	    Cursor cursor = getContentResolver().query(uri, null, null, null,
		    null);

	    if (cursor != null) {
		if (cursor.moveToFirst())
		    sms = Sms.parseSms(cursor);
		cursor.close();
	    }
	}

	Intent intent = new Intent(action);
	intent.setData(uri);

	// setClass
	if (sms == null || Sms.Type.MESSAGE_TYPE_DRAFT.equals(sms.type)
		|| !action.equals(Intent.ACTION_VIEW)) {
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

    protected AlertDialog.Builder createDeletAlert() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);

	builder.setIcon(android.R.drawable.ic_dialog_alert);
	builder.setTitle(R.string.questionDelete);
	builder.setCancelable(true);
	builder.setNegativeButton(android.R.string.no,
		new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialogInterface,
			    int arg1) {
			dialogInterface.dismiss();
		    }
		});

	return builder;
    }

    protected AlertDialog.Builder createResendAlert() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);

	builder.setIcon(android.R.drawable.ic_dialog_alert);
	builder.setTitle(R.string.questionResend);
	builder.setCancelable(true);
	builder.setNegativeButton(android.R.string.no,
		new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialogInterface,
			    int arg1) {
			dialogInterface.dismiss();
		    }
		});

	return builder;
    }

    protected AlertDialog.Builder createSendAlert() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);

	builder.setIcon(android.R.drawable.ic_dialog_alert);
	builder.setTitle(R.string.questionSend);
	builder.setCancelable(true);
	builder.setNegativeButton(android.R.string.no,
		new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialogInterface,
			    int arg1) {
			dialogInterface.dismiss();
		    }
		});

	return builder;
    }
}