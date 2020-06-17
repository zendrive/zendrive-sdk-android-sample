package com.zendrive.zendrivesdkdemo;

import com.zendrive.sdk.DriveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Trips detected by the Zendrive SDK.
 */
class TripListDetails {

    TripListDetails() {
        this.tripList = new ArrayList<>();
    }

    void addOrUpdateTrip(DriveInfo driveInfo) {
        int matchingInfo = -1;
        for (int i = tripList.size() - 1; i >= 0; --i) {
            if (tripList.get(i).driveId.equals(driveInfo.driveId)) {
                matchingInfo = i;
                break;
            }
        }
        if (matchingInfo == -1) {
            tripList.add(driveInfo);
        } else {
            tripList.set(matchingInfo, driveInfo);
        }
    }

    final List<DriveInfo> tripList;
}
