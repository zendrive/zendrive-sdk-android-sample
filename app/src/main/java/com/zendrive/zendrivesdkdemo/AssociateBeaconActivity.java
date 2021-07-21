package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.zendrive.sdk.ZendriveBeaconScanCallback;
import com.zendrive.sdk.ZendriveScannedBeaconInfo;
import com.zendrive.sdk.ZendriveVehicleTagging;
import com.zendrive.sdk.ZendriveVehicleTaggingOperationResult;
import com.zendrive.zendrivesdkdemo.databinding.ActivityAssociateBeaconBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssociateBeaconActivity extends AppCompatActivity {

    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 1;

    private ActivityAssociateBeaconBinding binding;
    private ZendriveBeaconScanCallback beaconScanCallback;
    private BeaconListAdapter beaconListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST_CODE);
        } else {
            loadScannedBeacons();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_ENABLE_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    loadScannedBeacons();
                    break;
                }
                case Activity.RESULT_CANCELED: {
                    new AlertDialog.Builder(this)
                            .setTitle("Please enable bluetooth")
                            .setMessage("Bluetooth is required to scan for nearby beacons")
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .show();
                }
                default: {
                    finish();
                    break;
                }
            }
        }
    }

    private void loadScannedBeacons() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_associate_beacon);
        binding.scanBeacons.setOnClickListener(view -> showUuidInputDialog());

        beaconListAdapter = new BeaconListAdapter(this, new ArrayList<>());
        binding.scannedBeaconList.setAdapter(beaconListAdapter);

        beaconScanCallback = (scanResult, scannedBeaconInfoList) -> {
            List<ScannedBeacon> beaconList =  new ArrayList<>();
            if (scanResult == ZendriveVehicleTaggingOperationResult.SUCCESS) {
                for (ZendriveScannedBeaconInfo scannedBeaconInfo: scannedBeaconInfoList) {
                    beaconList.add(new ScannedBeacon(scannedBeaconInfo.getUuid(),
                            scannedBeaconInfo.getMajor(), scannedBeaconInfo.getMinor()));
                }
            } else {
                Toast.makeText(this, "Beacon scan failed", Toast.LENGTH_LONG).show();
            }
            binding.beaconScanProgressBar.setVisibility(View.GONE);
            binding.scanBeacons.setEnabled(true);
            beaconListAdapter.updateScannedBeaconList(beaconList);
        };
    }

    private void showUuidInputDialog() {
        EditText uuidInput = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Enter UUID")
                .setView(uuidInput)
                .setPositiveButton("OK", (dialog, which) -> {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(uuidInput.getText().toString());
                    } catch (IllegalArgumentException e) {
                        uuid = null;
                    }

                    if (uuid == null) {
                        Toast.makeText(this, "Invalid UUID", Toast.LENGTH_LONG).show();
                    } else {
                        ZendriveVehicleTagging.getNearbyBeacons(this.getApplicationContext(),
                                uuid, null, null, beaconScanCallback);
                        binding.beaconScanProgressBar.setVisibility(View.VISIBLE);
                        binding.scanBeacons.setEnabled(false);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }
}
