package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;

/**
 * Created by deepanshu on 6/8/16.
 */
public class SettingsActivity extends PreferenceActivity {
    SwitchPreference driveTrackingPreference;
    ListPreference userTypePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Resources resources = getResources();
        String driveTrackingKey = resources.getString(R.string.drive_tracking_key);
        String userTypeKey = resources.getString(R.string.user_type_key);

        driveTrackingPreference =
                (SwitchPreference) preferenceScreen.findPreference(driveTrackingKey);
        userTypePreference = (ListPreference) preferenceScreen.findPreference(userTypeKey);
        if (savedInstanceState == null) {
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        userTypePreference.setValue(
                SharedPreferenceManager.getStringPreference(
                        this,
                        SharedPreferenceManager.USER_TYPE,
                        ZendriveManager.UserType.FREE.toString()));
        driveTrackingPreference.setChecked(
                SharedPreferenceManager.getZendriveAutoDetectionMode(this) ==
                        ZendriveDriveDetectionMode.AUTO_ON);
    }

    void save() {
        ZendriveDriveDetectionMode mode =
                driveTrackingPreference.isChecked() ? ZendriveDriveDetectionMode.AUTO_OFF :
                        ZendriveDriveDetectionMode.AUTO_ON;
        SharedPreferenceManager.setZendriveAutoDetectionMode(this, mode);
        SharedPreferenceManager.setPreference(this,
                                              SharedPreferenceManager.USER_TYPE,
                                              userTypePreference.getValue());
        // restart sdk with new settings.
        Zendrive.teardown(new ZendriveOperationCallback() {
            @Override
            public void onCompletion(ZendriveOperationResult zendriveOperationResult) {
                if (zendriveOperationResult.isSuccess()) {
                    ZendriveOperationCallback callback = new ZendriveOperationCallback() {
                        @Override
                        public void onCompletion(ZendriveOperationResult zendriveOperationResult) {
                            Log.d(Constants.LOG_TAG_DEBUG,
                                  "(Settings) Zendrive setup complete: " +
                                          zendriveOperationResult.isSuccess());
                            Intent refresh_ui = new Intent(Constants.REFRESH_UI);
                            if (!zendriveOperationResult.isSuccess()) {
                                refresh_ui.putExtra(Constants.ERROR,
                                                    zendriveOperationResult.getErrorMessage());
                            }
                            LocalBroadcastManager.getInstance(SettingsActivity.this)
                                    .sendBroadcast(refresh_ui);
                        }
                    };
                    MainActivity.initializeZendriveSDK(SettingsActivity.this, callback);
                }
                finish();
            }
        });
    }
}
