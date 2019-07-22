package com.zendrive.zendrivesdkdemo;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;

public class SettingsActivity extends PreferenceActivity {
    Preference zendriveSdkVersionPreference;
    SwitchPreference driveTrackingPreference;
    ListPreference userTypePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Resources resources = getResources();
        String zendriveSdkVersionKey = resources.getString(R.string.zendrive_sdk_version_key);
        String driveTrackingKey = resources.getString(R.string.drive_tracking_key);
        String userTypeKey = resources.getString(R.string.user_type_key);

        zendriveSdkVersionPreference = preferenceScreen.findPreference(zendriveSdkVersionKey);
        driveTrackingPreference =
                (SwitchPreference) preferenceScreen.findPreference(driveTrackingKey);
        userTypePreference = (ListPreference) preferenceScreen.findPreference(userTypeKey);
        if (savedInstanceState == null) {
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        zendriveSdkVersionPreference.setSummary(Zendrive.getBuildVersion());
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
                driveTrackingPreference.isChecked() ? ZendriveDriveDetectionMode.AUTO_ON :
                        ZendriveDriveDetectionMode.AUTO_OFF;
        SharedPreferenceManager.setZendriveAutoDetectionMode(this, mode);
        SharedPreferenceManager.setPreference(this,
                                              SharedPreferenceManager.USER_TYPE,
                                              userTypePreference.getValue());
        // restart sdk with new settings.
        Zendrive.teardown(this, new ZendriveOperationCallback() {
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
                    MainActivity.initializeZendriveSDK(SettingsActivity.this, callback, true);
                }
                finish();
            }
        });
    }
}
