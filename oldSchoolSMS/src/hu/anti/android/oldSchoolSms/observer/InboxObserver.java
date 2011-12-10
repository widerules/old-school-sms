package hu.anti.android.oldSchoolSms.observer;

import hu.anti.android.oldSchoolSms.Sms;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;

public class InboxObserver extends AbstractSmsObserver {
    private final Uri uri;

    public InboxObserver(ContentResolver contentResolver, Handler smsHandler) {
	super(contentResolver, smsHandler);
	this.uri = Uri.parse(Sms.Uris.SMS_URI_BASE + "inbox");
    }

    @Override
    public Uri getUri() {
	return uri;
    }
}
