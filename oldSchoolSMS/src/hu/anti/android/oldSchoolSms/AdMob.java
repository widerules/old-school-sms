package hu.anti.android.oldSchoolSms;

import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class AdMob {
    private static final String MY_BANNER_UNIT_ID = "a14e629cd0b493d";

    public static void addView(Activity activity, LinearLayout layout) {
	if (layout == null)
	    return;

	// Create the adView
	final AdView adView = new AdView(activity, AdSize.SMART_BANNER, MY_BANNER_UNIT_ID);

	// Add the adView to it
	layout.addView(adView);

	// load
	adView.loadAd(getAdRequest());
    }

    public static void removeView(Activity activity, LinearLayout layout) {
	if (layout != null)
	    layout.removeAllViews();
    }

    private static AdRequest getAdRequest() {
	// Initiate a generic request to load it with an ad
	AdRequest request = new AdRequest();

	// add test devices
	request.addTestDevice(AdRequest.TEST_EMULATOR);
	request.addTestDevice("CF95DC53F383F9A836FD749F3EF439CD");

	Log.d("OldSchoolSMS", "New AdRequest: " + request);

	return request;
    }
}
