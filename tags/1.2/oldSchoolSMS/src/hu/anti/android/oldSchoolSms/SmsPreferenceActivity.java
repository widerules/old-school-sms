package hu.anti.android.oldSchoolSms;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class SmsPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);

	OnPreferenceChangeListener onPatternChangeListener = new OnPreferenceChangeListener() {

	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d("OldSchoolSMS", "onPreferenceChange " + preference + " - " + newValue);

		if (!newValue.toString().matches("^ *[0-9]+( *[,.;/\\- ] *[0-9]+)* *$")) {

		    Toast.makeText(getApplicationContext(), R.string.alertVibrationPattern, Toast.LENGTH_LONG).show();
		    return false;
		}
		return true;
	    }
	};

	findPreference(Preferences.VIBRATOR_PATTERN).setOnPreferenceChangeListener(onPatternChangeListener);
	findPreference(Preferences.DELIVERY_VIBRATOR_PATTERN).setOnPreferenceChangeListener(onPatternChangeListener);
    }
}
