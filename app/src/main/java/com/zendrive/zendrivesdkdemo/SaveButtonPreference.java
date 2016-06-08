package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by deepanshu on 6/8/16.
 */
public class SaveButtonPreference extends Preference {

    public SaveButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SettingsActivity) v.getContext()).save();
            }
        });
    }
}
