package com.zendrive.zendrivesdkdemo;

import com.zendrive.sdk.DriveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * This class maintains all the trip information.
 *
 * Created by girishkadli on 12/24/15.
 */
public class TripListDetails {

    public TripListDetails() {
        this.tripList = new ArrayList<DriveInfo>();
    }

    public void addTrip(DriveInfo driveInfo) {
        tripList.add(driveInfo);
    }

    public List<DriveInfo> tripList;
}
