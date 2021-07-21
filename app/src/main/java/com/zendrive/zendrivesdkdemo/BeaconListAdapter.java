package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.zendrive.sdk.ZendriveVehicleBeacon;
import com.zendrive.sdk.ZendriveVehicleTagging;
import com.zendrive.sdk.ZendriveVehicleTaggingOperationResult;
import com.zendrive.zendrivesdkdemo.databinding.ScannedBeaconListItemBinding;

import java.util.List;

public class BeaconListAdapter extends
        RecyclerView.Adapter<BeaconListAdapter.ScannedBeaconViewHolder> {

    private final AssociateBeaconActivity activity;
    private final List<ScannedBeacon> beaconList;

    BeaconListAdapter(AssociateBeaconActivity activity, List<ScannedBeacon> scannedBeaconList) {
        this.activity = activity;
        this.beaconList = scannedBeaconList;
    }

    @Override
    public ScannedBeaconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ScannedBeaconViewHolder(ScannedBeaconListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false), this);
    }

    @Override
    public void onBindViewHolder(ScannedBeaconViewHolder holder, int position) {
        ScannedBeacon entry = beaconList.get(position);
        holder.beacon = entry;
        holder.binding.beaconUuid.setText(entry.uuid.toString());
        holder.binding.beaconMajor.setText(entry.major.toString());
        holder.binding.beaconMinor.setText(entry.minor.toString());
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return beaconList.size();
    }

    public void onClick(View view, ScannedBeacon beacon) {
        if (beacon != null) {
            associateBeacon(view, beacon);
        }
    }

    public void updateScannedBeaconList(List<ScannedBeacon> scannedBeaconList) {
        beaconList.clear();
        beaconList.addAll(scannedBeaconList);
        notifyDataSetChanged();
    }

    private void associateBeacon(View view, ScannedBeacon beacon) {
        Context context = view.getContext();
        EditText vehicleIdInput = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle("Enter vehicle Id")
                .setView(vehicleIdInput)
                .setMessage(beacon.toString())
                .setPositiveButton("OK", (dialog, which) -> {
                    if (vehicleIdInput.getText() == null) {
                        Toast.makeText(context, "Invalid vehicle Id", Toast.LENGTH_LONG).show();
                    } else {
                        ZendriveVehicleBeacon zendriveVehicleBeacon =
                                new ZendriveVehicleBeacon(beacon.uuid, beacon.major,
                                        beacon.minor, vehicleIdInput.getText().toString());
                        ZendriveVehicleTaggingOperationResult result =
                                ZendriveVehicleTagging.associateBeacon(
                                        activity.getApplicationContext(), zendriveVehicleBeacon);
                        if (result != ZendriveVehicleTaggingOperationResult.SUCCESS) {
                            String msg = String.format("Unable to associate beacon %s \n %s",
                                    result.name(), result.getMessage());
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        } else {
                            activity.setResult(Activity.RESULT_OK);
                            activity.finish();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    static class ScannedBeaconViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ScannedBeacon beacon;
        BeaconListAdapter beaconListAdapter;
        ScannedBeaconListItemBinding binding;

        public ScannedBeaconViewHolder(ScannedBeaconListItemBinding binding,
                                       BeaconListAdapter beaconListAdapter) {
            super(binding.getRoot());
            this.binding = binding;
            this.beaconListAdapter = beaconListAdapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            beaconListAdapter.onClick(view, beacon);
        }
    }
}
