package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zendrive.sdk.ZendriveDriveDetectionMode;

public class SharedPreferenceManager {

    public static void setPreference(Context context, String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void removePreference(Context context, String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public static String getStringPreference(Context context, String key, String defaultValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static ZendriveDriveDetectionMode getZendriveAutoDetectionMode(Context context){
        String modeString = getZendriveAutoDetectionModeString(context);
        String autoOffString = context.getResources().getString(R.string.auto_off);

        if(modeString.equals(autoOffString)) {
            return ZendriveDriveDetectionMode.AUTO_OFF;
        }
        return ZendriveDriveDetectionMode.AUTO_ON;
    }

    public static String getZendriveAutoDetectionModeString(Context context) {
        return getStringPreference(context, kKeyDetectionModePreference,
                context.getResources().getString(R.string.auto_on));
    }

    public static void setZendriveAutoDetectionModeString(Context context, String modeString) {
        setPreference(context, kKeyDetectionModePreference, modeString);
    }

    public static void setZendriveAutoDetectionMode(
            Context context, ZendriveDriveDetectionMode driveDetectionMode){
        String autoOnString = context.getResources().getString(R.string.auto_on);
        String autoOffString = context.getResources().getString(R.string.auto_off);
        String modeString = driveDetectionMode == ZendriveDriveDetectionMode.AUTO_OFF ?
                autoOffString: autoOnString;
        setZendriveAutoDetectionModeString(context, modeString);
    }

    public static void removeZendriveAutoDetectionMode(Context context){
        removePreference(context, kKeyDetectionModePreference);
    }

    public static void setDriverId(Context context, String driverId){
        setPreference(context, Constants.DRIVER_ID_KEY, driverId);
    }

    private static String kKeyDetectionModePreference = "kKeyDetctionModePreference";
}
