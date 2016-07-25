package com.zendrive.zendrivesdkdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.DriveResumeInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveDriverAttributes;
import com.zendrive.sdk.ZendriveLocationSettingsResult;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;

/**
 * Wrapper class for the Zendrive SDK.
 * This manages the interactions of the app with the Zendrive SDK such as initialization,
 * teardown and SDK callbacks.
 */
public class ZendriveManager {

    public enum UserType {
        /**
         * Default user type.
         */
        FREE,

        /**
         * Paid users.
         */
        PAID;

        public String toString() {
            if(this == FREE){
                return "Free";
            }
            return "Paid";
        }
    }

    private static ZendriveManager sharedInstance;

    public static ZendriveManager getSharedInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new ZendriveManager(context);
        }
        return sharedInstance;
    }

    private ZendriveManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Initialize Zendrive with the given configuration.
     * @param configuration Config for initialization.
     * @param setupCallback callback that is invoked after initialization.
     */
    public void initializeZendriveSDK(ZendriveConfiguration configuration,
                                      final ZendriveOperationCallback setupCallback) {
        if (Zendrive.isSDKSetup()) {
            ZendriveOperationResult result = ZendriveOperationResult.createSuccess();
            if (setupCallback != null) {
                setupCallback.onCompletion(result);
            }
            return;
        }

        // setup zendrive sdk
        Zendrive.setup(this.context, configuration, ZendriveSdkIntentService.class,
                setupCallback);
    }

    /**
     * @return Is zendrive sdk initialized.
     */
    public boolean isSdkInitialized() {
        return Zendrive.isSDKSetup();
    }

    /**
     * A drive was started by the Zendrive SDK.
     */
    public void onDriveStart(DriveStartInfo driveStartInfo) {
        driveInProgress = true;
        Zendrive.startForeground(NotificationUtility.FOREGROUND_MODE_NOTIFICATION_ID,
                NotificationUtility.createZendriveForegroundServiceNotification(context));

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.REFRESH_UI));
    }

    /**
     * An ongoing drive ended. Save this drive into the trip list.
     */
    public void onDriveEnd(DriveInfo driveInfo) {
        driveInProgress = false;
        TripListDetails tripListDetails = loadTripDetails();
        tripListDetails.addTrip(driveInfo);
        saveTripDetails(tripListDetails);
        Intent intent = new Intent(Constants.REFRESH_UI);
        intent.putExtra(Constants.DRIVE_DISTANCE, driveInfo.distanceMeters);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
        Zendrive.stopForeground(true);
    }

    public void onDriveResume(DriveResumeInfo driveInfo) {
        driveInProgress = true;
    }

    /**
     * An accident was detected by the Zendrive SDK.
     */
    public void onAccident(AccidentInfo accidentInfo) {
        Intent intent = new Intent(Constants.ACCIDENT);
        intent.putExtra(Constants.ACCIDENT_ID, accidentInfo.accidentId);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    /**
     * Location permission of the app changed.
     */
    public void onLocationPermissionsChange(boolean granted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            displayOrHideLocationPermissionNotification(granted);
            Intent intent = new Intent(Constants.EVENT_LOCATION_PERMISSION_CHANGE);
            intent.putExtra(Constants.EVENT_LOCATION_PERMISSION_CHANGE, granted);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);

        } else {
            throw new RuntimeException("Callback on non marshmallow sdk");
        }
    }

    /**
     * Location settings on the device changed.
     * @param locationSettingsResult
     */
    public void onLocationSettingsChange(ZendriveLocationSettingsResult locationSettingsResult) {
        displayOrHideLocationSettingNotification(locationSettingsResult);
        Intent intent = new Intent(Constants.EVENT_LOCATION_SETTING_CHANGE);
        intent.putExtra("DATA", locationSettingsResult);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);

    }

    public ZendriveConfiguration getSavedConfiguration(){
        final String driverId = SharedPreferenceManager.getStringPreference(this.context,
                SharedPreferenceManager.DRIVER_ID_KEY, null);
        if (null == driverId || driverId.equalsIgnoreCase("")) {
            return null;
        }

        ZendriveDriverAttributes userAttributes = new ZendriveDriverAttributes();
        String userType = SharedPreferenceManager.getStringPreference(context.getApplicationContext(),
                SharedPreferenceManager.USER_TYPE, UserType.FREE.name());
        // for paid users zendrive provides special services, which is set here.
        if(userType.equals(UserType.PAID.name())) {
            userAttributes.setServiceLevel(ZendriveDriverAttributes.ServiceLevel.LEVEL_1);
        } else {
            userAttributes.setServiceLevel(ZendriveDriverAttributes.ServiceLevel.LEVEL_DEFAULT);
        }
        ZendriveDriveDetectionMode driveDetectionMode =
                SharedPreferenceManager.getZendriveAutoDetectionMode(this.context);
        final ZendriveConfiguration configuration = new ZendriveConfiguration(Constants.zendriveSDKKey,
                driverId, driveDetectionMode);
        configuration.setDriverAttributes(userAttributes);
        return configuration;
    }

    public boolean isDriveInProgress(){
        return driveInProgress;
    }

    private void displayOrHideLocationSettingNotification(
            ZendriveLocationSettingsResult locationSettingsResult) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (locationSettingsResult.isSuccess()) {
            // Remove the displayed notification if any
            mNotificationManager.cancel(NotificationUtility.LOCATION_DISABLED_NOTIFICATION_ID);
        }
        else {
            // Notify user
            Notification notification =
                    NotificationUtility.createLocationSettingDisabledNotification(context);
            mNotificationManager.notify(
                    NotificationUtility.LOCATION_DISABLED_NOTIFICATION_ID, notification);
        }
    }



    private void displayOrHideLocationPermissionNotification(boolean isLocationPermissionGranted) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (isLocationPermissionGranted) {
            // Remove the displayed notification if any
            mNotificationManager.cancel(NotificationUtility.LOCATION_PERMISSION_DENIED_NOTIFICATION_ID);
        } else {
            // Notify user
            Notification notification = NotificationUtility.createLocationPermissionDeniedNotification(context);
            mNotificationManager.notify(NotificationUtility.LOCATION_PERMISSION_DENIED_NOTIFICATION_ID, notification);
        }
    }

    private TripListDetails loadTripDetails() {
        String tripDetailsJsonString = SharedPreferenceManager.getStringPreference(this.context,
                SharedPreferenceManager.TRIP_DETAILS_KEY, null);
        if (null == tripDetailsJsonString) {
            return new TripListDetails();
        }
        return new Gson().fromJson(tripDetailsJsonString, TripListDetails.class);
    }

    private void saveTripDetails(TripListDetails tripListDetails) {
        String tripListDetailsJsonString = new Gson().toJson(tripListDetails);
        SharedPreferenceManager.setPreference(this.context, SharedPreferenceManager.TRIP_DETAILS_KEY,
                tripListDetailsJsonString);
    }

    private Context context;
    private boolean driveInProgress = false;
}
