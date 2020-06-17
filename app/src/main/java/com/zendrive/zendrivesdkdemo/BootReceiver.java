package com.zendrive.zendrivesdkdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.LOG_TAG_DEBUG, "Receiver woken up by " + intent.getAction());
        MainActivity.initializeZendriveSDK(context, setupResult -> {
            if (setupResult.isSuccess()) {
                Log.d(Constants.LOG_TAG_DEBUG, "Setup Success");
            } else {
                Log.d(Constants.LOG_TAG_DEBUG, "Setup Failed: " + setupResult.getErrorMessage());
            }
        }, false);
    }
}
