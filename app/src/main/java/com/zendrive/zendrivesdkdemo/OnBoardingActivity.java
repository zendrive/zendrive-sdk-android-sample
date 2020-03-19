package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.zendrive.zendrivesdkdemo.databinding.ActivityOnBoardingBinding;

import java.util.Locale;

public class OnBoardingActivity extends Activity implements View.OnClickListener {

    private ActivityOnBoardingBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_boarding);
        binding.setClickHandler(this);
        populateOnBoardingText();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.loginButton) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (v == binding.settingsButton) {
            OnBoardingHelper.getAutoStartPermission(this);
        }
    }

    private void populateOnBoardingText () {
        String phoneBrand = Build.BRAND.toLowerCase(Locale.ROOT);
        if (phoneBrand.equals(OnBoardingHelper.BRAND_HUAWEI)) {
            binding.settingsText.setText(getResources().getString
                    (R.string.bulleted_onboarding_list_huawei));
        } else if (phoneBrand.equals(OnBoardingHelper.BRAND_XIAOMI) ||
                phoneBrand.equals(OnBoardingHelper.BRAND_XIAOMI_REDMI)) {
            binding.settingsText.setText(getResources().getString
                    (R.string.bulleted_onboarding_list_xiaomi));
        }
    }
}
