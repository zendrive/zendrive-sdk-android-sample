package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yogesh on 16/01/15.
 */
public class PreferenceManager {

    public static void saveApplicationKeyToPrefs(Context context, String applicationKey) {
        SharedPreferences.Editor editor = android.preference.PreferenceManager.
                getDefaultSharedPreferences(context).edit();
        editor.putString(kPreferenceKeyForApplicationKey, applicationKey);
        editor.apply();
    }

    public static String getApplicationKeyFromPrefs(Context context) {
        SharedPreferences preferences = android.preference.PreferenceManager.
                getDefaultSharedPreferences(context);
        String applicationKey = preferences.getString(kPreferenceKeyForApplicationKey, "your_application_key");
        return applicationKey;
    }

    private static final String kPreferenceKeyForApplicationKey = "zendrive_application_key";
}
