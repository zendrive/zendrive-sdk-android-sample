package com.zendrive.zendrivesdkdemo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
    private ProgressBar loadingIndicator;
    private ViewGroup loadingIndicatorParent;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        detailTextView = (TextView) rootView.findViewById(R.id.detailTextView);

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

        loadingIndicator = (ProgressBar)rootView.findViewById(R.id.loadingIndicator);
        loadingIndicatorParent = (ViewGroup)rootView.findViewById(R.id.loadingIndicatorParent);

        // Initialize zendrive sdk
        String driverId = "someUserId";
        this.initializeZendriveSDK(driverId);

        return rootView;
    }

    public void initializeZendriveSDK(final String driverId) {

        // Get user attributes (Optional)
        ZendriveDriverAttributes userAttributes = new ZendriveDriverAttributes();
        userAttributes.setFirstName("FirstName");
        userAttributes.setLastName("LastName");
        userAttributes.setEmail("userEmail");

        // Zendrive configuration
        ZendriveConfiguration configuration = new ZendriveConfiguration(
                "your_application_key",
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
                                    .setCancelable(false)
                                    .create();
                            ad.show();
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