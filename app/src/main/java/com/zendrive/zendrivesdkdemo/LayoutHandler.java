package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.view.View;

import com.zendrive.zendrivesdkdemo.databinding.ActivityMainBinding;

public interface LayoutHandler extends View.OnClickListener {
    void setup(Context context, ActivityMainBinding binding);
}
