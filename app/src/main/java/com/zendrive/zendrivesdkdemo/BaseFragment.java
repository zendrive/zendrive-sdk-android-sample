package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
    private ProgressDialog loadingDialog;
    private AlertDialog errorAlert;

    void showLoadingIndicator() {
        if (loadingDialog == null || !loadingDialog.isShowing()) {
            Context context = getContext();
            if (context == null) {
                return;
            }
            loadingDialog = new ProgressDialog(context);
            loadingDialog.setCancelable(false);
            loadingDialog.setMessage("Loading..");
            loadingDialog.show();
        }
    }

    void hideLoadingIndicator() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    AlertDialog showError(String error,
                          String positiveButtonTitle,
                          DialogInterface.OnClickListener positiveButtonListener,
                          String negativeButtonTitle,
                          DialogInterface.OnClickListener negativeButtonListener) {
        hideErrors();
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        errorAlert =
                new AlertDialog.Builder(activity)
                        .setTitle("Oops!!")
                        .setMessage(error)
                        .setPositiveButton(positiveButtonTitle, positiveButtonListener)
                        .setNegativeButton(negativeButtonTitle, negativeButtonListener)
                        .setCancelable(false)
                        .create();
        errorAlert.show();
        return errorAlert;
    }

    void hideErrors() {
        if (errorAlert != null && errorAlert.isShowing()) {
            errorAlert.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        hideLoadingIndicator();
        hideErrors();
        super.onDestroyView();
    }
}
