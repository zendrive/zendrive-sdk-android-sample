package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zendrive.sdk.Zendrive;
import com.zendrive.zendrivesdkdemo.databinding.ActivityLoginBinding;

/**
 * Login UI
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setClickHandler(this);
    }

    public void onClick(View v) {
        // Display DashboardFragment
        String driverId = binding.editText.getText().toString();
        if (!Zendrive.isValidInputParameter(driverId)) {
            Toast.makeText(this, "Enter valid driver id", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferenceManager.setDriverId(LoginActivity.this, driverId);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
