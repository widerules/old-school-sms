package hu.anti.android.oldSchoolSms;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SmsPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);
    }
}
