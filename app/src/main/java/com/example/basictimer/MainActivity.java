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

    private long mStateTime;
    private TextView mTimeString;

    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTimeString = findViewById(R.id.timerString);

        Button bSwitch = (Button) findViewById(R.id.btnSwitch);
        bSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStateTime == 0) {
                    bSwitch.setText(R.string.btn_Stop);
                    mTimer = new Timer();
                    mStateTime = System.currentTimeMillis();
                    mTimer.schedule(getTimerTask(), 100, 42);
                } else {
                    bSwitch.setText(R.string.btn_Stop);
                    mTimer.cancel();
                    mTimer.purge();
                    mStateTime = 0;
                }
            }
        });
    }

    private String timestampFormat(long dif) {
        long mil = dif%1000;
        dif /= 1000;
        return String.format(Locale.US, "%02d:%02d:%03d", dif/60, dif%60, mil);
    }

    private TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                String fStr = timestampFormat(System.currentTimeMillis() - mStateTime);
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