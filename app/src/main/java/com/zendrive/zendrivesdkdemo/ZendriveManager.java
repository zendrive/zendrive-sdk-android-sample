package com.zendrive.zendrivesdkdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

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
 * This class is an interface between application and zendrive sdk,
 * It collects data from sdk and passes it to application.
 */
public class ZendriveManager {

    private static ZendriveManager sharedInstance;
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


    public static ZendriveManager getSharedInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new ZendriveManager(context);
        }
        return sharedInstance;
    }

    private ZendriveManager(Context context) {
        this.context = context.getApplicationContext();
    }

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


    public boolean isSDKSetup() {
        return Zendrive.isSDKSetup();
    }

    // driver start callback handle.
    public void onDriveStart(DriveStartInfo driveStartInfo) {
        driveInProgress = true;
        Zendrive.startForeground(NotificationUtility.kForegroundModeNotificationId,
                NotificationUtility.getZendriveServiceNotification(this.context));

        Intent intent = new Intent(Constants.DRIVE_START);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    // driver end callback handle.
    public void onDriveEnd(DriveInfo driveInfo) {
        Intent intent = new Intent(Constants.DRIVE_END);
        intent.putExtra(Constants.DRIVE_DISTANCE, driveInfo.distanceMeters);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
        Zendrive.stopForeground(true);
    }

    // accident callback handle.
    public void onAccident(AccidentInfo accidentInfo) {
        Intent intent = new Intent(Constants.ACCIDENT);
        intent.putExtra(Constants.ACCIDENT_CONFIDENCE, accidentInfo.confidence);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    // location permission callback handle.
    public void onLocationPermissionsChange(boolean granted) {
        this.isLocationPermissionGranted = granted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            displayOrHideLocationPermissionNotification();
            Intent intent = new Intent(Constants.EVENT_LOCATION_PERMISSION_CHANGE);
            intent.putExtra(Constants.EVENT_LOCATION_PERMISSION_CHANGE, granted);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);

        } else {
            throw new RuntimeException("Callback on non marshmallow sdk");
        }

    }

    // location setting callback handle.
    public void onLocationSettingsChange(boolean locationEnabled) {
        this.isLocationSettingsEnabled = locationEnabled;
        displayOrHideLocationSettingNotification();
        Intent intent = new Intent(Constants.EVENT_LOCATION_SETTING_CHANGE);
        intent.putExtra(Constants.EVENT_LOCATION_SETTING_CHANGE, locationEnabled);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    public ZendriveConfiguration getSavedConfiguration(){

        final String driverId = SharedPreferenceManager.getStringPreference(this.context,
                Constants.DRIVER_ID_KEY, null);
        if (null == driverId || driverId.equalsIgnoreCase("")) {
            return null;
        }

        ZendriveDriverAttributes userAttributes = new ZendriveDriverAttributes();
        String userType = SharedPreferenceManager.getStringPreference(context.getApplicationContext(),
                Constants.USER_TYPE, UserType.FREE.toString());
        // for paid users zendrive provides special services, which is set here.
        if(userType.equals(UserType.PAID.toString())) {
            //userAttributes.setServiceLevel(ZendriveDriverAttributes.ServiceLevel.LEVEL_1);
        } else {
            //userAttributes.setServiceLevel(ZendriveDriverAttributes.ServiceLevel.LEVEL_DEFAULT);
        }
        ZendriveDriveDetectionMode driveDetectionMode =
                SharedPreferenceManager.getZendriveAutoDetectionMode(this.context);
        final ZendriveConfiguration configuration = new ZendriveConfiguration(Constants.zendriveSDKKey,
                driverId, driveDetectionMode, ZendriveAccidentDetectionMode.ENABLED);
        configuration.setDriverAttributes(userAttributes);
        return configuration;
    }

    public String getDriverId(){
        String driverId = SharedPreferenceManager.getStringPreference(this.context,
                Constants.DRIVER_ID_KEY, null);
        if (null == driverId || driverId.equalsIgnoreCase("")) {
            return null;
        }
        return driverId;
    }

    public void clearSettings(){
        SharedPreferenceManager.removePreference(context, Constants.DRIVER_ID_KEY);
        SharedPreferenceManager.removeZendriveAutoDetectionMode(context);
        SharedPreferenceManager.removePreference(context, Constants.USER_TYPE);
    }

    public boolean isDriveInProgress(){
        return driveInProgress;
    }

    private void displayOrHideLocationSettingNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (this.isLocationSettingsEnabled) {
            // Remove the displayed notification if any
            mNotificationManager.cancel(NotificationUtility.kLocationDisabledNotificationId);
        } else {
            // Notify user
            Notification notification = NotificationUtility.getLocationDisabledNotification(context);
            mNotificationManager.notify(NotificationUtility.kLocationDisabledNotificationId, notification);
        }
    }


    private void displayOrHideLocationPermissionNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (this.isLocationPermissionGranted) {
            // Remove the displayed notification if any
            mNotificationManager.cancel(NotificationUtility.kLocationPermissionDeniedNotificationId);
        } else {
            // Notify user
            Notification notification = NotificationUtility.getLocationPermissionDeniedNotification(context);
            mNotificationManager.notify(NotificationUtility.kLocationPermissionDeniedNotificationId, notification);
        }
    }
    private boolean isLocationSettingsEnabled = true;
    private boolean isLocationPermissionGranted = true;
    private Context context;
    private boolean driveInProgress = false;

}
