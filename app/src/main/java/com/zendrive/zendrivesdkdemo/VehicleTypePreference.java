package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.zendrive.sdk.ZendriveVehicleType;

import java.util.Locale;

class VehicleTypePreference extends ListPreference {
    public VehicleTypePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        int size = ZendriveVehicleType.values().length + 1;
        String[] values = new String[size];
        String[] entries = new String[size];
        int index = 0;
        for (ZendriveVehicleType entryValue : ZendriveVehicleType.values()) {
            values[index] = entryValue.name();
            String entry = values[index].toLowerCase(Locale.US);
            entries[index] = entry.substring(0, 1).toUpperCase() + entry.substring(1);
            index += 1;
        }
        entries[index] = Constants.NONE_VEHICLE_TYPE_OPTION_VALUE;
        values[index] = Constants.NONE_VEHICLE_TYPE_OPTION_VALUE;
        setEntryValues(values);
        setEntries(entries);
        setDefaultValue(Constants.NONE_VEHICLE_TYPE_OPTION_VALUE);
    }
}
