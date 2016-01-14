package com.zendrive.zendrivesdkdemo;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.ZendriveIntentService;

/**
 * Service which listen to zendrive sdk notifications and passes it to ZendriveManager.
 */
public class ZendriveSdkNotificationService extends ZendriveIntentService {
    public ZendriveSdkNotificationService() {
        super("ZDService");
    }

    @Override
    public void onCreate() {
        Log.d(Constants.LOG_TAG_DEBUG, " ZendriveSdkNotificationService Created");
        super.onCreate();
        this.sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        this.tripListDetails = loadTripDetails();
    }

    @Override
    public void onDriveStart(DriveStartInfo startInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive Start");
        ZendriveManager.getSharedInstance(getApplicationContext()).onDriveStart(startInfo);
    }

    @Override
    public void onDriveEnd(DriveInfo driveInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive End");
        this.tripListDetails.addTrip(driveInfo);
        saveTripDetails(this.tripListDetails);
        ZendriveManager.getSharedInstance(getApplicationContext()).onDriveEnd(driveInfo);
    }

    @Override
    public void onAccident(AccidentInfo accidentInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Accident Detected");
        ZendriveManager.getSharedInstance(getApplicationContext()).onAccident(accidentInfo);
    }

    @Override
    public void onLocationPermissionsChange(boolean granted) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Location Permission : " +
                granted);
        ZendriveManager.getSharedInstance(getApplicationContext()).onLocationPermissionsChange(granted);
    }

    @Override
    public void onLocationSettingsChange(boolean enabled) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Location Setting : " + enabled);
        ZendriveManager.getSharedInstance(getApplicationContext()).onLocationSettingsChange(enabled);
    }

    public TripListDetails loadTripDetails() {
        String tripDetailsJsonString = sharedPreferences.getString(Constants.TRIP_DETAILS_KEY, null);
        if (null == tripDetailsJsonString) {
            return new TripListDetails();
        }
        return new Gson().fromJson(tripDetailsJsonString, TripListDetails.class);
    }

    private void saveTripDetails(TripListDetails tripListDetails) {
        String tripListDetailsJsonString = new Gson().toJson(tripListDetails);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.TRIP_DETAILS_KEY, tripListDetailsJsonString);
        editor.apply();
    }

    private TripListDetails tripListDetails;

    private SharedPreferences sharedPreferences;
}
