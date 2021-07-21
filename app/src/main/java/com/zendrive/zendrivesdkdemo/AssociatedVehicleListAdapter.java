package com.zendrive.zendrivesdkdemo;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.zendrive.zendrivesdkdemo.databinding.AssociatedVehicleListItemBinding;

import java.util.List;

public class AssociatedVehicleListAdapter extends
        RecyclerView.Adapter<AssociatedVehicleListAdapter.VehicleInfoViewHolder> {

    private final DissociateVehicleListener dissociateVehicleListener;
    private final List<VehicleInfo> vehicleInfoList;

    AssociatedVehicleListAdapter(DissociateVehicleListener dissociateVehicleListener,
                                 List<VehicleInfo> vehicleInfoList) {
        this.dissociateVehicleListener = dissociateVehicleListener;
        this.vehicleInfoList = vehicleInfoList;
    }

    @Override
    public VehicleInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VehicleInfoViewHolder(AssociatedVehicleListItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(VehicleInfoViewHolder holder, int position) {
        VehicleInfo vehicleInfo = vehicleInfoList.get(position);
        holder.binding.vehicleId.setText(vehicleInfo.vehicleId);
        String connectionDetails = String.format("UUID: %s\nMajor: %d Minor: %d",
                vehicleInfo.uuid.toString(), vehicleInfo.major, vehicleInfo.minor);
        holder.binding.connectionDetails.setText(connectionDetails);
        holder.binding.delete.setOnClickListener(
                view -> dissociateVehicleListener.onDissociateClick(vehicleInfo));
    }

    @Override
    public int getItemCount() {
        return vehicleInfoList.size();
    }

    static class VehicleInfoViewHolder extends RecyclerView.ViewHolder {

        AssociatedVehicleListItemBinding binding;

        public VehicleInfoViewHolder(AssociatedVehicleListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

interface DissociateVehicleListener {
    void onDissociateClick(VehicleInfo vehicleInfo);
}
