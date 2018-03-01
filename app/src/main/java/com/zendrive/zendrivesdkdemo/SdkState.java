package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveState;

public class SdkState extends BaseObservable {

    private final Context context;

    public SdkState(Context context) {
        this.context = context.getApplicationContext();
    }

    @Bindable
    public boolean isSetup() {
        return Zendrive.getZendriveState(context) != null;
    }

    public void update() {
        notifyPropertyChanged(BR.setup);
        notifyPropertyChanged(BR.driving);
    }

    @Bindable
    public boolean isDriving() {
        ZendriveState state = Zendrive.getZendriveState(context);
        return state != null && state.isDriveInProgress;
    }
}
