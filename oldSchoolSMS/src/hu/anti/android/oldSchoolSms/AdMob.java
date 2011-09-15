package hu.anti.android.oldSchoolSms;

import android.app.Activity;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class AdMob {
    private static final String MY_BANNER_UNIT_ID = "a14e629cd0b493d";

    public static void addView(Activity activity, LinearLayout layout) {

	// Create the adView
	AdView adView = new AdView(activity, AdSize.BANNER, MY_BANNER_UNIT_ID);

	// Add the adView to it
	layout.addView(adView);

	// Initiate a generic request to load it with an ad
	AdRequest request = new AdRequest();

	// add test devices
	request.addTestDevice(AdRequest.TEST_EMULATOR);
	request.addTestDevice("CF95DC53F383F9A836FD749F3EF439CD");

	// load
	adView.loadAd(request);
    }
}
