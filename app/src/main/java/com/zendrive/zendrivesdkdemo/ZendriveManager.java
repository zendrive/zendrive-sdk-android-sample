package com.zendrive.zendrivesdkdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.DriveResumeInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.GooglePlaySettingsError;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveDriverAttributes;
import com.zendrive.sdk.ZendriveIssueType;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.ZendriveResolvableError;
import com.zendrive.sdk.ZendriveSettingError;
import com.zendrive.sdk.ZendriveSettingWarning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            if (this == FREE) {
                return "Free";
            }
            return "Paid";
        }
    }

    @SuppressLint("StaticFieldLeak")
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
     *
     * @param configuration Config for initialization.
     * @param setupCallback callback that is invoked after initialization.
     */
    public void initializeZendriveSDK(ZendriveConfiguration configuration,
                                      final ZendriveOperationCallback setupCallback) {
        if (Zendrive.isSDKSetup(context)) {
            ZendriveOperationResult result = ZendriveOperationResult.createSuccess();
            if (setupCallback != null) {
                setupCallback.onCompletion(result);
            }
            return;
        }

        // setup zendrive sdk
        Zendrive.setup(this.context, configuration, ZendriveSdkBroadcastReceiver.class,
                ZendriveSdkNotificationProvider.class,
                setupCallback);
    }

    /**
     * @return Is zendrive sdk initialized.
     */
    public boolean isSdkInitialized() {
        return Zendrive.isSDKSetup(context);
    }

    /**
     * A drive was started by the Zendrive SDK.
     */
    public void onDriveStart(DriveStartInfo driveStartInfo) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.REFRESH_UI));
    }

    /**
     * An ongoing drive ended. Save this drive into the trip list.
     */
    public void onDriveEnd(DriveInfo driveInfo) {
        TripListDetails tripListDetails = loadTripDetails();
        tripListDetails.addOrUpdateTrip(driveInfo);
        saveTripDetails(tripListDetails);
        Intent intent = new Intent(Constants.REFRESH_UI);
        intent.putExtra(Constants.DRIVE_DISTANCE, driveInfo.distanceMeters);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    /**
     * A previously ended drive is analyzed. Update this in the trip list.
     */
    public void onDriveAnalyzed(DriveInfo driveInfo) {
        TripListDetails tripListDetails = loadTripDetails();
        tripListDetails.addOrUpdateTrip(driveInfo);
        saveTripDetails(tripListDetails);
        Intent intent = new Intent(Constants.REFRESH_UI);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    public void onDriveResume(DriveResumeInfo driveInfo) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.REFRESH_UI));
    }

    /**
     * An accident was detected by the Zendrive SDK.
     */
    public void onAccident(AccidentInfo accidentInfo) {
        NotificationUtility.showCollisionNotification(context.getApplicationContext(),
                accidentInfo);
    }

    /**
     * Fetches Zendrive setting errors and warnings if an error or warning was reported by the SDK.
     */
    public void maybeCheckZendriveSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(Constants.SETTING_ERRORS, false) ||
                prefs.getBoolean(Constants.SETTING_WARNINGS, false)) {
            checkZendriveSettings(context);
        }
    }

    /**
     * Query the Zendrive SDK for errors and warnings that affect its normal operation.
     */
    public void checkZendriveSettings(final Context context) {
        NotificationUtility.cancelErrorAndWarningNotifications(context);
        Zendrive.getZendriveSettings(context, zendriveSettings -> {
            if (zendriveSettings == null) {
                // The callback returns NULL if SDK is not setup.
                return;
            }

            NotificationManager notificationManager =
                    (NotificationManager) context.
                            getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                return;
            }

            List<ZendriveIssueType> deniedPermissions = new ArrayList<>();
            for (ZendriveSettingError error : zendriveSettings.errors) {
                switch (error.type) {
                    case POWER_SAVER_MODE_ENABLED: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            notificationManager.notify(NotificationUtility.
                                            PSM_ENABLED_NOTIFICATION_ID,
                                    NotificationUtility.
                                            createPSMEnabledNotification(context, true));
                        } else {
                            throw new RuntimeException("Power saver mode " +
                                    "error on OS version < Lollipop.");
                        }
                        break;
                    }
                    case BACKGROUND_RESTRICTION_ENABLED: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            notificationManager.notify(NotificationUtility.
                                            BACKGROUND_RESTRICTION_NOTIFICATION_ID,
                                    NotificationUtility.
                                            createBackgroundRestrictedNotification(context));
                        } else {
                            throw new RuntimeException("Background restricted " +
                                    "callback on OS version < P.");
                        }
                        break;
                    }
                    case GOOGLE_PLAY_SETTINGS_ERROR: {
                        GooglePlaySettingsError e = (GooglePlaySettingsError) error;
                        Notification notification =
                                NotificationUtility.
                                        createGooglePlaySettingsNotification(context,
                                                e.googlePlaySettingsResult);
                        if (notification != null) {
                            notificationManager.
                                    notify(NotificationUtility.
                                            GOOGLE_PLAY_SETTINGS_NOTIFICATION_ID, notification);
                        }
                        break;
                    }
                    case LOCATION_PERMISSION_DENIED: {
                        deniedPermissions.add(ZendriveIssueType.LOCATION_PERMISSION_DENIED);
                        break;
                    }
                    case BATTERY_OPTIMIZATION_ENABLED: {
                        Notification batteryOptNotification = NotificationUtility.
                                getBatteryOptimizationEnabledNotification(context);
                        notificationManager.notify(NotificationUtility.
                                        BATTERY_OPTIMIZATION_NOTIFICATION_ID,
                                batteryOptNotification);
                        break;
                    }
                    case ONE_PLUS_DEEP_OPTIMIZATION: {
                        ZendriveResolvableError e = (ZendriveResolvableError) error;
                        Notification onePlusOptNotification = NotificationUtility.
                                getOnePlusDeepOptimizationEnabledNotification(context,
                                        e.navigableIntent);
                        notificationManager.notify(NotificationUtility.
                                ONE_PLUS_DEEP_OPTIMIZATION_NOTIFICATION_ID, onePlusOptNotification);
                        break;
                    }
                    case ACTIVITY_RECOGNITION_PERMISSION_DENIED: {
                        deniedPermissions.add(ZendriveIssueType.
                                ACTIVITY_RECOGNITION_PERMISSION_DENIED);
                        break;
                    }
                    case OVERLAY_PERMISSION_DENIED: {
                        notificationManager.notify(NotificationUtility.
                                OVERLAY_PERMISSION_DENIED_NOTIFICATION_ID, NotificationUtility.
                                createOverlayPermissionDeniedNotification(context));
                        break;
                    }
                    case AIRPLANE_MODE_ENABLED: {
                        notificationManager.notify(NotificationUtility.
                                AIRPLANE_MODE_ENABLED_NOTIFICATION_ID,
                                NotificationUtility.createAirplaneModeNotification(context));
                        break;
                    }
                }
            }

            for (ZendriveSettingWarning warning : zendriveSettings.warnings) {
                switch (warning.type) {
                    case POWER_SAVER_MODE_ENABLED: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            notificationManager.notify(NotificationUtility.
                                            PSM_ENABLED_NOTIFICATION_ID,
                                    NotificationUtility.createPSMEnabledNotification(context, false));
                        } else {
                            throw new RuntimeException("Power saver mode " +
                                    "warning on OS version < Lollipop.");
                        }
                        break;
                    }
                }
            }
            if (!deniedPermissions.isEmpty()) {
                showPermissionDeniedNotification(context, notificationManager,
                        deniedPermissions);
            }
        });
    }

    private void showPermissionDeniedNotification(Context context,
                                                  NotificationManager notificationManager,
                                                  List<ZendriveIssueType> deniedPermission) {

        if (deniedPermission.size() == 1) {
            ZendriveIssueType issueType = deniedPermission.get(0);
            if (issueType == ZendriveIssueType.LOCATION_PERMISSION_DENIED) {
                notificationManager.notify(
                        NotificationUtility.LOCATION_PERMISSION_DENIED_NOTIFICATION_ID,
                        NotificationUtility.createLocationPermissionDeniedNotification(context));
            } else if (issueType == ZendriveIssueType.ACTIVITY_RECOGNITION_PERMISSION_DENIED) {
                notificationManager.notify(
                        NotificationUtility.ACTIVITY_PERMISSION_DENIED_NOTIFICATION_ID,
                        NotificationUtility.createActivityPermissionDeniedNotification(context)
                );
            }
        } else {
            ArrayList<String> missingPermissions = new ArrayList<>();
            for (ZendriveIssueType issueType: deniedPermission) {
                if (issueType == ZendriveIssueType.LOCATION_PERMISSION_DENIED) {
                    Collections.addAll(missingPermissions, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Collections.addAll(missingPermissions,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    }
                }
                if (issueType == ZendriveIssueType.ACTIVITY_RECOGNITION_PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        // SDK should not flag this pre Q
                        throw new RuntimeException("Activity permission " +
                                "error on OS version < Q.");
                    }
                    missingPermissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
                }
            }

            notificationManager.cancel(
                    NotificationUtility.LOCATION_PERMISSION_DENIED_NOTIFICATION_ID);
            notificationManager.cancel(
                    NotificationUtility.ACTIVITY_PERMISSION_DENIED_NOTIFICATION_ID);
            notificationManager.notify(
                    NotificationUtility.MULTIPLE_PERMISSION_DENIED_NOTIFICATION_ID,
                    NotificationUtility.createMultiplePermissionsDeniedNotification(context,
                            missingPermissions)
            );
        }
    }

    public ZendriveConfiguration getSavedConfiguration() {
        final String driverId = SharedPreferenceManager.getStringPreference(this.context,
                SharedPreferenceManager.DRIVER_ID_KEY, null);
        if (null == driverId || driverId.equalsIgnoreCase("")) {
            return null;
        }

        ZendriveDriverAttributes userAttributes = new ZendriveDriverAttributes();
        String userType = SharedPreferenceManager.getStringPreference(context.getApplicationContext(),
                SharedPreferenceManager.USER_TYPE, UserType.FREE.name());
        // for paid users zendrive provides special services, which is set here.
        if (userType.equals(UserType.PAID.name())) {
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

    private final Context context;
}
