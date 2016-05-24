package com.zendrive.zendrivesdkdemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveDriveDetectionMode;

/**
 * UI for settings.
 */
public class SettingFragment extends BaseFragment {

    private RadioGroup trackingSettingRadioButton;
    private RadioGroup userTypeRadioButton;
    private Button applySettingsButton;

    private ZendriveDriveDetectionMode trackingMode = ZendriveDriveDetectionMode.AUTO_ON;
    private String userType = ZendriveManager.UserType.FREE.name();

    public SettingFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        trackingSettingRadioButton = (RadioGroup) view.findViewById(R.id.trackingStatus);
        userTypeRadioButton = (RadioGroup) view.findViewById(R.id.userType);
        applySettingsButton = (Button) view.findViewById(R.id.settingButton);

        trackingSettingRadioButton.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.trackingOn) {
                    trackingMode = ZendriveDriveDetectionMode.AUTO_ON;
                } else if (i == R.id.trackingOff) {
                    trackingMode = ZendriveDriveDetectionMode.AUTO_OFF;
                }
            }
        });

        userTypeRadioButton.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.freeUser) {
                    userType = ZendriveManager.UserType.FREE.name();
                } else if (i == R.id.paidUser) {
                    userType = ZendriveManager.UserType.PAID.name();
                }
            }
        });

        applySettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferenceManager.setZendriveAutoDetectionMode(getContext(), trackingMode);
                SharedPreferenceManager.setPreference(getContext(), SharedPreferenceManager.USER_TYPE, userType);
                final MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    // call teardown.
                    Zendrive.teardown(null);
                    // do fresh sdk start with new settings.
                    activity.loadMainScreen();
                }

            }
        });
        return view;
    }
}
