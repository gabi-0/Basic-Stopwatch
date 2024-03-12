package com.example.basictimer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.Serializable;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class WatchService extends Service {

    private int activityState;
    private int notifyState;
    private Timer mTimer;
    private int mWatchState;
    private long mWatchTimestamp;
    private long mLastNotify;

    private final IBinder UIBinder = new LocalBinder();
    private ServiceCallback uiCallback;
    private final IBinder NotifyBinder = new LocalBinder();
    private ServiceCallback notifyCallback;

    public WatchService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWatchState = WatchStates.STATE_NEW;
        mLastNotify = 0;
        Log.e("w++++service", "create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("w++service", "sv started: "+ startId );
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("w++++service", "destroy:::::::");
    }

    public void startWatch() {

        if(mWatchState == WatchStates.STATE_ACTIVE) return;

        mTimer = new Timer();

        if(mWatchState == WatchStates.STATE_PAUSED)
            mWatchTimestamp = System.currentTimeMillis() - mWatchTimestamp;
        else
            mWatchTimestamp = System.currentTimeMillis();
        mWatchState = WatchStates.STATE_ACTIVE;

        if(activityState == ServiceComm.ACTIVE) uiCallback.onStart(getDif());
        if(notifyState == ServiceComm.ACTIVE)   notifyCallback.onStart(getDif());

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long dif = System.currentTimeMillis() - mWatchTimestamp;
                if(activityState == ServiceComm.ACTIVE)
                    uiCallback.onUpdate(dif);
                if(notifyState == ServiceComm.ACTIVE) {
                    if((dif/1000) > mLastNotify) {
                        mLastNotify = dif/1000;
                        notifyCallback.onUpdate(dif);
                    }
                }
            }
        }, 40, 40);
    }

    public void pauseWatch() {

        mWatchState = WatchStates.STATE_PAUSED;
        mTimer.cancel();
        mWatchTimestamp = System.currentTimeMillis() - mWatchTimestamp;

        if(activityState == ServiceComm.ACTIVE) uiCallback.onPause(mWatchTimestamp);
        if(notifyState == ServiceComm.ACTIVE)   notifyCallback.onPause(mWatchTimestamp);
        mTimer.purge();
    }

    public void stopWatch() {
        mWatchState = WatchStates.STATE_NEW;
        mTimer.cancel();
        mWatchTimestamp = 0;

        if(activityState == ServiceComm.ACTIVE) uiCallback.onStop();
        if(notifyState == ServiceComm.ACTIVE)   notifyCallback.onStop();
        mTimer.purge();
    }

    public int getWatchState() {
        return mWatchState;
    }

    public long getWatchTimestamp() {
        return mWatchTimestamp;
    }

    public long getDif() {
        return System.currentTimeMillis() - mWatchTimestamp;
    }

    public long getWatchDif() {
        if(mWatchState == WatchStates.STATE_ACTIVE) return getDif();
        return mWatchTimestamp;
    }

    public void setUIState(int from, int st) {
        if(from == ServiceComm.FROM_NOTIF) {
            notifyState = st;
            mLastNotify = 0;
        } else
            activityState = st;
    }

    public class LocalBinder extends Binder {
        public WatchService getService() {
            return WatchService.this;
        }
        public void setCallback(int from, ServiceCallback clbk) {
            if(from == ServiceComm.FROM_NOTIF) {
                notifyCallback = clbk;
                if(mWatchState == WatchStates.STATE_ACTIVE) notifyCallback.onStart(getDif());
                else notifyCallback.onPause(mWatchTimestamp);
            }  else {
                uiCallback = clbk;
                if(mWatchState == WatchStates.STATE_ACTIVE) uiCallback.onStart(getDif());
                else uiCallback.onPause(mWatchTimestamp);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getIntExtra(ServiceComm.STARTED_FROM, -1) == ServiceComm.FROM_NOTIF)
            return NotifyBinder;

        return UIBinder;
    }
}