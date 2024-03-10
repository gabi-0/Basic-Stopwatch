package com.example.basictimer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private int mState;
    private long mTimestamp;
    private TextView mTimeString;

    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTimeString = findViewById(R.id.timerString);
        mTimeString.setText(timestampFormat(0));

        int theme = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(theme == Configuration.UI_MODE_NIGHT_YES) {
            int white = getResources().getColor(R.color.white);
            mTimeString.setTextColor(white);
        }

        Button bSwitch = (Button) findViewById(R.id.btnSwitch);
        Button bStop = (Button) findViewById(R.id.btnStop);
        bSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mState == TimerStates.STATE_ACTIVE) { // change to state paused
                    mState = TimerStates.STATE_PAUSED;
                    mTimer.cancel();
                    mTimestamp = System.currentTimeMillis() - mTimestamp;

                    mTimer.purge();
                    bSwitch.setText(R.string.btn_Play);
                    bStop.setVisibility(View.VISIBLE);
                } else {                                // play / resume
                    mTimer = new Timer();
                    bSwitch.setText(R.string.btn_Pause);
                    bStop.setVisibility(View.INVISIBLE);

                    if(mState == TimerStates.STATE_PAUSED)
                        mTimestamp = System.currentTimeMillis() - mTimestamp;
                    else
                        mTimestamp = System.currentTimeMillis();
                    mState = TimerStates.STATE_ACTIVE;

                    mTimer.schedule(getTimerTask(), 40, 40);
                }
            }
        });

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mState = TimerStates.STATE_NEW;
                mTimestamp = 0;
                bStop.setVisibility(View.INVISIBLE);
                mTimeString.setText(timestampFormat(0));
            }
        });

        Intent svIntent = new Intent(this, TimerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(svIntent);
        else
            startService(svIntent);
    }

    private String timestampFormat(long dif) {
        dif /= 100;
        long mil = dif%10;
        dif /= 10;
        return String.format(Locale.US, "%02d:%02d.%d", dif/60, dif%60, mil);
    }

    private TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                String fStr = timestampFormat(System.currentTimeMillis() - mTimestamp);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTimeString.setText(fStr);
                    }
                });
            }
        };
    }
}