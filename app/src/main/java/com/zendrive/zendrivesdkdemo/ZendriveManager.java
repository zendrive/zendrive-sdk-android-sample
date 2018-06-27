package com.zendrive.zendrivesdkdemo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

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
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.ZendriveSettingError;
import com.zendrive.sdk.ZendriveSettingWarning;
import com.zendrive.sdk.ZendriveSettings;
import com.zendrive.sdk.ZendriveSettingsCallback;

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
        driveInProgress = true;
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
    }

    /**
     * A previously ended drive is analyzed. Update this in the trip list.
     */
    public void onDriveAnalyzed(DriveInfo driveInfo) {
        TripListDetails tripListDetails = loadTripDetails();
        tripListDetails.updateTrip(driveInfo);
        saveTripDetails(tripListDetails);
        Intent intent = new Intent(Constants.REFRESH_UI);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    public void onDriveResume(DriveResumeInfo driveInfo) {
        driveInProgress = true;
    }

    /**
     * An accident was detected by the Zendrive SDK.
     */
    public void onAccident(AccidentInfo accidentInfo) {
        NotificationUtility.showCollisionNotification(context.getApplicationContext());
        Intent intent = new Intent(context, CollisionDetectedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.ACCIDENT_INFO, accidentInfo);
        context.getApplicationContext().startActivity(intent);
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
    @SuppressLint("newApi") // needed for background restriction. The wrap with
    // Build.VERSION.SDK_INT >= Build.VERSION_CODES.P still spuriously fails lint.
    public void checkZendriveSettings(final Context context) {
        NotificationUtility.cancelErrorAndWarningNotifications(context);
        Zendrive.getZendriveSettings(context, new ZendriveSettingsCallback() {
            @Override
            public void onComplete(@Nullable ZendriveSettings zendriveSettings) {
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
                            notificationManager.notify(NotificationUtility.
                                            LOCATION_PERMISSION_DENIED_NOTIFICATION_ID,
                                    NotificationUtility.
                                            createLocationPermissionDeniedNotification(context));

                            break;
                        }
                        case LOCATION_SETTINGS_ERROR: {
                            notificationManager.notify(NotificationUtility.
                                    LOCATION_DISABLED_NOTIFICATION_ID, NotificationUtility.
                                    createLocationSettingDisabledNotification(context));
                            break;
                        }
                        case WIFI_SCANNING_DISABLED: {
                            notificationManager.notify(NotificationUtility.
                                    WIFI_SCANNING_NOTIFICATION_ID, NotificationUtility.
                                    createWifiScanningDisabledNotification(context));
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
            }
        });
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

    public boolean isDriveInProgress() {
        return driveInProgress;
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
    private boolean driveInProgress = false;
}
