package com.zendrive.zendrivesdkdemo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriverAttributes;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.ZendriveSetupCallback;

/**
 * Application UI Class.
 * Shows details about drive activity.
 */
public class MainFragment extends Fragment {

    private TextView titleTextView, detailTextView;
    private Button startDriveButton, endDriveButton;
    private ViewGroup loadingIndicatorParent;
    private TextView sdkKeyTextView, driverIdTextView;
    private Button startSDKButton;
    private static final int kLocationPermissionRequest = 42;


    // TODO: Set these optional attributes of the driver here.
    private static final String driverFirstName = "";
    private static final String driverLastName = "";
    private static final String driverEmail = "";

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processIntent(context, intent);
            }
        };
        localBroadcastManager.registerReceiver(receiver, getIntentFilterForLocalBroadcast());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        detailTextView = (TextView) rootView.findViewById(R.id.detailTextView);
        sdkKeyTextView = (TextView) rootView.findViewById(R.id.sdkKeyTextView);
        driverIdTextView = (TextView) rootView.findViewById(R.id.driverIdTextView);

        sdkKeyTextView.setText(Constants.zendriveSDKKey.equals("") ?
                getResources().getText(R.string.default_sdk_key) :
                Constants.zendriveSDKKey);
        driverIdTextView.setText(Constants.driverId.equals("") ? getResources().getText(R.string.default_driver_id) :
                Constants.driverId);

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

        loadingIndicatorParent = (ViewGroup) rootView.findViewById(R.id.loadingIndicatorParent);
        loadingIndicatorParent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });

        startSDKButton = (Button) rootView.findViewById(R.id.startSDKButton);
        if (Constants.zendriveSDKKey.equals("") || Constants.driverId.equals("")) {
            startSDKButton.setEnabled(false);
            startDriveButton.setEnabled(false);
            endDriveButton.setEnabled(false);
        } else {
            startSDKButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Teardown Zendrive SDK if running
                    Zendrive.teardown();

                    // Initialize zendrive sdk
                    MainFragment.this.initializeZendriveSDK(getActivity().getApplicationContext());
                }
            });
        }

        return rootView;
    }

    /**
     * returns intent filters, which application is interested in.
     *
     * @return
     */
    private static IntentFilter getIntentFilterForLocalBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.DRIVE_START);
        intentFilter.addAction(Constants.DRIVE_END);
        intentFilter.addAction(Constants.ACCIDENT);
        intentFilter.addAction(Constants.EVENT_LOCATION_PERMISSION_CHANGE);
        intentFilter.addAction(Constants.EVENT_LOCATION_SETTING_CHANGE);
        return intentFilter;
    }

    /**
     * Processes intents and update application UI.
     *
     * @param context
     * @param intent
     */
    private void processIntent(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Constants.DRIVE_START)) {
            if (titleTextView != null) {
                titleTextView.setText(R.string.driving_title);
                detailTextView.setText("");
            }
        } else if (action.equals(Constants.DRIVE_END)) {
            if (titleTextView != null) {
                titleTextView.setText(R.string.drive_ended_title);
                double distanceMeters = intent.getDoubleExtra(Constants.DRIVE_DISTANCE, 0.0);
                Double distanceInMiles = distanceMeters * 0.000621371;
                detailTextView.setText("Distance :" + String.format("%.3f", distanceInMiles) + " miles");
            }
        } else if (action.equals(Constants.ACCIDENT)) {
            if (titleTextView != null) {
                titleTextView.setText(R.string.accident_title);
                double confidence = intent.getDoubleExtra(Constants.ACCIDENT_CONFIDENCE, 0.0);
                detailTextView.setText("Confidence :" + confidence);
            }
        } else if (action.equals(Constants.EVENT_LOCATION_SETTING_CHANGE)) {
            boolean enabled = intent.getBooleanExtra(Constants.EVENT_LOCATION_SETTING_CHANGE, false);
            String settingStatus = (enabled) ? "Enabled" : "Disabled";
            Toast.makeText(context, "High Accuracy Location Setting " + settingStatus,
                    Toast.LENGTH_SHORT).show();
        } else if (action.equals(Constants.EVENT_LOCATION_PERMISSION_CHANGE)) {
            boolean granted = intent.getBooleanExtra(Constants.EVENT_LOCATION_PERMISSION_CHANGE, false);
            requestLocationPermission(granted);
        }
    }

    @Override
    public void onDestroyView() {
        localBroadcastManager.unregisterReceiver(this.receiver);
        Zendrive.teardown();
        super.onDestroyView();
    }

    private void initializeZendriveSDK(Context applicationContext) {

        // Get user attributes (Optional)
        ZendriveDriverAttributes userAttributes = new ZendriveDriverAttributes();
        if (null != driverFirstName && !driverFirstName.equals("")) {
            userAttributes.setFirstName(driverFirstName);
        }
        if (null != driverLastName && !driverLastName.equals("")) {
            userAttributes.setLastName(driverLastName);
        }
        if (null != driverEmail && !driverEmail.equals("")) {
            userAttributes.setEmail(driverEmail);
        }

        // Zendrive configuration
        ZendriveConfiguration configuration = new ZendriveConfiguration(
                Constants.zendriveSDKKey, Constants.driverId);
        configuration.setDriverAttributes(userAttributes);

        this.clearText();

        // Show loading indicator
        showLoadingIndicator();

        ZendriveManager zendriveManager = ZendriveManager.getSharedInstance(getActivity().getApplicationContext());
        if (zendriveManager.isSDKSetup()) {
            return;
        }
        zendriveManager.initializeZendriveSDK(applicationContext, configuration,
                new ZendriveSetupCallback() {
                    @Override
                    public void onSetup(ZendriveOperationResult setupResult) {
                        hideLoadingIndicator();
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity == null) {
                            return;
                        }
                        if (setupResult.isSuccess()) {
                            // TODO: Show error in toast.
                            Log.d(Constants.LOG_TAG_DEBUG, "Setup Success");
                            titleTextView.setText(R.string.zendrive_setup_success);
                        } else {
                            Log.d(Constants.LOG_TAG_DEBUG, "Setup Failed" + setupResult.getErrorMessage());
                            AlertDialog ad = new AlertDialog.Builder(getActivity())
                                    .setTitle(
                                            getResources().getString(
                                                    R.string.zendrive_setup_failure) + " " +
                                                    setupResult.getErrorCode().toString())
                                    .setNegativeButton("Close", null)
                                    .create();
                            // TODO: Show error in toast.
                            ad.show();
                        }
                    }
                }
        );
    }

    private void requestLocationPermission(boolean granted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!granted) {
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        kLocationPermissionRequest);
            } else {
                Toast.makeText(this.getActivity().getApplicationContext(), "Location permission granted", Toast.LENGTH_LONG).show();
            }
        } else {
            throw new RuntimeException("Callback on non marshmallow sdk");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grants) {
        if (requestCode == kLocationPermissionRequest) {
            if (grants[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(Constants.LOG_TAG_DEBUG, "Permission granted for fine location");
            } else {
                new AlertDialog.Builder(this.getActivity())
                        .setTitle("Location permission")
                        .setMessage("Permission denied for fine location")
                        .setPositiveButton("Ok", null)
                        .create()
                        .show();
                Log.d(Constants.LOG_TAG_DEBUG, "Permission denied for fine location");
            }
        }
    }

    private void clearText() {
        detailTextView.setText("");
        titleTextView.setText("");
    }

    private void showLoadingIndicator() {
        if (loadingIndicatorParent != null) {
            loadingIndicatorParent.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingIndicator() {
        if (loadingIndicatorParent != null) {
            loadingIndicatorParent.setVisibility(View.GONE);
        }
    }

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver receiver;

}