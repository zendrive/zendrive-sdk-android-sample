package com.zendrive.zendrivesdkdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.LocationSettingsResult;
import com.zendrive.sdk.AccidentInfo;

import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;


/**
 * Utility to create notifications to show to the user when the Zendrive SDK has something
 * interesting to report.
 */
public class NotificationUtility {
    // Notification related constants
    public static final int FOREGROUND_MODE_NOTIFICATION_ID = 98;
    public static final int LOCATION_DISABLED_NOTIFICATION_ID = 99;
    public static final int LOCATION_PERMISSION_DENIED_NOTIFICATION_ID = 100;

    public static final int PSM_ENABLED_NOTIFICATION_ID = 101;
    public static final int BACKGROUND_RESTRICTION_NOTIFICATION_ID = 102;
    public static final int COLLISION_DETECTED_NOTIFICATION_ID = 103;
    public static final int WIFI_SCANNING_NOTIFICATION_ID = 104;
    public static final int GOOGLE_PLAY_SETTINGS_NOTIFICATION_ID = 105;
    public static final int ACTIVITY_PERMISSION_DENIED_NOTIFICATION_ID = 106;
    public static final int OVERLAY_PERMISSION_DENIED_NOTIFICATION_ID = 107;
    public static final int BATTERY_OPTIMIZATION_NOTIFICATION_ID = 108;
    public static final int ONE_PLUS_DEEP_OPTIMIZATION_NOTIFICATION_ID = 109;
    public static final int AIRPLANE_MODE_ENABLED_NOTIFICATION_ID = 110;

    public static final int MULTIPLE_PERMISSION_DENIED_NOTIFICATION_ID = 199;

    private static final int psmEnabledRequestCode = 200;
    private static final int backgroundRestrictedRequestCode = 202;
    private static final int googlePlaySettingsRequestCode = 204;
    private static final int locationPermissionRequestCode = 205;
    private static final int collisionActivityRequestCode = 206;
    private static final int activityPermissionRequestCode = 207;
    private static final int overlayPermissionRequestCode = 208;
    private static final int batteryOptimizationRequestCode = 209;
    private static final int onePlusDeepOptimizationRequestCode = 210;
    private static final int airplaneModeEnabledRequestCode = 211;

    private static final int multiplePermissionRequestCode = 299;

    // channel keys (id) are used to sort the channels in the notification
    // settings page. Meaningful ids and descriptions tell the user
    // about which notifications are safe to toggle on/off for the application.
    private static final String FOREGROUND_CHANNEL_KEY = "Foreground";
    private static final String SETTINGS_CHANNEL_KEY = "Settings";
    private static final String COLLISION_CHANNEL_KEY = "Collision";
    private static NotificationManagerCompat notificationManager;

