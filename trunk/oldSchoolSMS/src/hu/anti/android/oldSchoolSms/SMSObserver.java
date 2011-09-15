package hu.anti.android.oldSchoolSms;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public class SMSObserver extends ContentObserver {
    private final Handler smsHandler;
    private final ContentResolver contentResolver;

    public SMSObserver(ContentResolver contentResolver, final Handler smsHandler) {
	super(smsHandler);

	this.contentResolver = contentResolver;
	this.smsHandler = smsHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.ContentObserver#onChange(boolean)
     */
    @Override
    public void onChange(final boolean bSelfChange) {
	super.onChange(bSelfChange);

	Thread thread = new Thread() {
	    private int smsCount;

	    @Override
	    public void run() {
		try {

		    Uri uriSMSURI = Uri.parse(Sms.Uris.SMS_URI_BASE);
		    Cursor cur = contentResolver.query(uriSMSURI, null, null, null, "_id");

		    if (cur.getCount() != smsCount) {
			smsCount = cur.getCount();

			// Send message to Activity
			smsHandler.sendMessage(new Message());
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	};
	thread.start();
    }
}
