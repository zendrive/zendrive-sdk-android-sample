package com.zendrive.zendrivesdkdemo;

import com.zendrive.sdk.ZendriveVehicleBeacon;

import java.util.UUID;

public class VehicleInfo {

    String vehicleId;
    String bluetoothAddress;
    UUID uuid;
    Integer major;
    Integer minor;

    public VehicleInfo(String vehicleId, String bluetoothAddress, UUID uuid, Integer major,
                       Integer minor) {
        this.vehicleId = vehicleId;
        this.bluetoothAddress = bluetoothAddress;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public static VehicleInfo fromZendriveVehicleBeacon(ZendriveVehicleBeacon zendriveVehicleBeacon) {
        return new VehicleInfo(zendriveVehicleBeacon.getVehicleId(), null,
                zendriveVehicleBeacon.getUuid(), zendriveVehicleBeacon.getMajor(),
                zendriveVehicleBeacon.getMinor());
    }
}
