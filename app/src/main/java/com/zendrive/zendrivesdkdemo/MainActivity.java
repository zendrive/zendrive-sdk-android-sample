package com.zendrive.zendrivesdkdemo;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();
    }

    protected void setOrUnsetWakeupAlarm(boolean setAlarm) {
        WakeupAlarmManager alarmManager = new WakeupAlarmManager(this.getApplicationContext());
        if(setAlarm){
            alarmManager.setAlarm();
        } else {
            alarmManager.unsetAlarm();
        }
    }


    private void initializeView() {
        // Check if logging details are available
        if (isUserLoggedIn()) {
            // Load DashboardFragment if logged in
            loadMainScreen();
        }
        else {
            loadLoginScreen();
        }
    }

    private boolean isUserLoggedIn(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String savedDriverId = sharedPreferences.getString(Constants.DRIVER_ID_KEY, null);
        return savedDriverId!=null && !savedDriverId.equalsIgnoreCase("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    public void loadMainScreen() {
        // load main fragment
        MainFragment mainFragment = new MainFragment();
        displayFragment(mainFragment);
    }

    public void loadLoginScreen() {
        // load login fragment
        LoginFragment loginFragment = new LoginFragment();
        displayFragment(loginFragment);
    }

    public void loadSettingScreen(){
        // load setting fragment
        SettingFragment settingFragment = new SettingFragment();
        displayFragment(settingFragment);
    }

    public void displayFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.mainFrameLayout, fragment);
        removeAllFragments();
        ft.commitAllowingStateLoss();
    }

    private void removeAllFragments() {
        List<Fragment> al = getSupportFragmentManager().getFragments();
        if (al == null) {
            // code that handles no existing fragments
            return;
        }

        for (Fragment frag : al)
        {
            getSupportFragmentManager().beginTransaction().remove(frag).commitAllowingStateLoss();
        }
    }

    @Override
    public void onBackPressed() {
        if (isUserLoggedIn()) {
            // Load DashboardFragment if logged in
            loadMainScreen();
        }
        else {
            loadLoginScreen();
        }
    }
}