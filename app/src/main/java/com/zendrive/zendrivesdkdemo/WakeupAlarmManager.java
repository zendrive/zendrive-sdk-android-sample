package com.zendrive.zendrivesdkdemo;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.ZendriveSetupCallback;

/**
 * Manager for a periodic alarm that the application creates to restart Zendrive SDK when it
 * is unable to run in background because the OS killed it.
 *
 */
public class WakeupAlarmManager {
    /**
     * @param ctx The application context.
     */
    public WakeupAlarmManager(Context ctx) {
        this.context = ctx;
    }

    public void setAlarm() {
        if (kAppWakeupIntervalMillisecs > 0) {
            AlarmManager alarmManager = getAlarmManager();
            if (alarmManager == null) {
                return;
            }
            PendingIntent alarmIntent = getAlarmIntent();
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + kAppWakeupIntervalMillisecs,
                    alarmIntent);
        }
    }

    public void unsetAlarm() {
        AlarmManager alarmManager = getAlarmManager();
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(getAlarmIntent());
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getAlarmIntent() {
        Intent intent = new Intent(context, AppWakeupReciever.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    /**
     * The interval at which to wake up the SDK. If <= 0, then an alarm won't be setup.
     */
    static private long kAppWakeupIntervalMillisecs = 6 * AlarmManager.INTERVAL_HOUR;

    private Context context;
}
