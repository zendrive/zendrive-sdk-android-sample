package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.util.Log;

import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.AnalyzedDriveInfo;
import com.zendrive.sdk.DriveResumeInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.EstimatedDriveInfo;
import com.zendrive.sdk.ZendriveBroadcastReceiver;
import com.zendrive.sdk.ZendriveLocationSettingsResult;

/**
 * Broadcast receiver which receives Zendrive SDK callbacks and passes it to the ZendriveManager.
 */
public class ZendriveSdkBroadcastReceiver extends ZendriveBroadcastReceiver {

    @Override
    public void onDriveStart(Context context, DriveStartInfo startInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive Start");
        ZendriveManager.getSharedInstance(context).onDriveStart(startInfo);
    }

    @Override
    public void onDriveResume(Context context, DriveResumeInfo driveResumeInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive Resumed");
        ZendriveManager.getSharedInstance(context).onDriveResume(driveResumeInfo);
    }

    @Override
    public void onDriveEnd(Context context, EstimatedDriveInfo estimatedDriveInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive End");
        ZendriveManager.getSharedInstance(context).onDriveEnd(estimatedDriveInfo);
    }

    @Override
    public void onDriveAnalyzed(Context context, AnalyzedDriveInfo analyzedDriveInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Drive Analyzed");
        ZendriveManager.getSharedInstance(context).onDriveAnalyzed(analyzedDriveInfo);
    }

    @Override
    public void onAccident(Context context, AccidentInfo accidentInfo) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Accident Detected");
        ZendriveManager.getSharedInstance(context).onAccident(accidentInfo);
    }

    @Override
    public void onLocationPermissionsChange(Context context, boolean granted) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Location Permission : " +
                granted);
        ZendriveManager.getSharedInstance(context).onLocationPermissionsChange(granted);
    }

    @Override
    public void onLocationSettingsChange(Context context, ZendriveLocationSettingsResult settingsResult) {
        Log.d(Constants.LOG_TAG_DEBUG, "CallBack From SDK: Location Setting : " + settingsResult.isSuccess());
        ZendriveManager.getSharedInstance(context).onLocationSettingsChange(settingsResult);
    }
}
