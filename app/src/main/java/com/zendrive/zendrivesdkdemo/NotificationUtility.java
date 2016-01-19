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
 *
 * Created by yogesh on 10/26/15.
 */
public class NotificationUtility {
    // Notification related constants
    public static final int kLocationDisabledNotificationId = 99;
    public static final int kLocationPermissionDeniedNotificationId = 100;

    // Location disabled notification
    public static Notification getLocationDisabledNotification(Context context) {
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setContentTitle("Location Disabled")
                .setTicker("Location Disabled")
                .setContentText("Enable High Accuracy location.")
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }

}
