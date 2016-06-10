package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zendrive.sdk.AccidentFeedback;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveAccidentConfidence;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.zendrivesdkdemo.databinding.ActivityMainBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main UI.
 * Shows current state and details about drive activity.
 */
public class MainActivity extends Activity implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    private TextView titleTextView;
    private Button startDriveButton, endDriveButton;
    private Button triggerAccidentButton;
    private ActivityMainBinding binding;
    private static final int kLocationPermissionRequest = 42;
    private final SdkState sdkState = new SdkState();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processIntent(context, intent);
            }
        };
        localBroadcastManager.registerReceiver(receiver, getIntentFilterForLocalBroadcast());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setupUI();
        // setup zendrive sdk if not already setup.
        if(!Zendrive.isSDKSetup()) {
            // setting UI components.
            initializeZendriveSDK(this, new ZendriveOperationCallback() {
                @Override
                public void onCompletion(ZendriveOperationResult setupResult) {
                    if (setupResult.isSuccess()) {
                        Log.d(Constants.LOG_TAG_DEBUG, "Setup Success");
                        refreshUI();
                    } else {
                        Log.d(Constants.LOG_TAG_DEBUG, "Setup Failed" + setupResult.getErrorMessage());
                        AlertDialog ad = new AlertDialog.Builder(MainActivity.this).setTitle(
                                getResources().getString(R.string.zendrive_setup_failure) + " " +
                                        setupResult.getErrorCode().toString())
                                .setNegativeButton("Close", null).create();
                        ad.show();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    /**
     * - setup UI components.
     * - drive start button.
     * - drive end button.
     * - trigger mock accident button.
     * - trip details list.
     */
    private void setupUI() {
        binding.setClickHandler(this);
        binding.setState(sdkState);
        titleTextView = binding.titleTextView;
        tripListView = binding.tripListView;
        tripListView.setOnItemClickListener(this);
        // trigger mock accident button.

        triggerAccidentButton = binding.triggerAccidentButton;
        // by default accident trigger button is disabled.
        triggerAccidentButton.setEnabled(false);
        // start drive button.
        startDriveButton = binding.startDriveButton;

        // end drive button.
        endDriveButton = binding.endDriveButton;
    }

    public void onClick(View view) {
        if (view == binding.triggerAccidentButton) {
            // Generate a mock accident.
            ZendriveOperationResult result = Zendrive.triggerMockAccident(getApplicationContext(),
                                                                          ZendriveAccidentConfidence.HIGH);
            if (result.isSuccess()) {
                Log.d(Constants.LOG_TAG_DEBUG, "Accident trigger success");
            } else {
                Log.d(Constants.LOG_TAG_DEBUG, "Accident trigger failed: " +
                        result.getErrorMessage());
            }
        } else if (view == binding.startDriveButton) {
            // Generate a random tracking id
            String trackingId = "" + System.currentTimeMillis();
            // Zendrive start Drive API.
            Zendrive.startDrive(trackingId);
            startDriveButton.setEnabled(false);
        } else if (view == binding.endDriveButton) {
            Zendrive.stopDrive();
            endDriveButton.setEnabled(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TripListDetails tripDetails = loadTripDetails();
        if (null == tripDetails || null == tripDetails.tripList ||
                tripDetails.tripList.size() == 0) {
            return;
        }
        int size = tripDetails.tripList.size();
        // fetch corresponding drive info (trip details).
        DriveInfo driveInfo = tripDetails.tripList.get(size - (position + 1));
        if (driveInfo.waypoints.isEmpty()) {
            Toast.makeText(this, "No Location available for the trip.", Toast.LENGTH_SHORT).show();
        } else {
            displayMap(driveInfo);
        }
    }

    private void displayMap(DriveInfo driveInfo) {
        ((MyApplication) getApplication()).driveInfo = driveInfo;
        startActivity(new Intent(this, MapActivity.class));
    }

    private AlertDialog getAccidentFeedBackAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        addAccidentFeedback(true);
                        dialog.cancel();
                    }
                });
        return builder.create();
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
            accidentId = null;
        } else {
            Log.d(Constants.LOG_TAG_DEBUG, "AccidentId is null");
        }
    }

    /**
     * returns intent filters, which application is interested in.
     */
    private static IntentFilter getIntentFilterForLocalBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACCIDENT);
        intentFilter.addAction(Constants.EVENT_LOCATION_PERMISSION_CHANGE);
        intentFilter.addAction(Constants.EVENT_LOCATION_SETTING_CHANGE);
        intentFilter.addAction(Constants.REFRESH_UI);
        return intentFilter;
    }

    /**
     * Processes intents and update application UI.
     */
    private void processIntent(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Constants.ACCIDENT)) {
            if (titleTextView != null) {
                // show accident info on UI.
                titleTextView.setText(R.string.accident_title);
                this.accidentId = intent.getStringExtra(Constants.ACCIDENT_ID);
                AlertDialog alert = getAccidentFeedBackAlertDialog();
                alert.show();
            }
        } else if (action.equals(Constants.EVENT_LOCATION_SETTING_CHANGE)) {
            // Maybe ask user to re-enable the location settings.
        } else if (action.equals(Constants.EVENT_LOCATION_PERMISSION_CHANGE)) {
            boolean granted =
                    intent.getBooleanExtra(Constants.EVENT_LOCATION_PERMISSION_CHANGE, false);
            requestLocationPermission(granted);
        } else if (action.equals(Constants.REFRESH_UI)) {
            refreshUI();
            if (intent.hasExtra(Constants.ERROR)) {
                titleTextView.setText(intent.getStringExtra(Constants.ERROR));
            }
        }
    }

    // refresh UI based on driving activity.
    private void refreshUI() {
        sdkState.update();
        if (sdkState.isSetup()) {
            reloadTripListView();
        }
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(this.receiver);
        super.onDestroy();
    }

    public static void initializeZendriveSDK(Context mainActivity,
                                             ZendriveOperationCallback setupCallback) {
        ZendriveManager zendriveManager = ZendriveManager.getSharedInstance(mainActivity.getApplicationContext());
        if (zendriveManager.isSdkInitialized()) {
            return;
        }
        // check for valid sdk key.
        if(null == Constants.zendriveSDKKey || Constants.zendriveSDKKey.equals("")){
            AlertDialog alertDialog = getSdkKeyAlertDialog(mainActivity);
            alertDialog.show();
            return;
        }

        ZendriveConfiguration configuration = zendriveManager.getSavedConfiguration();
        if(null == configuration){
            return;
        }

        zendriveManager.initializeZendriveSDK(configuration, setupCallback);
    }

    private static AlertDialog getSdkKeyAlertDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.invalid_sdk_key));
        builder.setMessage(context.getResources().getString(R.string.default_sdk_key));
        return builder.create();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grants) {
        if (requestCode == kLocationPermissionRequest) {
            if (grants[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(Constants.LOG_TAG_DEBUG, "Permission granted for fine location");
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.location_permission))
                        .setMessage(getResources().getString(R.string.location_permission_denied))
                        .setPositiveButton("Ok", null)
                        .create()
                        .show();
                Log.d(Constants.LOG_TAG_DEBUG, "Permission denied for fine location");
            }
        }
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
            String value = String.format(
                    "Trip Start: %s\n" +
                    "Trip End: %s\n" +
                    "Distance: %smiles\n", startTime, endTime, distance);
            // TODO: Add event summary and score
            values[j] = value;
            j++;
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this.getApplicationContext(),
                                                        R.layout.activity_listview, values);
        tripListView.setAdapter(adapter);
    }

    private String getDateString(long timestamp) {
        Date d = new Date(timestamp);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm z");
        return dateFormat.format(d);
    }

    private TripListDetails loadTripDetails() {

        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(this.getApplicationContext());
        String tripDetailsJsonString = sharedPreferences.getString(SharedPreferenceManager.TRIP_DETAILS_KEY, null);
        if (null == tripDetailsJsonString) {
            return new TripListDetails();
        }
        return new Gson().fromJson(tripDetailsJsonString, TripListDetails.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.logout){
            Zendrive.teardown(null);
            SharedPreferenceManager.clear(this);
            new WakeupAlarmManager(this.getApplicationContext()).unsetAlarm();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return false;
    }

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver receiver;
    private ListView tripListView;
    private String accidentId = null;
}