package com.zendrive.zendrivesdkdemo;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Manager for a periodic alarm that the application creates to restart Zendrive SDK when it
 * is unable to run in background because the OS killed it.
 *
 * The Zendrive SDK uses its own internal alarms to keep awake tracking driving in the background.
 * However, in the case of a SDK upgrade, the Zendrive SDK will not always be able to keep awake
 * in the background. In this case, the application should explicitly reinitialize the SDK to
 * resume drive tracking.
 */
class WakeupAlarmManager {
     /**
      */
     public static WakeupAlarmManager getInstance() {
         if (INSTANCE == null) {
             INSTANCE = new WakeupAlarmManager();
         }
         return INSTANCE;
     }

    private WakeupAlarmManager() {
    }

    /**
     * Must *not* be called from a synchronized block
     */
    void setAlarm(Context context) {
        AlarmManager alarmManager = getAlarmManager(context);
        long wakeupInterval = APP_WAKEUP_INTERVAL_MILLISECS;
        synchronized (mutex) {
            PendingIntent alarmIntent = getAlarmIntent(context);
            // Cancel pending alarms, if any
            alarmManager.cancel(alarmIntent);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + wakeupInterval,
                    alarmIntent);
        }
    }

    void unsetAlarm(Context context) {
        AlarmManager alarmManager = getAlarmManager(context);
        synchronized (mutex) {
            alarmManager.cancel(getAlarmIntent(context));
        }
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, AppWakeupReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }


    private static WakeupAlarmManager INSTANCE;
    private final Object mutex = new Object();


    /**
     * The interval at which to wake up the SDK. If <= 0, then an alarm won't be setup.
     *
     * Choose this based on the usage and the context of the app. Keeping this too low will
     * cause your app to wakeup a lot and consume battery. Keeping this too high may cause your
     * app to miss drive tracking when the Zendrive SDK upgrade happens.
     *
     * Consider keeping this value dynamic rather than as a constant.
     * For example, keep the interval small - like 5 minutes when the user is actively driving or
     * using the app. Increase the interval to 1 hour or more when the user is inactive say when
     * he is off duty.
     */
    private static final long APP_WAKEUP_INTERVAL_MILLISECS = AlarmManager.INTERVAL_HOUR;
}
