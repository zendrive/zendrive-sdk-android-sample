package com.zendrive.zendrivesdkdemo;

import java.util.UUID;

public class ScannedBeacon {
    UUID uuid;
    Integer major;
    Integer minor;

    ScannedBeacon(UUID uuid, int major, int minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    @Override
    public String toString() {
        return "UUID: " + uuid +
                "\nMajor=" + major +
                "\nMinor=" + minor;
    }
}
