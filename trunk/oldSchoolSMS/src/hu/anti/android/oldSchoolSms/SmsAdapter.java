package hu.anti.android.oldSchoolSms;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TwoLineListItem;

public class SmsAdapter extends ArrayAdapter<Sms> {

    private final List<Sms> objects;

    private boolean showMessageContentInList;

    public SmsAdapter(Context context, List<Sms> objects) {
	super(context, android.R.layout.simple_list_item_2, objects);

	this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	// get sms object
	Sms sms = objects.get(position);

	// if new, create it
	if (convertView == null) {
	    LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    convertView = li.inflate(android.R.layout.simple_list_item_2, null);
	}

	// list item
	TwoLineListItem listItem = (TwoLineListItem) convertView;

	// sender
	String person = sms.getDisplayName(getContext().getContentResolver());
	listItem.getText1().setText(person);

	// sms date
	String text2 = sms.date.toLocaleString();

	// message
	if (showMessageContentInList) {
	    text2 += "\n" + sms.body;
	}

	// second line text
	listItem.getText2().setText(text2);

	// sent-received
	if (Sms.Type.MESSAGE_TYPE_INBOX.equals(sms.type))
	    listItem.getText1().setGravity(Gravity.LEFT);
	else if (Sms.Type.MESSAGE_TYPE_OUTBOX.equals(sms.type) || Sms.Type.MESSAGE_TYPE_FAILED.equals(sms.type) || Sms.Type.MESSAGE_TYPE_SENT.equals(sms.type)
		|| Sms.Type.MESSAGE_TYPE_UNDELIVERED.equals(sms.type))
	    listItem.getText1().setGravity(Gravity.RIGHT);
	else
	    listItem.getText1().setGravity(Gravity.CENTER);

	// read-unread
	int textStyle = R.style.normalSms;
	if (Sms.Type.MESSAGE_TYPE_INBOX.equals(sms.type) && sms.read != null && !sms.read)
	    textStyle = R.style.unreadSms;
	else
	    textStyle = R.style.normalSms;
	listItem.getText1().setTextAppearance(getContext(), textStyle);

	// Log.d("OldSchoolSMS", "SMS type: [" + sms.type + "] on:" + sms);

	// finish
	return convertView;
    }

    public void setShowMessageContentInList(boolean showMessageContentInList) {
	this.showMessageContentInList = showMessageContentInList;
    }
}
