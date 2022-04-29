package com.zendrive.zendrivesdkdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.zendrive.sdk.ZendriveBeaconScanCallback;
import com.zendrive.sdk.ZendriveScannedBeaconInfo;
import com.zendrive.sdk.ZendriveVehicleTagging;
import com.zendrive.sdk.ZendriveVehicleTaggingOperationResult;
import com.zendrive.zendrivesdkdemo.databinding.ActivityScannedBeaconsListBinding;
import com.zendrive.zendrivesdkdemo.databinding.ScannedBeaconListItemBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScannedBeaconListActivity extends AppCompatActivity {

    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 1;
    private ActivityScannedBeaconsListBinding binding;
    private ScannedBeaconListAdapter beaconListAdapter;
    private ZendriveBeaconScanCallback beaconScanCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST_CODE);
        } else {
            loadScannedBeacons();
        }
    }

    private void loadScannedBeacons() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scanned_beacons_list);
        showUuidInputDialog();
        beaconListAdapter = new ScannedBeaconListAdapter(new ArrayList<>());
        binding.scannedBeaconList.setAdapter(beaconListAdapter);
        beaconScanCallback = (scanResult, scannedBeaconInfoList) -> {
            List<ScannedBeacon> beaconList = new ArrayList<>();
            if (scanResult == ZendriveVehicleTaggingOperationResult.SUCCESS) {
                for (int i = 0; i < scannedBeaconInfoList.size(); i++) {
                    ZendriveScannedBeaconInfo scannedBeacon = scannedBeaconInfoList.get(i);
                    beaconList.add(new ScannedBeacon(scannedBeacon.getUuid(),
                            scannedBeacon.getMajor(), scannedBeacon.getMinor()));
                }
            } else {
                Toast.makeText(getApplicationContext(), scanResult.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            binding.beaconScanProgressBar.setVisibility(View.GONE);
            TextView beaconListDescription = binding.beaconListDescription;
            if (beaconList.isEmpty()) {
                beaconListDescription.setText(" No beacons found");
                beaconListDescription.setTypeface(Typeface.DEFAULT);
            } else {
                beaconListDescription.setText(" List of scanned beacons");
                beaconListDescription.setTypeface(Typeface.DEFAULT_BOLD);
            }
            beaconListAdapter.updateScannedBeaconList(beaconList);
        };
    }

    private void showUuidInputDialog() {
        EditText uuidInput = new EditText(this);
        uuidInput.setHint("UUID");
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
                        ZendriveVehicleTagging.getNearbyBeacons(
                                this.getApplicationContext(),
                                uuid, null, null, beaconScanCallback);
                        binding.beaconScanProgressBar.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    public static class ScannedBeaconListAdapter extends RecyclerView.Adapter<ScannedBeaconViewHolder> {
        private final List<ScannedBeacon> scannedBeaconList;

        public ScannedBeaconListAdapter(List<ScannedBeacon> scannedBeaconList) {
            this.scannedBeaconList = scannedBeaconList;
        }

        @NonNull
        @Override
        public ScannedBeaconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ScannedBeaconViewHolder(ScannedBeaconListItemBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ScannedBeaconViewHolder holder, int position) {
            ScannedBeacon scannedBeacon = scannedBeaconList.get(position);
            holder.binding.beaconMajor.setText(scannedBeacon.major.toString());
            holder.binding.beaconMinor.setText(scannedBeacon.minor.toString());
            holder.binding.beaconUuid.setText(scannedBeacon.uuid.toString());
            holder.binding.executePendingBindings();
        }


        @Override
        public int getItemCount() {
            return scannedBeaconList.size();
        }

        public void updateScannedBeaconList(List<ScannedBeacon> beaconList) {
            scannedBeaconList.clear();
            scannedBeaconList.addAll(beaconList);
            notifyDataSetChanged();
        }
    }

    static class ScannedBeaconViewHolder extends RecyclerView.ViewHolder {

        ScannedBeaconListItemBinding binding;

        public ScannedBeaconViewHolder(@NonNull ScannedBeaconListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
