package com.zendrive.zendrivesdkdemo;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.ZendriveEventType;
import com.zendrive.sdk.feedback.ZendriveFeedback;
import com.zendrive.zendrivesdkdemo.databinding.ActivityCollisionBinding;

public class CollisionDetectedActivity extends Activity implements View.OnClickListener {

    private ActivityCollisionBinding collisionBinding;
    private AccidentInfo accidentInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        collisionBinding = DataBindingUtil.setContentView(this, R.layout.activity_collision);
        accidentInfo = getIntent().getParcelableExtra(Constants.ACCIDENT_INFO);
    }

    @Override
    public void onClick(View view) {
        if (view == collisionBinding.collisionHelpButton) {
            ZendriveFeedback.addEventOccurrence(this, accidentInfo.driveId,
                    accidentInfo.timestampMillis, ZendriveEventType.COLLISION, true);
        } else if (view == collisionBinding.fakeCollisionButton) {
            ZendriveFeedback.addEventOccurrence(this, accidentInfo.driveId,
                    accidentInfo.timestampMillis, ZendriveEventType.COLLISION, false);
        }
        NotificationUtility.removeCollisionNotification(this);
        Intent intent = new Intent(this, MainActivity.class);
        // skip launching MainActivity, if it is already present
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
