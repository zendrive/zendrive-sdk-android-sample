package com.zendrive.zendrivesdkdemo;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.android.annotations.NonNull;

/**
 * Manager for a periodic alarm that the application creates to restart Zendrive SDK when it
 * is unable to run in background because the OS killed it.
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
            PendingIntent alarmIntent = getAlarmIntent();
            getAlarmManager().set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                  SystemClock.elapsedRealtime() + kAppWakeupIntervalMillisecs,
                                  alarmIntent);
        }
    }

    public void unsetAlarm() {
        getAlarmManager().cancel(getAlarmIntent());
    }

    @NonNull
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
    private static final long kAppWakeupIntervalMillisecs = 6 * AlarmManager.INTERVAL_HOUR;

    private Context context;
}
