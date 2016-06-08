package com.zendrive.zendrivesdkdemo;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveState;

/**
 * Created by deepanshu on 6/8/16.
 */
public class SdkState extends BaseObservable {

    @Bindable
    public boolean isSetup() {
        return Zendrive.getZendriveState() != null;
    }

    public void update() {
        notifyPropertyChanged(BR.setup);
        notifyPropertyChanged(BR.driving);
    }

    @Bindable
    public boolean isDriving() {
        ZendriveState state = Zendrive.getZendriveState();
        return state != null && state.isDriveInProgress;
    }
}
