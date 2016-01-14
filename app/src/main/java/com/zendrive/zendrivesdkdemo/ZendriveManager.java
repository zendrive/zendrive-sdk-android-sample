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
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.ZendriveSetupCallback;

/**
 * This class is an interface between application and zendrive sdk,
 * It collects data from sdk and passes it to application.
 *
 * Created by yogesh on 10/16/15.
 */
public class ZendriveManager {
    private boolean isLocationSettingsEnabled = true;
    private Context context;

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

    public void initializeZendriveSDK(final Context applicationContext, ZendriveConfiguration configuration,
                                      final ZendriveSetupCallback setupCallback) {
        if (Zendrive.isSDKSetup()) {
            ZendriveOperationResult result = ZendriveOperationResult.createSuccess();
            if (setupCallback != null) {
                setupCallback.onSetup(result);
            }
            return;
        }

        // setup zendrive sdk
        Zendrive.setup(applicationContext, configuration, ZendriveSdkNotificationService.class,
                setupCallback);
    }


    public boolean isSDKSetup() {
        return Zendrive.isSDKSetup();
    }

    public void onDriveStart(DriveStartInfo driveStartInfo) {
        Intent intent = new Intent(Constants.DRIVE_START);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    public void onDriveEnd(DriveInfo driveInfo) {
        Intent intent = new Intent(Constants.DRIVE_END);
        intent.putExtra(Constants.DRIVE_DISTANCE, driveInfo.distanceMeters);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    public void onAccident(AccidentInfo accidentInfo) {
        Intent intent = new Intent(Constants.ACCIDENT);
        intent.putExtra(Constants.ACCIDENT_CONFIDENCE, accidentInfo.confidence);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    public void onLocationPermissionsChange(boolean granted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Constants.EVENT_LOCATION_PERMISSION_CHANGE);
            intent.putExtra(Constants.EVENT_LOCATION_PERMISSION_CHANGE, granted);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);

        } else {
            throw new RuntimeException("Callback on non marshmallow sdk");
        }

    }

    public void onLocationSettingsChange(boolean locationEnabled) {
        this.isLocationSettingsEnabled = locationEnabled;
        displayOrHideLocationSettingNotification();
        Intent intent = new Intent(Constants.EVENT_LOCATION_SETTING_CHANGE);
        intent.putExtra(Constants.EVENT_LOCATION_SETTING_CHANGE, locationEnabled);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
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
}
