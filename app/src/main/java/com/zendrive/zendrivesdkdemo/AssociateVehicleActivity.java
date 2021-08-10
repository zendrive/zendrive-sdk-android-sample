package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.zendrive.sdk.ZendriveVehicleInfo;
import com.zendrive.sdk.ZendriveVehicleTagging;
import com.zendrive.sdk.ZendriveVehicleTaggingOperationResult;
import com.zendrive.zendrivesdkdemo.databinding.ActivityAssociateVehicleBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssociateVehicleActivity extends AppCompatActivity implements VehicleIdInputListener,
        View.OnClickListener {

    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 1;

    ActivityAssociateVehicleBinding binding;
    private BluetoothDeviceListAdapter bluetoothDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST_CODE);
        } else {
            loadContent();
        }
    }

    @Override
    public void setVehicleId(String vehicleId) {
        binding.vehicleIdInput.setText(Editable.Factory.getInstance().newEditable(
                getVehicleId(getApplicationContext(), vehicleId)));
    }

    /**
     * Returns a globally unique identifier to identify this installation uniquely
     *
     * @param trimChars truncate the string to return only last trimChars characters
     */
    @NonNull
    public static String getUUID(Context context, int trimChars) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String uuid = sharedPreferences.getString(Constants.UUID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(Constants.UUID, uuid).apply();
        }
        return uuid.length() <= trimChars || trimChars <= 0 ? uuid :
                uuid.substring(uuid.length() - trimChars);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.associate) {
            PairedBluetoothDevice selectedDevice = bluetoothDeviceListAdapter.getSelectedBluetoothDevice();
            if (selectedDevice == null) {
                Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show();
            } else {
                ZendriveVehicleInfo zendriveVehicleInfo = new ZendriveVehicleInfo(
                        binding.vehicleIdInput.getText().toString(), selectedDevice.address);
                ZendriveVehicleTaggingOperationResult result = ZendriveVehicleTagging.associateVehicle(
                        this, zendriveVehicleInfo);
                if (result != ZendriveVehicleTaggingOperationResult.SUCCESS) {
                    Toast.makeText(this, "Unable to associate vehicle", Toast.LENGTH_SHORT).show();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        } else if (v == binding.cancelAssociate) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_ENABLE_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    loadContent();
                    break;
                }
                case Activity.RESULT_CANCELED: {
                    new AlertDialog.Builder(this)
                            .setTitle("Please enable bluetooth")
                            .setMessage("Bluetooth is required to get paired devices")
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

    private void loadContent() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_associate_vehicle);
        ArrayList<PairedBluetoothDevice> deviceList = new ArrayList();
        List<BluetoothDevice> bluetoothDevices = ZendriveVehicleTagging.
                getBluetoothPairedDevices(this);
        if (bluetoothDevices != null) {
            for (BluetoothDevice device : bluetoothDevices) {
                deviceList.add(new PairedBluetoothDevice(device.getName(), device.getAddress(), false));
            }
        }

        bluetoothDeviceListAdapter = new BluetoothDeviceListAdapter(this, deviceList);
        binding.bluetoothDeviceList.setAdapter(bluetoothDeviceListAdapter);
        binding.vehicleIdInput.getText().clear();
        binding.associate.setOnClickListener(this);
        binding.cancelAssociate.setOnClickListener(this);
    }

    /**
     * Get vehicle Id from device name. Remove invalid characters
     *
     * @param context
     * @param deviceName contains invalid characters
     * @return vehicle id
     */
    private String getVehicleId(Context context, String deviceName) {
        // Replace space with underscore
        String vehicleId = deviceName.replace(" ", "_");
        vehicleId = vehicleId.replaceAll("\\? \\\\&/;#", "");
        // Max length of allowed vehicle id is 64 chars
        vehicleId = vehicleId.substring(0, Math.min(vehicleId.length(), 60));
        return vehicleId + "_" + getUUID(context, 3);
    }
}
