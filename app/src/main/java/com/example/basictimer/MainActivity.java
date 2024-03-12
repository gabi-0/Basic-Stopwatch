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

    private WatchService mWatchSv = null;
    private ServiceConnection mSvCon = null;
    private Intent mWatchIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadIntent();
        startService(mWatchIntent);
        bindWatchSv();

        mViewStopwatch = findViewById(R.id.timerString);
        mViewStopwatch.setText(timestampAccurateFormat(0));
        mBtnSwitch = findViewById(R.id.btnSwitch);
        mBtnStop = findViewById(R.id.btnStop);
    }

    private void loadIntent() {
        if(mWatchIntent != null) return;

        mWatchIntent = new Intent(this, WatchService.class);
        mWatchIntent.putExtra(ServiceComm.STARTED_FROM, ServiceComm.FROM_UI);
    }

    private void bindWatchSv() {
        if(mSvCon == null) {
            mSvCon = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    WatchService.LocalBinder svBind = (WatchService.LocalBinder) service;
                    mWatchSv = svBind.getService();
                    svBind.setCallback(ServiceComm.FROM_UI, getSvCallback());
                    mWatchSv.setUIState(ServiceComm.FROM_UI, ServiceComm.ACTIVE);
                    mViewStopwatch.setText(timestampAccurateFormat(mWatchSv.getWatchDif()));
                    loadBtnEvents();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
//                createWatchService();
                    Log.e("Main", "watch service was disconnected");
                }
            };
        }
        bindService(mWatchIntent, mSvCon, BIND_IMPORTANT);
    }

    private ServiceCallback getSvCallback() {
        return new ServiceCallback(){
            @Override
            public void onStart(long dif) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnSwitch.setText(R.string.btn_Pause);
                        mBtnStop.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onPause(long dif) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnSwitch.setText(R.string.btn_Play);
                        if(mWatchSv.getWatchTimestamp() == 0)       // TODO: figure why is this IF here
                            mBtnStop.setVisibility(View.INVISIBLE);
                        else
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

    private void loadBtnEvents() {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWatchSv.setUIState(ServiceComm.FROM_UI, ServiceComm.INACTIVE);

        Intent svIntent = new Intent(this, NotifyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(svIntent);
        unbindService(mSvCon);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int theme = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(theme == Configuration.UI_MODE_NIGHT_YES) {
            int white = getResources().getColor(R.color.white);
            mViewStopwatch.setTextColor(white);
        }

        loadIntent();
        bindWatchSv();

        if(mWatchSv != null) {
            mWatchSv.setUIState(ServiceComm.FROM_UI, ServiceComm.ACTIVE);
            mViewStopwatch.setText(timestampAccurateFormat(mWatchSv.getWatchDif()));
        }
    }

/*    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        ActivityStateSaver activSt = new ActivityStateSaver();
        activSt.serviceCon = mSvCon;
        outState.putSerializable("e", activSt);
        Log.i("main_save", "saveinstance");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSvCon = (ServiceConnection) savedInstanceState.getSerializable("e");
        Log.i("main_save", "restoring-------");
    }*/
}