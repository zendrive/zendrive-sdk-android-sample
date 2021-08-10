package com.zendrive.zendrivesdkdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zendrive.zendrivesdkdemo.databinding.BluetoothDeviceListItemBinding;

import java.util.List;

public class BluetoothDeviceListAdapter extends
        RecyclerView.Adapter<BluetoothDeviceListAdapter.BluetoothDeviceViewHolder> {

    private final VehicleIdInputListener vehicleIdInputListener;
    private final List<PairedBluetoothDevice> bondedDeviceList;

    private int selectedItemPosition = -1;

    BluetoothDeviceListAdapter(VehicleIdInputListener vehicleIdInputListener,
                               List<PairedBluetoothDevice> bondedDeviceList) {
        this.vehicleIdInputListener = vehicleIdInputListener;
        this.bondedDeviceList = bondedDeviceList;
    }

    @NonNull
    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BluetoothDeviceViewHolder(BluetoothDeviceListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false), this);
    }

    @Override
    public void onBindViewHolder(BluetoothDeviceViewHolder holder, int position) {
        PairedBluetoothDevice entry = bondedDeviceList.get(position);
        holder.bluetooth = entry;
        holder.binding.bluetoothDeviceName.setText(entry.name);
        holder.binding.bluetoothDeviceAddress.setText(entry.address);
        holder.binding.checkbox.setChecked(entry.isChecked);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return bondedDeviceList.size();
    }

    public void onClick(int position) {
        PairedBluetoothDevice clickedDevice = bondedDeviceList.get(position);
        if (clickedDevice.isChecked) {
            clickedDevice.isChecked = false;
            selectedItemPosition = -1;
        } else {
            int previousSelection = selectedItemPosition;
            if (previousSelection != -1) {
                bondedDeviceList.get(previousSelection).isChecked = false;
            }
            clickedDevice.isChecked = true;
            selectedItemPosition = position;
            vehicleIdInputListener.setVehicleId(clickedDevice.name);
        }
        notifyItemChanged(position);
    }

    public PairedBluetoothDevice getSelectedBluetoothDevice() {
        int selection = selectedItemPosition;
        if (selection != -1) {
            return bondedDeviceList.get(selection);
        } else {
            return null;
        }
    }

    static class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        PairedBluetoothDevice bluetooth;
        BluetoothDeviceListAdapter bluetoothDeviceListAdapter;
        BluetoothDeviceListItemBinding binding;

        public BluetoothDeviceViewHolder(BluetoothDeviceListItemBinding binding,
                                         BluetoothDeviceListAdapter bluetoothDeviceListAdapter) {
            super(binding.getRoot());
            this.binding = binding;
            this.bluetoothDeviceListAdapter = bluetoothDeviceListAdapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            bluetoothDeviceListAdapter.onClick(getBindingAdapterPosition());
        }
    }
}

interface VehicleIdInputListener {
    void setVehicleId(String vehicleId);
}
