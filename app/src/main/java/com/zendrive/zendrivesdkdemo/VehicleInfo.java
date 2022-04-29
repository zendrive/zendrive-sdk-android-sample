package com.zendrive.zendrivesdkdemo;

import com.zendrive.sdk.ZendriveVehicleInfo;

public class VehicleInfo {

    String vehicleId;
    String bluetoothAddress;

    public VehicleInfo(String vehicleId, String bluetoothAddress) {
        this.vehicleId = vehicleId;
        this.bluetoothAddress = bluetoothAddress;
    }

    public static VehicleInfo fromZendriveVehicleInfo(ZendriveVehicleInfo zendriveVehicleInfo) {
        return new VehicleInfo(zendriveVehicleInfo.getVehicleId(),
                zendriveVehicleInfo.getBluetoothAddress());
    }
}
