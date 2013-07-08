package hu.anti.android.oldSchoolSms.widget;

import hu.anti.android.oldSchoolSms.AbstractSmsActivity;
import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.SmsListActivity.SmsBoxType;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RemoteViews;

public class SmsAppWidgetConfigure extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	// Set the result to CANCELED. This will cause the widget host to cancel
	// out of the widget placement if they press the back button.
	setResult(RESULT_CANCELED);

	super.onCreate(savedInstanceState);
	setContentView(R.layout.widget_settings);

	// ////////////////////////////////////////////////////////////
	// ALL button
	View mainAll = this.findViewById(R.id.mainAll);
	mainAll.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		SmsAppWidgetConfigure.this.onClick(getApplicationContext(),
			SmsBoxType.ALL);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// INBOX button
	View mainInbox = this.findViewById(R.id.mainInbox);
	mainInbox.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		SmsAppWidgetConfigure.this.onClick(getApplicationContext(),
			SmsBoxType.INBOX);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// SENT button
	View mainSent = this.findViewById(R.id.mainSent);
	mainSent.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		SmsAppWidgetConfigure.this.onClick(getApplicationContext(),
			SmsBoxType.SENT);
	    }
	});

	// ////////////////////////////////////////////////////////////
	// DRAFT button
	View mainDraft = this.findViewById(R.id.mainDraft);
	mainDraft.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View paramView) {
		SmsAppWidgetConfigure.this.onClick(getApplicationContext(),
			SmsBoxType.DRAFT);
	    }
	});

    }

    private void onClick(Context context, SmsBoxType smsBox) {
	// get the App Widget ID
	Intent intent = getIntent();
	Bundle extras = intent.getExtras();

	if (extras == null)
	    return;

	int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
		AppWidgetManager.INVALID_APPWIDGET_ID);

	// Perform your App Widget configuration
	SmsAppWidgetProvider.savePref(getApplicationContext(), appWidgetId,
		smsBox);

	// get an instance of the AppWidgetManager
	AppWidgetManager appWidgetManager = AppWidgetManager
		.getInstance(context);

	// Update the App Widget with a RemoteViews layout
	RemoteViews views = new RemoteViews(context.getPackageName(),
		R.layout.widget);
	appWidgetManager.updateAppWidget(appWidgetId, views);

	Log.i(AbstractSmsActivity.OLD_SCHOOL_SMS, "Created new widget "
		+ appWidgetId + " to " + smsBox);

	SmsAppWidgetProvider.updateWidget(context, appWidgetManager,
		appWidgetId);

	// create the return Intent
	Intent resultValue = new Intent();
	resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	setResult(RESULT_OK, resultValue);
	finish();
    }
}
