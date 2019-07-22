package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.zendrive.sdk.Zendrive;

public class SdkState extends BaseObservable {

    private final Context context;

    public SdkState(Context context) {
        this.context = context.getApplicationContext();
    }

    @Bindable
    public boolean isSetup() {
        return Zendrive.isSDKSetup(context);
    }

    public void update() {
        notifyPropertyChanged(BR.setup);
        notifyPropertyChanged(BR.driving);
    }

    @Bindable
    public boolean isDriving() {
        return Zendrive.getActiveDriveInfo(context) != null;
    }
}
