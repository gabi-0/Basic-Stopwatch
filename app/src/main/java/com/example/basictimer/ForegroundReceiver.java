package com.example.basictimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ForegroundReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        WatchService wService = ServiceConnector.getInstance();

        String act = intent.getAction();
        if(act == null) return;

        switch(act) {
            case NotifyManager.ACTION_PLAY:
                wService.startWatch();
                break;
            case NotifyManager.ACTION_PAUSE:
                wService.pauseWatch();
                break;
            case NotifyManager.ACTION_STOP:
                wService.stopWatch();
                break;
        }
    }
}