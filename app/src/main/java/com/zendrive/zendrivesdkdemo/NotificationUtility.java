package com.zendrive.zendrivesdkdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.zendrive.sdk.ZendriveLocationSettingsResult;


/**
 * Utility to create notifications to show to the user when the Zendrive SDK has something
 * interesting to report.
 */
public class NotificationUtility {
    // Notification related constants
    public static final int FOREGROUND_MODE_NOTIFICATION_ID = 98;
    public static final int LOCATION_DISABLED_NOTIFICATION_ID = 99;
    public static final int LOCATION_PERMISSION_DENIED_NOTIFICATION_ID = 100;

    // channel keys (id) are used to sort the channels in the notification
    // settings page. Meaningful ids and descriptions tell the user
    // about which notifications are safe to toggle on/off for the application.
    private static final String FOREGROUND_CHANNEL_KEY = "Foreground";
    private static final String LOCATION_CHANNEL_KEY = "Location";

    /**
     * Create a notification when location permission is denied to the application.
     * @param context App context
     * @return the created notifcation.
     */
    public static Notification createLocationPermissionDeniedNotification(Context context) {
        createNotificationChannels(context);
        // TODO: The click intent should not point to location settings. Perhaps we can load
        // the app permissions tab.
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);

        return new NotificationCompat.Builder(context.getApplicationContext(), LOCATION_CHANNEL_KEY)
                .setContentTitle(context.getResources().getString(R.string.location_permission_denied))
                .setTicker(context.getResources().getString(R.string.location_permission_denied))
                .setContentText(context.getResources().getString(R.string.grant_location_permission))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setCategory(Notification.CATEGORY_ERROR)
                .setContentIntent(pendingIntent)
                .build();
    }

    /**
     * Create a notification when high accuracy location is disabled on the device.
     * @param context App context
     * @param settingsResult to get potential resolution from play services
     * @return the created notifcation.
     */
    public static Notification createLocationSettingDisabledNotification(Context context,
                                                                         ZendriveLocationSettingsResult settingsResult) {
        createNotificationChannels(context);
        if (BuildConfig.DEBUG && settingsResult.isSuccess()) {
            throw new AssertionError("Only expected failed settings result");
        }
        // TODO: use the result from the callback and show appropriate message and intent
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);

        return new NotificationCompat.Builder(context.getApplicationContext(), LOCATION_CHANNEL_KEY)
                .setContentTitle(context.getResources().getString(R.string.location_disabled))
                .setTicker(context.getResources().getString(R.string.location_disabled))
                .setContentText(context.getResources().getString(R.string.enable_location))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_ERROR)
                .build();
    }

    /**
     * Create a notification that is displayed when the Zendrive SDK
     * detects a possible drive.
     *
     * @param context App context
     * @return the created notifcation.
     */
    public static Notification createMaybeInDriveNotification(Context context) {
        createNotificationChannels(context);

        // suppresses deprecated warning for setPriority(PRIORITY_MIN)
        //noinspection deprecation
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Zendrive")
                .setDefaults(0)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentText("Detecting possible drive.").build();
    }

    /**
     * Create a notification that is displayed when the Zendrive SDK
     * determines that the user is driving.
     *
     * @param context App context
     * @return the created notifcation.
     */
    public static Notification createInDriveNotification(Context context) {
        createNotificationChannels(context);

        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Zendrive")
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentText("Drive started.").build();
    }

    private static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            NotificationChannel lowPriorityNotificationChannel = new NotificationChannel(FOREGROUND_CHANNEL_KEY,
                    "Zendrive trip tracking",
                    NotificationManager.IMPORTANCE_MIN);
            lowPriorityNotificationChannel.setShowBadge(false);
            manager.createNotificationChannel(lowPriorityNotificationChannel);

            NotificationChannel defaultNotificationChannel = new NotificationChannel
                    (LOCATION_CHANNEL_KEY, "Problems",
                            NotificationManager.IMPORTANCE_DEFAULT);
            defaultNotificationChannel.setShowBadge(true);
            manager.createNotificationChannel(defaultNotificationChannel);
        }
    }

    private static PendingIntent getNotificationClickIntent(Context context) {
        Intent notificationIntent = new Intent(context.getApplicationContext(), SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context.getApplicationContext(), 0,
                                         notificationIntent, 0);
    }

}
