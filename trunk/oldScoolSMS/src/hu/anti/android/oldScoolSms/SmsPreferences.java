package hu.anti.android.oldScoolSms;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SmsPreferences extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);
    }
}
