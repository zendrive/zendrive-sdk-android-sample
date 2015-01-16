package com.zendrive.zendrivesdkdemo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText applicationKeyEditText;
    private Button restartSDKButton;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        detailTextView = (TextView) rootView.findViewById(R.id.detailTextView);
        applicationKeyEditText = (EditText) rootView.findViewById(R.id.applicationKeyEditText);

        String applicationKey = PreferenceManager.getApplicationKeyFromPrefs(this.getActivity());
        applicationKeyEditText.setText(applicationKey);
        applicationKeyEditText.setHint("your_application_key");

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
                String driverId = "someDriverId";
                MainFragment.this.initializeZendriveSDK(driverId);
            }
        });

        return rootView;
    }

    public void initializeZendriveSDK(final String driverId) {

        // Get user attributes (Optional)
        ZendriveDriverAttributes userAttributes = new ZendriveDriverAttributes();
        userAttributes.setFirstName("FirstName");
        userAttributes.setLastName("LastName");
        userAttributes.setEmail("userEmail");
        final String applicationKey = applicationKeyEditText.getText().toString();

        // Zendrive configuration
        ZendriveConfiguration configuration = new ZendriveConfiguration(
                applicationKey,
                driverId);
        configuration.setDriverAttributes(userAttributes);

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
                                    .setTitle("Zendrive setup failed")
                                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            initializeZendriveSDK(driverId);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create();
                            ad.show();
                        }
                        else {
                            detailTextView.setText("Zendrive SDK setup success!!");
                            PreferenceManager.saveApplicationKeyToPrefs
                                    (MainFragment.this.getActivity(), applicationKey);
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
                    titleTextView.setText("Driving");
                    detailTextView.setText("");
                }
            }

            @Override
            public void onDriveEnd(DriveInfo tripInfo) {
                if(titleTextView != null){
                    titleTextView.setText("Drive ended");
                    Double distanceInMiles = tripInfo.distanceMeters*0.000621371;
                    detailTextView.setText("Distance :" + String.format("%.3f", distanceInMiles) + " miles");
                }
            }
        };

        return listener;
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