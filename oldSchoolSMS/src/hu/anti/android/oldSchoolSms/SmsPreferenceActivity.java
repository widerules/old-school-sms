package hu.anti.android.oldSchoolSms;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class SmsPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);

	// ////////////////////////////////////////////////////////////
	// onPatternChangeListener
	OnPreferenceChangeListener onPatternChangeListener = new OnPreferenceChangeListener() {

	    @Override
	    public boolean onPreferenceChange(Preference preference,
		    Object newValue) {
		Log.d(AbstractSmsActivity.OLD_SCHOOL_SMS, "onPreferenceChange "
			+ preference + " - " + newValue);

		if (!newValue.toString().matches(
			"^ *[0-9]+( *[,.;/\\- ] *[0-9]+)* *$")) {

		    Toast.makeText(getApplicationContext(),
			    R.string.alertVibrationPattern, Toast.LENGTH_LONG)
			    .show();
		    return false;
		}
		return true;
	    }
	};

	findPreference(Preferences.VIBRATOR_PATTERN)
		.setOnPreferenceChangeListener(onPatternChangeListener);
	findPreference(Preferences.DELIVERY_VIBRATOR_PATTERN)
		.setOnPreferenceChangeListener(onPatternChangeListener);

	// ////////////////////////////////////////////////////////////
	// OnPreferenceClickListener
	findPreference("web").setOnPreferenceClickListener(
		new OnPreferenceClickListener() {
		    @Override
		    public boolean onPreferenceClick(Preference preference) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(SmsPreferenceActivity.this
					.getString(R.string.webSummary)));
			startActivity(browserIntent);
			return true;
		    }
		});
	findPreference("email").setOnPreferenceClickListener(
		new OnPreferenceClickListener() {
		    @Override
		    public boolean onPreferenceClick(Preference preference) {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);

			emailIntent.setType("plain/text");
			emailIntent.putExtra(
				android.content.Intent.EXTRA_EMAIL,
				new String[] { SmsPreferenceActivity.this
					.getString(R.string.emailSummary) });
			emailIntent
				.putExtra(
					android.content.Intent.EXTRA_SUBJECT,
					SmsPreferenceActivity.this
						.getString(R.string.app_name)
						+ " v"
						+ SmsPreferenceActivity.this
							.getString(R.string.versionName));
			// emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
			// "Text");

			/* Send it off to the Activity-Chooser */
			startActivity(Intent.createChooser(emailIntent,
				"Send mail..."));
			return true;
		    }
		});
	findPreference("fb").setOnPreferenceClickListener(
		new OnPreferenceClickListener() {
		    @Override
		    public boolean onPreferenceClick(Preference preference) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(SmsPreferenceActivity.this
					.getString(R.string.fbSummary)));
			startActivity(browserIntent);
			return true;
		    }
		});
    }
}
