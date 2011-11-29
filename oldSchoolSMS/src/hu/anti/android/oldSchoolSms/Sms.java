package hu.anti.android.oldSchoolSms;

import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.util.Log;

public class Sms {

    public static class Uris {

	public static final String SMS_URI_BASE = "content://sms/";

	public static final List<String> URIS = new ArrayList<String>();
	static {
	    URIS.add("content://sms/");
	    URIS.add(SMS_URI_BASE + "inbox");
	    URIS.add(SMS_URI_BASE + "sent");
	    URIS.add(SMS_URI_BASE + "draft");
	    URIS.add(SMS_URI_BASE + "status");
	    URIS.add(SMS_URI_BASE + "queued");
	    URIS.add(SMS_URI_BASE + "outbox");
	    URIS.add(SMS_URI_BASE + "undelivered");
	    URIS.add(SMS_URI_BASE + "failed");
	}
    }

    public class Fields {
	public static final String ID = "_id";
	public static final String ADDRESS = "address";
	public static final String PERSON = "person";
	public static final String DATE = "date";
	public static final String READ = "read";
	public static final String STATUS = "status";
	public static final String TYPE = "type";
	public static final String BODY = "body";
	public static final String SEEN = "seen";

	public static final String PROTOCOL = "protocol";
	public static final String REPLY_PATH_PRESENT = "reply_path_present";
	public static final String SUBJECT = "subject";
	public static final String SERVICE_CENTER = "service_center";
	public static final String LOCKED = "locked";
    }

    /**
     * 
     * Sending message:
     * <ul>
     * <li>outbox - failed</li>
     * <li>outbox - undelivered - sent</li>
     * <li>outbox - undelivered - failed</li>
     * </ul>
     * 
     * @author anti
     * 
     */
    public class Type {
	public static final String MESSAGE_TYPE_INBOX = "1";
	public static final String MESSAGE_TYPE_SENT = "2";
	public static final String MESSAGE_TYPE_DRAFT = "3";
	public static final String MESSAGE_TYPE_UNDELIVERED = "4";
	public static final String MESSAGE_TYPE_FAILED = "5";// ?unde
	public static final String MESSAGE_TYPE_OUTBOX = "6";

	// failed 4-5-6
    }

    public class Status {
	public static final String NONE = "-1";
	public static final String COMPLETE = "0";
	public static final String PENDING = "64";
	public static final String FAILED = "128";
    }

    public class Other {
	public static final String SMS_EXTRA_NAME = "pdus";

	public static final int MESSAGE_IS_NOT_READ = 0;
	public static final int MESSAGE_IS_READ = 1;

	public static final int MESSAGE_IS_NOT_SEEN = 0;
	public static final int MESSAGE_IS_SEEN = 1;
    }

    public Long _id;
    public String address;
    public String person;
    public Date date;
    public Boolean read;
    public String status;
    public String type;
    public String body;
    public Boolean seen;
    public String protocol;
    public Boolean reply_path_present;
    public String subject;
    public String service_center;
    public Boolean locked;

    public Sms() {
    }

    public static Sms getSms(ContentResolver contentResolver, long id) {
	Uri uri = ContentUris.withAppendedId(Uri.parse(Sms.Uris.SMS_URI_BASE), id);

	return getSms(contentResolver, uri);
    }

    public static Sms getSms(ContentResolver contentResolver, Uri uri) {
	Cursor cursor = contentResolver.query(uri, null, null, null, null);

	Log.d("OldSchoolSMS", "For [" + uri + "] found [" + cursor.getCount() + "] element(s)");

	if (!cursor.moveToFirst())
	    return new Sms();

	return parseSms(cursor);
    }

    // public static Sms getSms(Uri data) {
    // }

