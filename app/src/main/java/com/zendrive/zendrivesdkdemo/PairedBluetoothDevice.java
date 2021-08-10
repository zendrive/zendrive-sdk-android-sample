package com.zendrive.zendrivesdkdemo;

public class PairedBluetoothDevice {
    String name;
    String address;
    Boolean isChecked;

    PairedBluetoothDevice(String name, String address, Boolean isChecked) {
        this.name = name;
        this.address = address;
        this.isChecked = isChecked;
    }

    @Override
    public String toString() {
        return "Name: " + name +
                "\nAddress:" + address;
    }
}
