package com.example.basictimer;

import static com.example.basictimer.FormatWatch.timestampAccurateFormat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private TextView mViewStopwatch;
    private Button mBtnSwitch;
    private Button mBtnStop;

    private WatchService mWatchSv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewStopwatch = findViewById(R.id.timerString);
        mViewStopwatch.setText(timestampAccurateFormat(0));

        int theme = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(theme == Configuration.UI_MODE_NIGHT_YES) {
            int white = getResources().getColor(R.color.white);
            mViewStopwatch.setTextColor(white);
        }

        createWatchService();

        mBtnSwitch = findViewById(R.id.btnSwitch);
        mBtnStop = findViewById(R.id.btnStop);
        mBtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // only from STATE_ACTIVE the watch can go to PAUSED
                if(mWatchSv.getWatchState() == WatchStates.STATE_ACTIVE)
                    mWatchSv.pauseWatch();
                else
                    mWatchSv.startWatch();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWatchSv.stopWatch();
            }
        });

//        Intent svIntent = new Intent(this, NotifyService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            startForegroundService(svIntent);
//        else
//            startService(svIntent);
    }

    private void createWatchService() {
        Intent watchInt = new Intent(this, WatchService.class);
        watchInt.putExtra(ServiceComm.STARTED_FROM, ServiceComm.FROM_UI);
        startService(watchInt);

        bindService(watchInt, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                WatchService.LocalBinder svBind = (WatchService.LocalBinder)service;
                svBind.setCallback(ServiceComm.FROM_UI, getSvCallback());
                mWatchSv = svBind.getService();
                mWatchSv.setUIState(ServiceComm.FROM_UI, ServiceComm.ACTIVE);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
//                createWatchService();
                Log.e("Service", "watch service was disconnected");
            }
        }, BIND_IMPORTANT);
    }

    private ServiceCallback getSvCallback() {
        return new ServiceCallback(){
            @Override
            public void onStart() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnSwitch.setText(R.string.btn_Pause);
                        mBtnStop.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onPause() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnSwitch.setText(R.string.btn_Play);
                        mBtnStop.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onStop() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnSwitch.setText(R.string.btn_Play);
                        mBtnStop.setVisibility(View.INVISIBLE);
                        mViewStopwatch.setText(timestampAccurateFormat(0));
                    }
                });
            }

            @Override
            public void onUpdate(long dif) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mViewStopwatch.setText(timestampAccurateFormat(dif));
                    }
                });
            }
        };
    }
}