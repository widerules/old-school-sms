package hu.anti.android.oldSchoolSms.observer;

import hu.anti.android.oldSchoolSms.Sms;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public abstract class AbstractSmsObserver extends ContentObserver {
    private final Handler smsHandler;
    private final ContentResolver contentResolver;

    public AbstractSmsObserver(ContentResolver contentResolver,
	    final Handler smsHandler) {
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
		Cursor cur = null;
		try {
		    cur = contentResolver.query(getUri(), null, null, null,
			    Sms.Fields.ID);

		    if (cur.getCount() != smsCount) {
			smsCount = cur.getCount();

			// Send message to Activity
			smsHandler.sendMessage(new Message());
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

		if (cur != null)
		    cur.close();
	    }
	};
	thread.start();
    }

    public abstract Uri getUri();

    public Uri getBaseUri() {
	return Uri.parse(Sms.Uris.SMS_URI_BASE);
    }
}