    /**
     * Create a notification when location permission is denied to the application.
     *
     * @param context App context
     * @return the created notification.
     */
    public static Notification createLocationPermissionDeniedNotification(Context context) {
        createNotificationChannels(context);
        Intent actionIntent = new Intent(context, MainActivity.class);
        actionIntent.setAction(Constants.EVENT_LOCATION_PERMISSION_ERROR);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, locationPermissionRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Location Permission Denied")
                .setTicker("Location Permission Denied")
                .setContentText("Grant location permission to Zendrive app.")
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * Create a notification when there was a settings error reported by Google Play Services.
     * @param context App context
     * @param result The {@link LocationSettingsResult} object from Play Services
     * @return created notification
     */
    public static Notification createGooglePlaySettingsNotification(Context context,
                                                                    LocationSettingsResult result) {
        if (result.getStatus().isSuccess()) {
            return null;
        }
        createNotificationChannels(context);
        Intent actionIntent = new Intent(context, MainActivity.class);
        actionIntent.setAction(Constants.EVENT_GOOGLE_PLAY_SETTING_ERROR);
        actionIntent.putExtra(Constants.EVENT_GOOGLE_PLAY_SETTING_ERROR, result);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, googlePlaySettingsRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Location Settings Error")
                .setTicker("Location Settings Error")
                .setContentText("Tap here to resolve.")
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * Create a notification when Power Saver Mode is enabled on the device.
     * @param context App context
     * @return created notification
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static Notification createPSMEnabledNotification(Context context, boolean isError) {
        createNotificationChannels(context);
        Intent actionIntent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, psmEnabledRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        String errorWarningPrefix = isError ? "Error: " : "Warning: ";

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle(errorWarningPrefix + "Power Saver Mode Enabled")
                .setTicker(errorWarningPrefix + "power Saver Mode Enabled")
                .setContentText("Disable power saver mode.")
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }

    /**
     * Create a notification when Battery Optimization is enabled on the device for the application.
     *
     * For more details, see:
     * <a href="https://developer.android.com/training/monitoring-device-state/doze-standby">
     * Optimize for Doze and App Standby</a>
     *
     * @param context App context
     * @return created notification
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public static Notification getBatteryOptimizationEnabledNotification(Context context) {
        createNotificationChannels(context);
        Intent actionIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + context.getPackageName()));
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pi = PendingIntent.getActivity(context, batteryOptimizationRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Battery Optimization Enabled")
                .setTicker("Battery Optimization Enabled")
                .setContentText("Tap to disable Battery Optimization")
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }

    /**
     * Create a notification when OnePlus Deep Optimization setting is enabled on the device.
     *
     * @param context App context
     * @param intent Resolvable intent. See {@link com.zendrive.sdk.ZendriveResolvableError#navigableIntent}
     * @return created notification
     */
    public static Notification getOnePlusDeepOptimizationEnabledNotification(Context context,
                                                                             Intent intent) {
        createNotificationChannels(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, onePlusDeepOptimizationRequestCode,
                intent, FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Deep Optimization Enabled")
                .setTicker("Deep Optimization Enabled")
                .setContentText("Tap to navigate to Deep Optimization setting screen")
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }

    /**
     * Create a notification when background restriction is enabled on the device.
     *
     * @param context App context
     * @return created notification
     */
    @RequiresApi(Build.VERSION_CODES.P)
    public static Notification createBackgroundRestrictedNotification(Context context) {
        createNotificationChannels(context);
        Intent actionIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, backgroundRestrictedRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        return new Notification.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Background Restricted")
                .setTicker("Background Restricted")
                .setContentText("Disable Background Restriction")
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }

    /**
     * Create a notification when physical activity permission is denied to the application.
     *
     * @param context App context
     * @return the created notification.
     */
    public static Notification createActivityPermissionDeniedNotification(Context context) {
        createNotificationChannels(context);
        Intent actionIntent = new Intent(context, MainActivity.class);
        actionIntent.setAction(Constants.EVENT_ACTIVITY_PERMISSION_ERROR);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, activityPermissionRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Activity Permission Denied")
                .setTicker("Activity Permission Denied")
                .setContentText("Grant activity permission to Zendrive app.")
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * Create a notification when overlay permission is denied to the application.
     *
     * @param context App context
     * @return the created notification.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public static Notification createOverlayPermissionDeniedNotification(Context context) {
        createNotificationChannels(context);
        Intent actionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, overlayPermissionRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Overlay Permission Denied")
                .setTicker("Overlay Permission Denied")
                .setContentText("Grant overlay permission to Zendrive app.")
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * Create a notification when airplane mode is enabled on the device.
     *
     * @param context App context
     * @return the created notification.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public static Notification createAirplaneModeNotification(Context context) {
        createNotificationChannels(context);
        Intent actionIntent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, airplaneModeEnabledRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Airplane Mode enabled")
                .setTicker("Airplane Mode Denied")
                .setContentText("Disable airplane mode.")
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * Create a notification when multiple permissions are denied to the application.
     *
     * @param context App context
     * @return the created notification.
     */
    public static Notification createMultiplePermissionsDeniedNotification(Context context,
                                                                           ArrayList<String> missingPermissionList) {
        createNotificationChannels(context);
        Intent actionIntent = new Intent(context, MainActivity.class);
        actionIntent.setAction(Constants.EVENT_MULTIPLE_PERMISSIONS_ERROR);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        actionIntent.putExtra(Constants.MULTIPLE_PERMISSIONS_DENIED_LIST, missingPermissionList);
        PendingIntent pi = PendingIntent.getActivity(context, multiplePermissionRequestCode,
                actionIntent, FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, SETTINGS_CHANNEL_KEY)
                .setContentTitle("Multiple Permissions Denied")
                .setTicker("Multiple Permissions Denied")
                .setContentText("Grant permissions to Zendrive app.")
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * Create a notification that is displayed when the Zendrive SDK
     * detects a possible drive.
     *
     * @param context App context
     * @return the created notification.
     */
    public static Notification createMaybeInDriveNotification(Context context) {
        createNotificationChannels(context);

        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Zendrive")
                .setDefaults(0)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentText("Detecting possible drive.")
                .setContentIntent(getNotificationClickIntent(context))
                .build();
    }

    /**
     * Create a notification that is displayed when the Zendrive SDK
     * determines that the user is driving.
     *
     * @param context App context
     * @return the created notification.
     */
    public static Notification createInDriveNotification(Context context) {
        createNotificationChannels(context);
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Zendrive")
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentText("Drive started.")
                .setContentIntent(getNotificationClickIntent(context))
                .build();
    }

    public static void cancelErrorAndWarningNotifications(Context context) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(LOCATION_DISABLED_NOTIFICATION_ID);
        manager.cancel(LOCATION_PERMISSION_DENIED_NOTIFICATION_ID);
        manager.cancel(PSM_ENABLED_NOTIFICATION_ID);
        manager.cancel(BACKGROUND_RESTRICTION_NOTIFICATION_ID);
        manager.cancel(WIFI_SCANNING_NOTIFICATION_ID);
        manager.cancel(GOOGLE_PLAY_SETTINGS_NOTIFICATION_ID);
        manager.cancel(ACTIVITY_PERMISSION_DENIED_NOTIFICATION_ID);
        manager.cancel(OVERLAY_PERMISSION_DENIED_NOTIFICATION_ID);
        manager.cancel(MULTIPLE_PERMISSION_DENIED_NOTIFICATION_ID);
        manager.cancel(BATTERY_OPTIMIZATION_NOTIFICATION_ID);
        manager.cancel(ONE_PLUS_DEEP_OPTIMIZATION_NOTIFICATION_ID);
    }

    /**
     * Create and show a notification when Zendrive SDK detects a collision.
     * @param context App context
     * @param accidentInfo AccidentInfo associated with the collision
     */
    public static void showCollisionNotification(Context context, AccidentInfo accidentInfo) {
        createNotificationChannels(context);
        Notification notification = new NotificationCompat.Builder(context, COLLISION_CHANNEL_KEY)
                .setContentTitle("Zendrive")
                .setContentText("Collision Detected.")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
                .setFullScreenIntent(getActivityPendingIntentForCollision(context, accidentInfo),
                        true)
                .setAutoCancel(true)
                .build();

        getNotificationManager(context).notify(COLLISION_DETECTED_NOTIFICATION_ID, notification);
    }

    public static void removeCollisionNotification(Context context) {
       getNotificationManager(context).cancel(COLLISION_DETECTED_NOTIFICATION_ID);
    }

    private static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel lowPriorityNotificationChannel = new NotificationChannel(FOREGROUND_CHANNEL_KEY,
                    "Zendrive trip tracking",
                    NotificationManager.IMPORTANCE_MIN);
            lowPriorityNotificationChannel.setShowBadge(false);
            manager.createNotificationChannel(lowPriorityNotificationChannel);

            NotificationChannel defaultNotificationChannel = new NotificationChannel
                    (SETTINGS_CHANNEL_KEY, "Problems",
                            NotificationManager.IMPORTANCE_HIGH);
            defaultNotificationChannel.setShowBadge(true);
            manager.createNotificationChannel(defaultNotificationChannel);

            NotificationChannel collisionDetectedNotificationChannel
                    = new NotificationChannel(COLLISION_CHANNEL_KEY, "Collision Detected",
                    NotificationManager.IMPORTANCE_HIGH);
            collisionDetectedNotificationChannel.setShowBadge(false);
            manager.createNotificationChannel(collisionDetectedNotificationChannel);
        }
    }

    private static PendingIntent getNotificationClickIntent(Context context) {
        Intent notificationIntent = new Intent(context.getApplicationContext(), SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context.getApplicationContext(), 0,
                notificationIntent, 0);
    }

    private static PendingIntent getActivityPendingIntentForCollision(Context context,
                                                                      AccidentInfo accidentInfo) {
        Intent intent = new Intent(context, CollisionDetectedActivity.class);
        intent.putExtra(Constants.ACCIDENT_INFO, accidentInfo);
        return PendingIntent.getActivity(context, collisionActivityRequestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static NotificationManagerCompat getNotificationManager(Context context) {
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(context);
        }
        return notificationManager;
    }
}
