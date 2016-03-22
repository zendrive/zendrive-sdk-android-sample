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
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveAccidentDetectionMode;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveDriverAttributes;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.ZendriveSetupCallback;

/**
 * Wrapper class for the Zendrive SDK.
 * This manages the interactions of the app with the Zendrive SDK such as initialization,
 * teardown and SDK callbacks.
 */
public class ZendriveManager {

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
                                      final ZendriveSetupCallback setupCallback) {
        if (Zendrive.isSDKSetup()) {
            ZendriveOperationResult result = ZendriveOperationResult.createSuccess();
            if (setupCallback != null) {
                setupCallback.onSetup(result);
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
        Zendrive.startForeground(NotificationUtility.kForegroundModeNotificationId,
                NotificationUtility.createZendriveForegroundServiceNotification(this.context));

        Intent intent = new Intent(Constants.DRIVE_START);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    /**
     * An ongoing drive ended. Save this drive into the trip list.
     */
    public void onDriveEnd(DriveInfo driveInfo) {
        TripListDetails tripListDetails = loadTripDetails();
        tripListDetails.addTrip(driveInfo);
        saveTripDetails(tripListDetails);
        Intent intent = new Intent(Constants.DRIVE_END);
        intent.putExtra(Constants.DRIVE_DISTANCE, driveInfo.distanceMeters);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
        Zendrive.stopForeground(true);
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
     */
    public void onLocationSettingsChange(boolean locationEnabled) {
        displayOrHideLocationSettingNotification(locationEnabled);
        Intent intent = new Intent(Constants.EVENT_LOCATION_SETTING_CHANGE);
        intent.putExtra(Constants.EVENT_LOCATION_SETTING_CHANGE, locationEnabled);
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
                driverId, driveDetectionMode, ZendriveAccidentDetectionMode.ENABLED);
        configuration.setDriverAttributes(userAttributes);
        return configuration;
    }

    public boolean isDriveInProgress(){
        return driveInProgress;
    }

    private void displayOrHideLocationSettingNotification(boolean isLocationSettingsEnabled) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (isLocationSettingsEnabled) {
            // Remove the displayed notification if any
            mNotificationManager.cancel(NotificationUtility.kLocationDisabledNotificationId);
        } else {
            // Notify user
            Notification notification = NotificationUtility.createLocationSettingDisabledNotification(context);
            mNotificationManager.notify(NotificationUtility.kLocationDisabledNotificationId, notification);
        }
    }


    private void displayOrHideLocationPermissionNotification(boolean isLocationPermissionGranted) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (isLocationPermissionGranted) {
            // Remove the displayed notification if any
            mNotificationManager.cancel(NotificationUtility.kLocationPermissionDeniedNotificationId);
        } else {
            // Notify user
            Notification notification = NotificationUtility.createLocationPermissionDeniedNotification(context);
            mNotificationManager.notify(NotificationUtility.kLocationPermissionDeniedNotificationId, notification);
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
