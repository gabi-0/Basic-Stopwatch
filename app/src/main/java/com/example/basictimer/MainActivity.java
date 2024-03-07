package com.example.basictimer;

import androidx.appcompat.app.AppCompatActivity;

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

        Button bSwitch = (Button) findViewById(R.id.btnSwitch);
        bSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mState == TimerStates.STATE_ACTIVE) { // change to state paused
                    mState = TimerStates.STATE_PAUSED;
                    mTimer.cancel();
                    mTimestamp = System.currentTimeMillis() - mTimestamp;

                    mTimer.purge();
                    bSwitch.setText(R.string.btn_Play);
                } else {
                    mTimer = new Timer();
                    bSwitch.setText(R.string.btn_Pause);

                    if(mState == TimerStates.STATE_PAUSED)
                        mTimestamp = System.currentTimeMillis() - mTimestamp;
                    else
                        mTimestamp = System.currentTimeMillis();
                    mState = TimerStates.STATE_ACTIVE;

                    mTimer.schedule(getTimerTask(), 100, 42);
                }
            }
        });
    }

    private String timestampFormat(long dif) {
        long mil = dif%1000;
        dif /= 1000;
        return String.format(Locale.US, "%02d:%02d.%03d", dif/60, dif%60, mil);
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