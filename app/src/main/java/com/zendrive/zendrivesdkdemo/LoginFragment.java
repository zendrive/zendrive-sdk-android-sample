package com.zendrive.zendrivesdkdemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Login UI
 */
public class LoginFragment extends BaseFragment {

    private View loginButton;
    private TextView loginTextView;

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        loginButton = view.findViewById(R.id.loginButton);
        loginTextView = (TextView) view.findViewById(R.id.editText);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    // Display DashboardFragment
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingIndicator();
                            String driverId = loginTextView.getText().toString();
                            SharedPreferenceManager.setDriverId(getActivity().getApplicationContext(),
                                    driverId);
                            activity.setOrUnsetWakeupAlarm(true);
                            activity.loadMainScreen();
                        }
                    });
                }

            }
        });
        return view;
    }


    @Override
    public void onDestroyView() {
        loginButton = null;
        loginTextView = null;
        super.onDestroyView();
    }
}
