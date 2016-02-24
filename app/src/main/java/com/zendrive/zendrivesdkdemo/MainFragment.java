package com.zendrive.zendrivesdkdemo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.zendrive.sdk.AccidentFeedback;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveAccidentConfidence;
import com.zendrive.sdk.ZendriveAccidentDetectionMode;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveDriverAttributes;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.ZendriveSetupCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Application UI Class.
 * Shows details about drive activity.
 */
public class MainFragment extends BaseFragment {

    private TextView titleTextView, detailTextView;
    private Button startDriveButton, endDriveButton;
    private Button triggerAccidentButton;
    private ViewGroup loadingIndicatorParent;
    private TextView driverIdTextView;
    private static final int kLocationPermissionRequest = 42;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceStatep) {
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processIntent(context, intent);
            }
        };
        localBroadcastManager.registerReceiver(receiver, getIntentFilterForLocalBroadcast());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        setupUI(rootView);
        // setup zendrive sdk if not already setup.
        if(!Zendrive.isSDKSetup()) {
            // setting UI components.
            initializeZendriveSDK();
        } else {
            // incase of zendrive sdk running already in background.
            ZendriveManager zendriveManager = ZendriveManager.getSharedInstance(getContext().getApplicationContext());
            if (zendriveManager.isDriveInProgress()) {
                refreshUI(true);
            } else {
                refreshUI(false);
            }
        }
        return rootView;
    }

    /**
     * - setup UI components.
     * - drive start button.
     * - drive end button.
     * - trigger mock accident button.
     * - trip details list.
     *
     * @param rootView
     * @return
     */
    private View setupUI(View rootView) {

        titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        detailTextView = (TextView) rootView.findViewById(R.id.detailTextView);
        driverIdTextView = (TextView) rootView.findViewById(R.id.driverIdTextView);
        this.tripListView = (ListView) rootView.findViewById(R.id.tripListView);

        // trigger mock accident button.
        triggerAccidentButton = (Button) rootView.findViewById(R.id.triggerAccidentButton);
        triggerAccidentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate a mock accident.
                ZendriveOperationResult result = Zendrive.triggerMockAccident(getActivity().getApplicationContext(),
                        ZendriveAccidentConfidence.HIGH);
                if (result.isSuccess()) {
                    Log.d(Constants.LOG_TAG_DEBUG, "Accident trigger success");
                } else {
                    Log.d(Constants.LOG_TAG_DEBUG, "Accident trigger failed: " +
                            result.getErrorMessage());
                }
            }
        });
        // by default accident trigger button is disabled.
        triggerAccidentButton.setEnabled(false);
        // start drive button.
        startDriveButton = (Button) rootView.findViewById(R.id.startDriveButton);
        startDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate a random tracking id
                String trackingId = "" + System.currentTimeMillis();
                // Zendrive start Drive API.
                Zendrive.startDrive(trackingId);
                startDriveButton.setEnabled(false);
                endDriveButton.setEnabled(true);
            }
        });

        // end drive button.
        endDriveButton = (Button) rootView.findViewById(R.id.endDriveButton);
        endDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Zendrive.stopDrive();
                triggerAccidentButton.setEnabled(false);
                endDriveButton.setEnabled(false);
                startDriveButton.setEnabled(true);
            }
        });

        // loading indicator.
        loadingIndicatorParent = (ViewGroup) rootView.findViewById(R.id.loadingIndicatorParent);
        loadingIndicatorParent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });

        return rootView;
    }


    private AlertDialog getAccidentFeedBackAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResources().getString(R.string.accident_feedback_title));
        builder.setNegativeButton(getResources().getString(R.string.no_accdient),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addAccidentFeedback(false);
                        dialog.cancel();
                    }
                });

        builder.setPositiveButton(getResources().getString(R.string.accident),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addAccidentFeedback(false);
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        return alert;
    }

    private void addAccidentFeedback(boolean isAccident) {
        if (accidentId != null) {
            AccidentFeedback.Builder builder = new AccidentFeedback.Builder(accidentId, isAccident);
            ZendriveOperationResult result = Zendrive.addAccidentFeedback(accidentId,
                    builder.build());
            if (result.isSuccess()) {
                Log.d(Constants.LOG_TAG_DEBUG, "Accident feedback successful.");
            } else {
                Log.d(Constants.LOG_TAG_DEBUG, "Accident feedback failed: " +
                        result.getErrorMessage());
            }
        } else {
            Log.d(Constants.LOG_TAG_DEBUG, "AccidentId is null");
        }
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
                refreshUI(true);
            }
        } else if (action.equals(Constants.DRIVE_END)) {
            refreshUI(false);
        } else if (action.equals(Constants.ACCIDENT)) {
            if (titleTextView != null) {
                // show accident info on UI.
                titleTextView.setText(R.string.accident_title);
                double confidence = intent.getDoubleExtra(Constants.ACCIDENT_CONFIDENCE, 0.0);
                detailTextView.setText("Confidence :" + confidence);
                this.accidentId = intent.getStringExtra(Constants.ACCIDENT_ID);
                AlertDialog alert = getAccidentFeedBackAlertDialog();
                alert.show();
            }
        } else if (action.equals(Constants.EVENT_LOCATION_SETTING_CHANGE)) {
        } else if (action.equals(Constants.EVENT_LOCATION_PERMISSION_CHANGE)) {
            boolean granted = intent.getBooleanExtra(Constants.EVENT_LOCATION_PERMISSION_CHANGE, false);
            requestLocationPermission(granted);
        }
    }

    // refresh UI based on driving activity.
    private void refreshUI(boolean isDriving){
        if(isDriving){
            if (titleTextView != null) {
                titleTextView.setText(R.string.driving_title);
                detailTextView.setText("");
            }
            triggerAccidentButton.setEnabled(true);
            endDriveButton.setEnabled(true);
            startDriveButton.setEnabled(false);
        } else {
            if (titleTextView != null) {
                titleTextView.setText(R.string.drive_ended_title);
                detailTextView.setText("");
            }
            reloadTripListView();
            triggerAccidentButton.setEnabled(false);
            endDriveButton.setEnabled(false);
            startDriveButton.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        localBroadcastManager.unregisterReceiver(this.receiver);
        super.onDestroyView();
    }

    public void initializeZendriveSDK() {

        ZendriveManager zendriveManager = ZendriveManager.getSharedInstance(getActivity().getApplicationContext());
        if (zendriveManager.isSDKSetup()) {
            return;
        }
        // check for valid sdk key.
        if(null == Constants.zendriveSDKKey || Constants.zendriveSDKKey.equalsIgnoreCase("")){
            AlertDialog alertDialog = getSdkKeyAlertDialog();
            alertDialog.show();
            return;
        }

        ZendriveConfiguration configuration = zendriveManager.getSavedConfiguration();
        if(null == configuration){
            return;
        }
        this.clearText();
        // Show loading indicator
        showLoadingIndicator();

        zendriveManager.initializeZendriveSDK(configuration,
                new ZendriveSetupCallback() {
                    @Override
                    public void onSetup(ZendriveOperationResult setupResult) {
                        hideLoadingIndicator();
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity == null) {
                            return;
                        }
                        if (setupResult.isSuccess()) {
                            Log.d(Constants.LOG_TAG_DEBUG, "Setup Success");
                            titleTextView.setText(R.string.zendrive_setup_success);
                            // enable drive buttons
                            startDriveButton.setEnabled(true);
                            endDriveButton.setEnabled(false);
                            triggerAccidentButton.setEnabled(false);
                            // load previous trips.
                            reloadTripListView();
                        } else {
                            Log.d(Constants.LOG_TAG_DEBUG, "Setup Failed" + setupResult.getErrorMessage());
                            AlertDialog ad = new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(
                                                    R.string.zendrive_setup_failure) + " " +
                                            setupResult.getErrorCode().toString())
                                    .setNegativeButton("Close", null)
                                    .create();
                            ad.show();
                        }
                    }
                }
        );
    }

    private AlertDialog getSdkKeyAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResources().getString(R.string.invalid_sdk_key));
        builder.setMessage(getResources().getString(R.string.default_sdk_key));
        AlertDialog alert = builder.create();
        return alert;
    }

    private void requestLocationPermission(boolean granted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!granted) {
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        kLocationPermissionRequest);
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
                        .setTitle(getResources().getString(R.string.location_permission))
                        .setMessage(getResources().getString(R.string.location_permission_denied))
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

    private void reloadTripListView() {
        TripListDetails tripDetails = loadTripDetails();
        int size = tripDetails.tripList.size();
        String[] values = new String[size];
        int j = 0;
        for (int i = size - 1; i >= 0; i--) {
            DriveInfo info = tripDetails.tripList.get(i);
            String startTime = getDateString(info.startTimeMillis);
            String endTime = getDateString(info.endTimeMillis);
            double distanceValue = (info.distanceMeters * 0.000621371);
            String distance = String.format("%.2f", distanceValue);
            String value = "Trip Start: " + startTime + "\nTrip End: " + endTime + "\nDistance: " + distance + "miles";
            values[j] = value;
            j++;
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(),
                R.layout.activity_listview, values);
        tripListView.setAdapter(adapter);
    }

    private String getDateString(long timestamp) {
        Date d = new Date(timestamp);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm z");
        return dateFormat.format(d).toString();
    }

    private TripListDetails loadTripDetails() {

        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        String tripDetailsJsonString = sharedPreferences.getString(Constants.TRIP_DETAILS_KEY, null);
        if (null == tripDetailsJsonString) {
            return new TripListDetails();
        }
        return new Gson().fromJson(tripDetailsJsonString, TripListDetails.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem fav = menu.add(0, 0 ,1 , getResources().getString(R.string.setting_button));
        fav.setIcon(android.R.drawable.btn_minus);
        MenuItem logout = menu.add(0, 1, 2, getResources().getString(R.string.logout_button));
        logout.setIcon(android.R.drawable.btn_minus);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity activity = (MainActivity) getActivity();
        if(item.getItemId() == 0) {
            activity.loadSettingScreen();
        } else {
            Zendrive.teardown();
            ZendriveManager.getSharedInstance(getContext()).clearSettings();
            activity.setOrUnsetWakeupAlarm(false);
            activity.loadLoginScreen();
        }
        return false;
    }

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver receiver;
    private ListView tripListView;
    private String accidentId = null;

}