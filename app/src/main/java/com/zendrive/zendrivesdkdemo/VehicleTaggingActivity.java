package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.databinding.DataBindingUtil;

import com.zendrive.sdk.ZendriveVehicleInfo;
import com.zendrive.sdk.ZendriveVehicleTagging;
import com.zendrive.zendrivesdkdemo.databinding.ActivityVehicleTaggingBinding;

import java.util.ArrayList;
import java.util.List;

public class VehicleTaggingActivity extends AppCompatActivity implements DissociateVehicleListener {

    private static final int ASSOCIATE_VEHICLE_REQUEST_CODE = 1;

    private ActivityVehicleTaggingBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle_tagging);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Bluetooth not available")
                    .setMessage("The device doesn't seem to have bluetooth. " +
                            "This feature will not be available.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
        } else {
            loadVehicleList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ASSOCIATE_VEHICLE_REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
            loadVehicleList();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDissociateClick(@NonNull VehicleInfo vehicleInfo) {
        new AlertDialog.Builder(this)
                .setTitle("Dissociate Vehicle")
                .setMessage("Do you want to dissociate this vehicle?")
                .setCancelable(true)
                .setPositiveButton("YES", (dialog, which) -> {
                    ZendriveVehicleTagging.dissociateVehicle(this.getApplicationContext(),
                            vehicleInfo.vehicleId);
                    loadVehicleList();
                    dialog.dismiss();
                })
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadVehicleList() {
        List<VehicleInfo> vehicleInfoList = new ArrayList<>();
        List<ZendriveVehicleInfo> vehicleList =
                ZendriveVehicleTagging.getAssociatedVehicles(this.getApplicationContext());
        if (vehicleList != null) {
            for (ZendriveVehicleInfo zendriveVehicleInfo: vehicleList) {
                vehicleInfoList.add(VehicleInfo.fromZendriveVehicleInfo(zendriveVehicleInfo));
            }
        }

        TextView listDescriptionTextView = binding.associatedVehicleListDescription;
        if (vehicleInfoList.isEmpty()) {
            listDescriptionTextView.setText("No Vehicles associated");
            listDescriptionTextView.setTypeface(Typeface.DEFAULT);
        } else {
            listDescriptionTextView.setText("List of associated vehicle");
            listDescriptionTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        AssociatedVehicleListAdapter adapter = new AssociatedVehicleListAdapter(this,
                vehicleInfoList);
        binding.associatedVehiclesListView.setAdapter(adapter);
        binding.registerVehicle.setOnClickListener(v ->
                startActivityForResult(new Intent(getApplicationContext(),
                        AssociateVehicleActivity.class), ASSOCIATE_VEHICLE_REQUEST_CODE));
    }
}
