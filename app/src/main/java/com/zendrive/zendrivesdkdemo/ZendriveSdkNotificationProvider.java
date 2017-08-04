package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.zendrive.sdk.ZendriveNotificationContainer;
import com.zendrive.sdk.ZendriveNotificationProvider;

/**
 * Used to provide the Zendrive SDK with notifications that are triggered
 * when the SDK goes into foreground mode.
 */
public class ZendriveSdkNotificationProvider implements ZendriveNotificationProvider {

    @NonNull
    @Override
    @RequiresApi(Build.VERSION_CODES.O)
    public ZendriveNotificationContainer getMaybeInDriveNotificationContainer(@NonNull Context context) {
        return new ZendriveNotificationContainer(
                NotificationUtility.FOREGROUND_MODE_NOTIFICATION_ID,
                NotificationUtility.createMaybeInDriveNotification(context));
    }

    @NonNull
    @Override
    public ZendriveNotificationContainer getInDriveNotificationContainer(@NonNull Context context) {
        return new ZendriveNotificationContainer(
                NotificationUtility.FOREGROUND_MODE_NOTIFICATION_ID,
                NotificationUtility.createInDriveNotification(context));
    }
}
