package com.zendrive.zendrivesdkdemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;


/**
 * This class creates zendrive notifications.
 */

public class NotificationUtility {
    // Notification related constants
    public static final int kForegroundModeNotificationId = 98;
    public static final int kLocationDisabledNotificationId = 99;
    public static final int kLocationPermissionDeniedNotificationId = 100;

    public static Notification getZendriveServiceNotification(Context context) {
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

    private static PendingIntent getNotificationClickIntent(Context context) {
        Intent notificationIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                notificationIntent, 0);
        return pendingIntent;
    }

    // Location permission denied notification
    public static Notification getLocationPermissionDeniedNotification(Context context) {
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setContentTitle(context.getResources().getString(R.string.location_permission_denied))
                .setTicker(context.getResources().getString(R.string.location_permission_denied))
                .setContentText(context.getResources().getString(R.string.grant_location_permission))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }

    // Location disabled notification
    public static Notification getLocationDisabledNotification(Context context) {
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setContentTitle(context.getResources().getString(R.string.location_disabled))
                .setTicker(context.getResources().getString(R.string.location_disabled))
                .setContentText(context.getResources().getString(R.string.enable_location))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }

}
