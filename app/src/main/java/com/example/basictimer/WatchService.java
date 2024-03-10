package com.example.basictimer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class WatchService extends Service {

    private int activityState;
    private int notifyState;
    private Timer mTimer;
    private int mWatchState;
    private long mWatchTimestamp;

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
    }

    public void startWatch() {

        if(mWatchState == WatchStates.STATE_ACTIVE) return;

        mTimer = new Timer();

        if(mWatchState == WatchStates.STATE_PAUSED)
            mWatchTimestamp = System.currentTimeMillis() - mWatchTimestamp;
        else
            mWatchTimestamp = System.currentTimeMillis();
        mWatchState = WatchStates.STATE_ACTIVE;

        if(activityState == ServiceComm.ACTIVE) uiCallback.onStart();
        if(notifyState == ServiceComm.ACTIVE)   notifyCallback.onStart();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long dif = System.currentTimeMillis() - mWatchTimestamp;
                if(activityState == ServiceComm.ACTIVE)
                    uiCallback.onUpdate(dif);
                if(notifyState == ServiceComm.ACTIVE)
                    notifyCallback.onUpdate(dif);
            }
        }, 40, 40);
    }

    public void pauseWatch() {

        mWatchState = WatchStates.STATE_PAUSED;
        mTimer.cancel();
        mWatchTimestamp = System.currentTimeMillis() - mWatchTimestamp;

        if(activityState == ServiceComm.ACTIVE) uiCallback.onPause();
        if(notifyState == ServiceComm.ACTIVE)   notifyCallback.onPause();
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

    public void setUIState(int from, int st) {
        if(from == ServiceComm.FROM_NOTIF)
            notifyState = st;
        else
            activityState = st;
    }

    public class LocalBinder extends Binder {
        public WatchService getService() {
            return WatchService.this;
        }
        public void setCallback(int from, ServiceCallback clbk) {
            if(from == ServiceComm.FROM_NOTIF)
                notifyCallback = clbk;
            else
                uiCallback = clbk;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getIntExtra(ServiceComm.STARTED_FROM, -1) == ServiceComm.FROM_NOTIF)
            return NotifyBinder;

        return UIBinder;
    }
}