package hu.anti.android.oldSchoolSms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

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
	return sharedPrefs.getString("notificationSound", "DEFAULT_SOUND");
    }

    public String getVibratorPattern() {
	return sharedPrefs.getString("vibratorPattern", "500,200,300");
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
	return sharedPrefs.getString("deliverySound", "DEFAULT_SOUND");
    }

    public String getDeliveryVibratorPattern() {
	return sharedPrefs.getString("deliveryVibratorPattern", "500");
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
}
