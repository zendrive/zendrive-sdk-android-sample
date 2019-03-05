package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Helper to prompt user to setup correct Location Settings for Zendrive.
 */
class LocationSettingsHelper {
    static final int REQUEST_CHECK_SETTINGS = 42;


    static void resolveLocationSettings(Activity activity, LocationSettingsResult result) {
        switch (result.getStatus().getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // Should not happen
                Log.e(Constants.LOG_TAG_DEBUG, "Success received when expected error from Google Play Services");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    result.getStatus().startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
                break;
        }
    }
}
