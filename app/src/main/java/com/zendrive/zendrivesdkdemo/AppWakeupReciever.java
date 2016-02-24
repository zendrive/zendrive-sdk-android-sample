package com.zendrive.zendrivesdkdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.ZendriveSetupCallback;

/**
 * This wakes up application.
 */
public class AppWakeupReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ZendriveManager zendriveManager = ZendriveManager.getSharedInstance(context.getApplicationContext());
        if (!zendriveManager.isSDKSetup()) {
            ZendriveConfiguration configuration = zendriveManager.getSavedConfiguration();
            if(null == configuration){
                return;
            }
            // silent setup in the background.
            zendriveManager.initializeZendriveSDK(configuration, new ZendriveSetupCallback() {
                @Override
                public void onSetup(ZendriveOperationResult zendriveOperationResult) {

                }
            });
        }
    }
}
