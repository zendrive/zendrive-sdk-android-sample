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
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveAccidentConfidence;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriveType;
import com.zendrive.sdk.ZendriveEventType;
import com.zendrive.sdk.ZendriveLocationSettingsResult;
import com.zendrive.sdk.ZendriveOperationCallback;
import com.zendrive.sdk.ZendriveOperationResult;
import com.zendrive.sdk.feedback.ZendriveFeedback;
import com.zendrive.zendrivesdkdemo.databinding.ActivityMainBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.zendrive.zendrivesdkdemo.Constants.*;

/**
 * Main UI.
 * Shows current state and details about drive activity.
 */
public class MainActivity extends Activity implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    private TextView titleTextView;
    private Button startDriveButton, endDriveButton;
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
                processIntent(intent);
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
                        Log.d(LOG_TAG_DEBUG, "Setup Success");
                        refreshUI();
                    } else {
                        Log.d(Constants.LOG_TAG_DEBUG, "Setup Failed: " + setupResult.getErrorMessage());
                        AlertDialog ad = new AlertDialog.Builder(MainActivity.this).setTitle(
                                getResources().getString(R.string.zendrive_setup_failure) + " " +
                                        setupResult.getErrorCode().toString())
                                .setNegativeButton("Close", null).create();
                        ad.show();
                    }
                }
            }, true);
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

        Button triggerAccidentButton = binding.triggerAccidentButton;
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
            Zendrive.triggerMockAccident(getApplicationContext(),
                    ZendriveAccidentConfidence.HIGH, new ZendriveOperationCallback() {
                        @Override
                        public void onCompletion(ZendriveOperationResult result) {
                            if (result.isSuccess()) {
                                Log.d(LOG_TAG_DEBUG, "Accident trigger success");
                            } else {
                                Log.d(LOG_TAG_DEBUG, "Accident trigger failed: " +
                                        result.getErrorMessage());
                            }
                        }
                    });
        } else if (view == binding.startDriveButton) {
            // Zendrive start Drive API.
            Zendrive.startDrive(TRIP_TRACKING_ID, new ZendriveOperationCallback() {
                @Override
                public void onCompletion(ZendriveOperationResult zendriveOperationResult) {
                    if (!zendriveOperationResult.isSuccess()) {
                        startDriveButton.setEnabled(true);
                    }
                }
            });
            startDriveButton.setEnabled(false);
        } else if (view == binding.endDriveButton) {
            Zendrive.stopDrive(TRIP_TRACKING_ID, new ZendriveOperationCallback() {
                @Override
                public void onCompletion(ZendriveOperationResult zendriveOperationResult) {
                    if (!zendriveOperationResult.isSuccess()) {
                        endDriveButton.setEnabled(true);
                    }
                }
            });
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

    private AlertDialog getAccidentFeedBackAlertDialog(final String driveId,
                                                       final long collisionTimestamp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.collision_feedback_title));
        builder.setNegativeButton(getResources().getString(R.string.no_collision),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ZendriveFeedback.addEventOccurrence(driveId, collisionTimestamp,
                                ZendriveEventType.COLLISION, false);
                    }
                });

        builder.setPositiveButton(getResources().getString(R.string.collision),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ZendriveFeedback.addEventOccurrence(driveId, collisionTimestamp,
                                ZendriveEventType.COLLISION, true);
                    }
                });
        return builder.create();
    }

    /**
     * returns intent filters, which application is interested in.
     */
    private static IntentFilter getIntentFilterForLocalBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACCIDENT);
        intentFilter.addAction(EVENT_LOCATION_PERMISSION_CHANGE);
        intentFilter.addAction(EVENT_LOCATION_SETTING_CHANGE);
        intentFilter.addAction(REFRESH_UI);
        return intentFilter;
    }

    /**
     * Processes intents and update application UI.
     */
    private void processIntent(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACCIDENT:
                if (titleTextView != null) {
                    // show accident info on UI.
                    titleTextView.setText(R.string.collision_title);
                    AlertDialog alert = getAccidentFeedBackAlertDialog(
                            intent.getStringExtra(DRIVE_ID),
                            intent.getLongExtra(ACCIDENT_TIMESTAMP, -1));
                    alert.show();
                }
                break;
            case EVENT_LOCATION_SETTING_CHANGE:
                ZendriveLocationSettingsResult result = intent.getParcelableExtra(EVENT_LOCATION_SETTING_CHANGE);
                if (result != null && !result.isSuccess()) {
                    LocationSettingsHelper.resolveLocationSettings(this, result);
                }
                // Maybe ask user to re-enable the location settings.
                break;
            case EVENT_LOCATION_PERMISSION_CHANGE:
                boolean granted =
                        intent.getBooleanExtra(EVENT_LOCATION_PERMISSION_CHANGE, false);
                requestLocationPermission(granted);
                break;
            case REFRESH_UI:
                refreshUI();
                if (intent.hasExtra(ERROR)) {
                    titleTextView.setText(intent.getStringExtra(ERROR));
                }
                break;
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

    public static void initializeZendriveSDK(Context context,
            ZendriveOperationCallback setupCallback, boolean showAlertDialog) {
        ZendriveManager zendriveManager = ZendriveManager.getSharedInstance(context.getApplicationContext());
        if (zendriveManager.isSdkInitialized()) {
            return;
        }
        // check for valid sdk key.
        if(null == Constants.zendriveSDKKey || Constants.zendriveSDKKey.equals("")){
            if (showAlertDialog) {
                AlertDialog alertDialog = getSdkKeyAlertDialog(context);
                alertDialog.show();
            } else {
                Log.e(Constants.LOG_TAG_DEBUG,
                        context.getResources().getString(R.string.default_sdk_key));
            }
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
                Log.d(LOG_TAG_DEBUG, "Permission granted for fine location");
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.location_permission))
                        .setMessage(getResources().getString(R.string.location_permission_denied))
                        .setPositiveButton("Ok", null)
                        .create()
                        .show();
                Log.d(LOG_TAG_DEBUG, "Permission denied for fine location");
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
            String distance = String.format(Locale.US, "%.2f", distanceValue);
            ZendriveDriveType driveType = info.driveType;
            String type = info.userMode != null ? info.userMode.name() : driveType.name();
            String value = String.format(
                    "Trip Start: %s\n" +
                    "Trip End: %s\n" +
                    "Distance: %smiles\n" +
                    "%s\n",
                    startTime, endTime, distance, type);
            values[j] = value;
            j++;
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this.getApplicationContext(),
                                                        R.layout.activity_listview, values);
        tripListView.setAdapter(adapter);
    }

    private String getDateString(long timestamp) {
        Date d = new Date(timestamp);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm z", Locale.US);
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
            WakeupAlarmManager.getInstance().unsetAlarm(getApplicationContext());
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocationSettingsHelper.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    break;
                default:
                    break;
            }
        }
    }

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver receiver;
    private ListView tripListView;
    private String accidentId = null;
    private String trackingId;
}
