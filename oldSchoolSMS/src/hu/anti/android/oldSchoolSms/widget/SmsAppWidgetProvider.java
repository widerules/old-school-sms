package hu.anti.android.oldSchoolSms.widget;

import hu.anti.android.oldSchoolSms.AbstractSmsActivity;
import hu.anti.android.oldSchoolSms.R;
import hu.anti.android.oldSchoolSms.SmsListActivity;
import hu.anti.android.oldSchoolSms.SmsListActivity.SmsBoxType;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

public class SmsAppWidgetProvider extends AppWidgetProvider {
    private static final String PREFS_NAME = "hu.anti.android.oldSchoolSms.widget.SmsAppWidgetProvider";
    private static final String PREF_KEY = "widget_";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	    int[] appWidgetIds) {
	// Perform this loop procedure for each App Widget that belongs to this
	// provider
	for (int i = 0; i < appWidgetIds.length; i++) {
	    int appWidgetId = appWidgetIds[i];

	    updateWidget(context, appWidgetManager, appWidgetId);
	}
    }

    public static void updateWidget(Context context,
	    AppWidgetManager appWidgetManager, int appWidgetId) {
	SmsBoxType smsBox = loadPref(context, appWidgetId);

	// Create an Intent to launch Activity
	Intent intent = new Intent();

	intent.setClass(context, SmsListActivity.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		& Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
		& Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	intent.putExtra(SmsListActivity.SMS_BOX, smsBox.name());

	PendingIntent pendingIntent = PendingIntent.getActivity(context,
		appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

	// Get the layout for the App Widget and attach an on-click listener
	// to the button
	RemoteViews widget = new RemoteViews(context.getPackageName(),
		R.layout.widget);
	// button
	widget.setImageViewResource(R.id.widgetButton,
		getSmsBoxIcon(context, smsBox));
	widget.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
	// label
	widget.setCharSequence(R.id.widgetLabel, "setText",
		getSmsBoxName(context, smsBox));

	// Button b=null;
	// b.setBackground(context.getResources().getDrawable(R.drawable.icon));

	// Tell the AppWidgetManager to perform an update on the current app
	// widget
	appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    // Write the prefix to the SharedPreferences object for this widget
    public static void savePref(Context context, int appWidgetId,
	    SmsBoxType smsBox) {
	SharedPreferences.Editor prefs = context.getSharedPreferences(
		PREFS_NAME, 0).edit();
	prefs.putString(PREF_KEY + appWidgetId, smsBox.name());
	prefs.commit();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static SmsBoxType loadPref(Context context, int appWidgetId) {
	SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
	String string = prefs.getString(PREF_KEY + appWidgetId, null);
	if (string != null) {
	    return SmsBoxType.valueOf(string);
	} else {
	    Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS,
		    "Not found pref for widget " + appWidgetId
			    + " useing defult: ALL");

	    return SmsBoxType.ALL;
	}
    }

    protected static String getSmsBoxName(Context context, SmsBoxType smsBox) {
	switch (smsBox) {
	case ALL:
	    return context.getString(R.string.BoxAll);
	case INBOX:
	    return context.getString(R.string.BoxInbox);
	case SENT:
	    return context.getString(R.string.BoxSent);
	case DRAFT:
	    return context.getString(R.string.BoxDraft);

	default:
	    return context.getString(R.string.app_name);
	}
    }

    protected static int getSmsBoxIcon(Context context, SmsBoxType smsBox) {
	switch (smsBox) {
	case ALL:
	    return R.drawable.all;
	case INBOX:
	    return R.drawable.inbox;
	case SENT:
	    return R.drawable.outbox;
	case DRAFT:
	    return R.drawable.draft;

	default:
	    return R.drawable.icon;
	}
    }
}
