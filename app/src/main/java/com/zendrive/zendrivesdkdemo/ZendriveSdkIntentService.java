package com.zendrive.zendrivesdkdemo;

import android.util.Log;

import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.DriveResumeInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.ZendriveIntentService;
import com.zendrive.sdk.ZendriveLocationSettingsResult;

/**
 * Intent service which receives Zendrive SDK callbacks and passes it to the ZendriveManager.
 */
public class ZendriveSdkIntentService extends ZendriveIntentService {
    public ZendriveSdkIntentService() {
        super("ZDService");
    }

    @Override
    public void onCreate() {
        Log.d(Constants.LOG_TAG_DEBUG, " ZendriveSdkNotificationService Created");
        super.onCreate();
    }

    @Override
    public void onDriveStart(DriveStartInfo startInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive Start");
        ZendriveManager.getSharedInstance(getApplicationContext()).onDriveStart(startInfo);
    }

    @Override
    public void onDriveEnd(DriveInfo driveInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive End");
        ZendriveManager.getSharedInstance(getApplicationContext()).onDriveEnd(driveInfo);
    }

    @Override
    public void onDriveResume(DriveResumeInfo driveResumeInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive End");
        ZendriveManager.getSharedInstance(getApplicationContext()).onDriveResume(driveResumeInfo);
    }

    @Override
    public void onAccident(AccidentInfo accidentInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Accident Detected");
        ZendriveManager.getSharedInstance(getApplicationContext()).onAccident(accidentInfo);
    }

    @Override
    public void onLocationPermissionsChange(boolean granted) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Location Permission : " +
                granted);
        ZendriveManager.getSharedInstance(getApplicationContext()).onLocationPermissionsChange(granted);
    }

    @Override
    public void onLocationSettingsChange(ZendriveLocationSettingsResult settingsResult) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Location Setting : " + settingsResult.isSuccess());
        ZendriveManager.getSharedInstance(getApplicationContext()).onLocationSettingsChange(settingsResult);

    }
}
