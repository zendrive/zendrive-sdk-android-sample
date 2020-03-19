package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zendrive.sdk.Zendrive;
import com.zendrive.zendrivesdkdemo.databinding.ActivityLoginBinding;

/**
 * Login UI
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final int DRAW_PERMISSION_REQUEST_CODE = 1;

    private AppOpsManager.OnOpChangedListener onOpChangedListener = null;
    private boolean hasOverlayPermission;
    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setClickHandler(this);
        hasOverlayPermission = !overlayPermissionNeeded() || Settings.canDrawOverlays(this);
        updateOverlayPermissionButtonVisibility();
    }

    public void onClick(View v) {
        if (v == binding.loginButton) {
            // Display DashboardFragment
            String driverId = binding.editText.getText().toString();
            if (!Zendrive.isValidInputParameter(driverId)) {
                Toast.makeText(this, "Enter valid driver id", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferenceManager.setDriverId(LoginActivity.this, driverId);
            if (OnBoardingHelper.isOnBoardingNeeded()) {
                startActivity(new Intent(this, OnBoardingActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        } else if (v == binding.overlayPermissionButton) {
            if (overlayPermissionNeeded()) {
                onOpChangedListener = new AppOpsManager.OnOpChangedListener() {
                    @Override
                    public void onOpChanged(String op, String packageName) {
                        if (AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW.equals(op) &&
                                getPackageName().equals(packageName)) {
                            hasOverlayPermission = !hasOverlayPermission;
                        }
                    }
                };
                AppOpsManager appOpsManager = getSystemService(AppOpsManager.class);
                // Do not specify the package name here. That will result in duplicate calls to
                // onOpChanged - one for when package name matches and one for when mode matches
                appOpsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                        null, onOpChangedListener);
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, DRAW_PERMISSION_REQUEST_CODE);
            } else {
                Log.d(TAG, "overlayPermission button clicked when it should have been invisible");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_PERMISSION_REQUEST_CODE) {
            if (overlayPermissionNeeded()) {
                // Cannot use Settings.canDrawOverlays() here directly because of
                // https://issuetracker.google.com/issues/66072795
                // Hence we must use an onOpChangedListener and monitor changes ourselves
                AppOpsManager appOpsManager = getSystemService(AppOpsManager.class);
                appOpsManager.stopWatchingMode(onOpChangedListener);
                if (!hasOverlayPermission) {
                    Toast.makeText(this,
                            "Need draw permission to generate phone screen interaction events",
                            Toast.LENGTH_SHORT).show();
                }
            }
            updateOverlayPermissionButtonVisibility();
        }
    }

    private void updateOverlayPermissionButtonVisibility() {
        if (hasOverlayPermission) {
            binding.overlayPermissionButton.setVisibility(View.GONE);
        } else {
            binding.overlayPermissionButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean overlayPermissionNeeded() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
