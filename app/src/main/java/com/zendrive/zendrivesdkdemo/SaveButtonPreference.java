package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

public class SaveButtonPreference extends Preference {

    public SaveButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        view.findViewById(R.id.save).setOnClickListener(v ->
                ((SettingsActivity) v.getContext()).save());
    }
}
