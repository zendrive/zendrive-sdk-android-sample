package com.zendrive.zendrivesdkdemo;

import android.util.Log;

import com.zendrive.sdk.DriveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Trips detected by the Zendrive SDK.
 */
class TripListDetails {

    TripListDetails() {
        this.tripList = new ArrayList<DriveInfo>();
    }

    void addTrip(DriveInfo driveInfo) {
        tripList.add(driveInfo);
    }

    void updateTrip(DriveInfo driveInfo) {
        int matchingInfo = -1;
        for (int i = tripList.size() - 1; i >= 0; --i) {
            if (tripList.get(i).driveId.equals(driveInfo.driveId)) {
                matchingInfo = i;
                break;
            }
        }
        if (matchingInfo == -1) {
            Log.e(Constants.LOG_TAG_DEBUG, "Updating trip which was not added earlier.");
            return;
        }
        tripList.set(matchingInfo, driveInfo);
    }

    List<DriveInfo> tripList;
}
