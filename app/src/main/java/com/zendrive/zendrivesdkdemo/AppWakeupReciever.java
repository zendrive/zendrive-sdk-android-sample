package com.zendrive.zendrivesdkdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;

/**
 * This wakes up application.
 */
public class AppWakeupReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ZendriveManager zendriveManager = ZendriveManager.getSharedInstance(context.getApplicationContext());
        if (!zendriveManager.isSdkInitialized()) {
            ZendriveConfiguration configuration = zendriveManager.getSavedConfiguration();
            if (null == configuration) {
                return;
            }
            zendriveManager.initializeZendriveSDK(configuration, new ZendriveOperationCallback() {
                @Override
                public void onCompletion(ZendriveOperationResult zendriveOperationResult) {
                    Log.d(Constants.LOG_TAG_DEBUG, "(AppWakeup) Zendrive setup complete: " + zendriveOperationResult.isSuccess());
                }
            });
        }
    }
}
