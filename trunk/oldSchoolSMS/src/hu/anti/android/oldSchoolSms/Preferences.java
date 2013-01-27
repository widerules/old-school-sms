package hu.anti.android.oldSchoolSms;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences {

    public static final String DEFAULT_SOUND = "DEFAULT_SOUND";

    public static final String VIBRATOR_PATTERN = "vibratorPattern";
    public static final String DefaultVibratorPattern = "500,200,300";

    public static final String DELIVERY_VIBRATOR_PATTERN = "deliveryVibratorPattern";
    public static final String DefaultDeliveryVibratorPattern = "500";

    private final SharedPreferences sharedPrefs;

    public Preferences(Context context) {
	sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getShowMessageContentInList() {
	return sharedPrefs.getBoolean("showMessageContentInList", true);
    }

    public int getPageSize() {
	return Integer.parseInt(sharedPrefs.getString("pageSize", "50"));
    }

    public boolean getNotifyOnNewSms() {
	return sharedPrefs.getBoolean("notifyOnNewSms", true);
    }

    public String getNotificationSound() {
	return sharedPrefs.getString("notificationSound", DEFAULT_SOUND);
    }

    public long[] getVibratorPattern() {
	long[] pattern;
	try {
	    pattern = extractPattern(sharedPrefs.getString(VIBRATOR_PATTERN,
		    DefaultVibratorPattern));
	} catch (Exception e) {
	    Log.e("OldSchoolSMS",
		    "Wrong vibration pattern: " + e.getLocalizedMessage());

	    pattern = extractPattern(Preferences.DefaultVibratorPattern);
	}

	return pattern;
    }

    public boolean getNotifyOnSuccessfulSend() {
	return sharedPrefs.getBoolean("notifyOnSuccessfulSend", true);
    }

    public boolean getNotifyOnDelivery() {
	return sharedPrefs.getBoolean("notifyOnDelivery", true);
    }

    public boolean getPopupOnNewSms() {
	return sharedPrefs.getBoolean("popupOnNewSms", true);
    }

    public String getDeliverySound() {
	return sharedPrefs.getString("deliverySound", DEFAULT_SOUND);
    }

    public long[] getDeliveryVibratorPattern() {
	long[] pattern;
	try {
	    pattern = extractPattern(sharedPrefs.getString(
		    DELIVERY_VIBRATOR_PATTERN, DefaultDeliveryVibratorPattern));
	} catch (Exception e) {
	    Log.e("OldSchoolSMS",
		    "Wrong vibration pattern: " + e.getLocalizedMessage());

	    pattern = extractPattern(Preferences.DefaultDeliveryVibratorPattern);
	}

	return pattern;
    }

    public String getSmsBox() {
	return sharedPrefs.getString("smsBox", "ALL");
    }

    public boolean getDebug() {
	return sharedPrefs.getBoolean("debug", false);
    }

    public boolean getDebugModeSmsList() {
	return getDebug() && sharedPrefs.getBoolean("debugModeSmsList", false);
    }

    public boolean getExtendedBoxList() {
	return getDebug() && sharedPrefs.getBoolean("extendedBoxList", false);
    }

    private long[] extractPattern(String vibratorPattern) {
	// extract pattern
	String[] split = vibratorPattern.split("[,.;/\\- ]");
	// create vibration pattern holder
	long[] pattern = new long[(split.length + 1)];
	// set starting wait to 0ms
	pattern[0] = 0;
	// decode pattern string
	for (int i = 0; i < split.length; i++) {
	    pattern[i + 1] = Long.parseLong(split[i]);
	}
	return pattern;
    }

    public String getLanguage() {
	String string = sharedPrefs.getString("language", null);

	return string == null ? Locale.getDefault().getCountry() : string;
    }
}
