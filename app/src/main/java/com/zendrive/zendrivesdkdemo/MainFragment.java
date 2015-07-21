package com.zendrive.zendrivesdkdemo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriverAttributes;
import com.zendrive.sdk.ZendriveListener;

public class MainFragment extends Fragment {

    private TextView titleTextView, detailTextView;
    private Button startDriveButton, endDriveButton;
    private ViewGroup loadingIndicatorParent;
    private TextView sdkKeyTextView, driverIdTextView;
    private Button restartSDKButton;

    // TODO: Set your sdk key and driver id here.
    private static final String zendriveSDKKey = "";
    private static final String driverId = "";

    // TODO: Set these optional attributes of the driver here.
    private static final String driverFirstName = "";
    private static final String driverLastName = "";
    private static final String driverEmail = "";

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        detailTextView = (TextView) rootView.findViewById(R.id.detailTextView);
        sdkKeyTextView = (TextView) rootView.findViewById(R.id.sdkKeyTextView);
        driverIdTextView = (TextView) rootView.findViewById(R.id.driverIdTextView);

        sdkKeyTextView.setText(
                zendriveSDKKey.equals("") ?
                        getResources().getText(R.string.default_sdk_key) :
                        zendriveSDKKey);
        driverIdTextView.setText(
                driverId.equals("") ?
                        getResources().getText(R.string.default_driver_id) :
                        driverId);

        startDriveButton = (Button) rootView.findViewById(R.id.startDriveButton);
        startDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate a random tracking id
                String trackingId = "" + System.currentTimeMillis();
                Zendrive.startDrive(trackingId);
            }
        });

        endDriveButton = (Button) rootView.findViewById(R.id.endDriveButton);
        endDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Zendrive.stopDrive();
            }
        });

        loadingIndicatorParent = (ViewGroup)rootView.findViewById(R.id.loadingIndicatorParent);
        loadingIndicatorParent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });

        restartSDKButton = (Button) rootView.findViewById(R.id.restartSDKButton);
        restartSDKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Teardown Zendrive SDK if running
                Zendrive.teardown();

                // Initialize zendrive sdk
                MainFragment.this.initializeZendriveSDK();
            }
        });

        return rootView;
    }

    public void initializeZendriveSDK() {

        // Get user attributes (Optional)
        ZendriveDriverAttributes userAttributes = new ZendriveDriverAttributes();
        if (!driverFirstName.equals("")) {
            userAttributes.setFirstName(driverFirstName);
        }
        if (!driverLastName.equals("")) {
            userAttributes.setLastName(driverLastName);
        }
        if (!driverEmail.equals("")) {
            userAttributes.setEmail(driverEmail);
        }

        // Zendrive configuration
        ZendriveConfiguration configuration = new ZendriveConfiguration(
                zendriveSDKKey, driverId);
        configuration.setDriverAttributes(userAttributes);

        this.clearText();

        // Get zendrive listener to catch drive start and end events
        ZendriveListener listener = getZendriveListener();

        // Show loading indicator
        showLoadingIndicator();

        // setup zendrive sdk
        Zendrive.setup(
                this.getActivity().getApplicationContext(),
                configuration,
                listener,
                new Zendrive.SetupCallback() {
                    @Override
                    public void onSetup(boolean setupResult) {

                        // hide loading indicator
                        hideLoadingIndicator();

                        if (setupResult == false) {
                            AlertDialog ad = new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.zendrive_setup_failure)
                                    .setNegativeButton("Close", null)
                                    .create();
                            ad.show();
                        }
                        else {
                            titleTextView.setText(R.string.zendrive_setup_success);
                            restartSDKButton.setText(R.string.restart_zendrive_sdk);
                        }
                    }
                }
        );
    }

    private ZendriveListener getZendriveListener() {
        ZendriveListener listener = new ZendriveListener() {
            @Override
            public void onDriveStart(DriveStartInfo startInfo) {
                if(titleTextView != null){
                    titleTextView.setText(R.string.driving_title);
                    detailTextView.setText("");
                }
            }

            @Override
            public void onDriveEnd(DriveInfo tripInfo) {
                if(titleTextView != null){
                    titleTextView.setText(R.string.drive_ended_title);
                    Double distanceInMiles = tripInfo.distanceMeters*0.000621371;
                    detailTextView.setText("Distance :" + String.format("%.3f", distanceInMiles) + " miles");
                }
            }
        };

        return listener;
    }

    private void clearText() {
        detailTextView.setText("");
        titleTextView.setText("");
    }

    private void showLoadingIndicator(){
        if(loadingIndicatorParent != null){
            loadingIndicatorParent.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingIndicator(){
        if(loadingIndicatorParent != null){
            loadingIndicatorParent.setVisibility(View.GONE);
        }
    }
}