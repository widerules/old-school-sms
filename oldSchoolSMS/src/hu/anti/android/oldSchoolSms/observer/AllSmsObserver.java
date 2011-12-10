package hu.anti.android.oldSchoolSms.observer;

import hu.anti.android.oldSchoolSms.Sms;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;

public class AllSmsObserver extends AbstractSmsObserver {
    private final Uri uri;

    public AllSmsObserver(ContentResolver contentResolver, Handler smsHandler) {
	super(contentResolver, smsHandler);
	this.uri = Uri.parse(Sms.Uris.SMS_URI_BASE);
    }

    @Override
    public Uri getUri() {
	return uri;
    }
}