    public static Sms parseSms(Cursor cursor) {
	Sms sms = new Sms();

	sms._id = getColumnLong(cursor, Sms.Fields.ID);
	sms.address = getColumnString(cursor, Sms.Fields.ADDRESS);
	sms.person = getColumnString(cursor, Sms.Fields.PERSON);
	sms.date = new Date(getColumnLong(cursor, Sms.Fields.DATE));
	sms.read = getColumnBoolean(cursor, Sms.Fields.READ);
	sms.status = getColumnString(cursor, Sms.Fields.STATUS);
	sms.type = getColumnString(cursor, Sms.Fields.TYPE);
	sms.body = getColumnString(cursor, Sms.Fields.BODY);
	sms.seen = getColumnBoolean(cursor, Sms.Fields.SEEN);
	sms.protocol = getColumnString(cursor, Sms.Fields.PROTOCOL);
	sms.reply_path_present = getColumnBoolean(cursor, Sms.Fields.REPLY_PATH_PRESENT);
	sms.subject = getColumnString(cursor, Sms.Fields.SUBJECT);
	sms.service_center = getColumnString(cursor, Sms.Fields.SERVICE_CENTER);
	sms.locked = getColumnBoolean(cursor, Sms.Fields.LOCKED);

	return sms;
    }

    private static Boolean getColumnBoolean(Cursor cursor, String column) {
	String string = getColumnString(cursor, column);

	return "1".equals(string);
    }

    private static String getColumnString(Cursor cursor, String column) {
	int index = cursor.getColumnIndex(column);

	if (index == -1)
	    return null;

	return cursor.getString(index);
    }

    private static Long getColumnLong(Cursor cursor, String column) {
	int index = cursor.getColumnIndex(column);

	if (index == -1)
	    return null;

	return cursor.getLong(index);
    }

    @SuppressWarnings("unused")
    private static Integer getColumnInt(Cursor cursor, String column) {
	int index = cursor.getColumnIndex(column);

	if (index == -1)
	    return null;

	return cursor.getInt(index);
    }

    public String getDisplayName(ContentResolver contentResolver) {
	return getDisplayName(contentResolver, address);
    }

    public static String getDisplayName(ContentResolver contentResolver, String phoneNumber) {
	if (phoneNumber == null)
	    return "-";

	// try the number simply...
	String person = getPersonName(contentResolver, phoneNumber);

	if (person != null)
	    return person;

	// url encode
	person = getPersonName(contentResolver, URLEncoder.encode(phoneNumber));

	if (person != null)
	    return person;

	// url decode
	person = getPersonName(contentResolver, URLDecoder.decode(phoneNumber));

	if (person != null)
	    return person;

	// default...
	return "(" + phoneNumber + ")";
    }

    private static String getPersonName(ContentResolver contentResolver, String phoneNumber) {
	if (phoneNumber == null || phoneNumber.length() == 0)
	    return null;

	Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

	Cursor personCursor = contentResolver.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);

	if (personCursor != null && personCursor.moveToFirst())
	    return personCursor.getString(0);

	return null;
    }

    public static String decodeSmsDeliveryStatus(Resources resources, String status) {
	if (status == null)
	    return "";

	else if (Sms.Status.NONE.equals(status))
	    return resources.getString(R.string.SMS_STATUS_NONE);
	else if (Sms.Status.COMPLETE.equals(status))
	    return resources.getString(R.string.SMS_STATUS_COMPLETE);
	else if (Sms.Status.PENDING.equals(status))
	    return resources.getString(R.string.SMS_STATUS_PENDING);
	else if (Sms.Status.FAILED.equals(status))
	    return resources.getString(R.string.SMS_STATUS_FAILED);

	return status;
    }

    public static String decodeSmsSendStatus(Resources resources, int resultCode) {
	switch (resultCode) {
	case Activity.RESULT_OK:
	    return resources.getString(R.string.SMS_SENT_RESULT_OK);

	case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	    return resources.getString(R.string.SMS_SENT_RESULT_ERROR_GENERIC_FAILUREK);

	case SmsManager.RESULT_ERROR_NO_SERVICE:
	    return resources.getString(R.string.SMS_SENT_RESULT_ERROR_NO_SERVICE);

	case SmsManager.RESULT_ERROR_NULL_PDU:
	    return resources.getString(R.string.SMS_SENT_RESULT_ERROR_NO_SERVICE);

	case SmsManager.RESULT_ERROR_RADIO_OFF:
	    return resources.getString(R.string.SMS_SENT_RESULT_ERROR_RADIO_OFF);
	}

	return "resultCode: [" + resultCode + "]";
    }

    @Override
    public String toString() {
	String string = "SMS:\n";

	for (Field field : Sms.class.getFields()) {
	    try {
		string += field.getName() + "= " + field.get(this) + "\n";
	    } catch (Exception e) {
		Log.w("OldSchoolSMS", field.getName(), e);
	    }
	}
	return string;
    }
}
