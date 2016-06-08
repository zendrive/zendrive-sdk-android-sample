package com.zendrive.zendrivesdkdemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;


/**
 * Utility to create notifications to show to the user when the Zendrive SDK has something
 * interesting to report.
 */
public class NotificationUtility {
    // Notification related constants
    public static final int FOREGROUND_MODE_NOTIFICATION_ID = 98;
    public static final int LOCATION_DISABLED_NOTIFICATION_ID = 99;
    public static final int LOCATION_PERMISSION_DENIED_NOTIFICATION_ID = 100;

    /**
     * Create a notification that is used when we run Zendrive as a foreground service.
     * @param context application context.
     * @return the created notification
     */
    public static Notification createZendriveForegroundServiceNotification(Context context) {
        PendingIntent notificationIntent = getNotificationClickIntent(context);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(context.getResources().getString(R.string.drive_progress_title))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(notificationIntent)
                .build();
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        return notification;
    }

    /**
     * Create a notification when location permission is denied to the application.
     * @param context App context
     * @return the created notifcation.
     */
    public static Notification createLocationPermissionDeniedNotification(Context context) {
        // TODO: The click intent should not point to location settings. Perhaps we can load
        // the app permissions tab.
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);

        return new NotificationCompat.Builder(context.getApplicationContext())
                .setContentTitle(context.getResources().getString(R.string.location_permission_denied))
                .setTicker(context.getResources().getString(R.string.location_permission_denied))
                .setContentText(context.getResources().getString(R.string.grant_location_permission))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .build();
    }

    /**
     * Create a notification when high accuracy location is disabled on the device.
     * @param context App context
     * @return the created notifcation.
     */
    public static Notification createLocationSettingDisabledNotification(Context context) {
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);

        return new NotificationCompat.Builder(context.getApplicationContext())
                .setContentTitle(context.getResources().getString(R.string.location_disabled))
                .setTicker(context.getResources().getString(R.string.location_disabled))
                .setContentText(context.getResources().getString(R.string.enable_location))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .build();
    }

    private static PendingIntent getNotificationClickIntent(Context context) {
        Intent notificationIntent = new Intent(context.getApplicationContext(), SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context.getApplicationContext(), 0,
                                         notificationIntent, 0);
    }

}
