package com.zendrive.zendrivesdkdemo;

import com.zendrive.sdk.ZendriveVehicleBeacon;
import com.zendrive.sdk.ZendriveVehicleInfo;

import java.util.UUID;

public class VehicleInfo {

    VehicleAssociationType vehicleAssociationType;
    String vehicleId;
    String bluetoothAddress;
    UUID uuid;
    Integer major;
    Integer minor;

    public VehicleInfo(VehicleAssociationType vehicleAssociationType, String vehicleId,
                       String bluetoothAddress, UUID uuid, Integer major,
                       Integer minor) {
        this.vehicleAssociationType = vehicleAssociationType;
        this.vehicleId = vehicleId;
        this.bluetoothAddress = bluetoothAddress;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public static VehicleInfo fromZendriveVehicleInfo(ZendriveVehicleInfo zendriveVehicleInfo) {
        return new VehicleInfo(VehicleAssociationType.BLUETOOTH_STEREO,
                zendriveVehicleInfo.getVehicleId(), zendriveVehicleInfo.getBluetoothAddress(),
                null, null, null);
    }

    public static VehicleInfo fromZendriveVehicleBeacon(ZendriveVehicleBeacon zendriveVehicleBeacon) {
        return new VehicleInfo(VehicleAssociationType.BEACON, zendriveVehicleBeacon.getVehicleId(),
                null, zendriveVehicleBeacon.getUuid(), zendriveVehicleBeacon.getMajor(),
                zendriveVehicleBeacon.getMinor());
    }

    enum VehicleAssociationType {
        BLUETOOTH_STEREO,
        BEACON
    }
}
