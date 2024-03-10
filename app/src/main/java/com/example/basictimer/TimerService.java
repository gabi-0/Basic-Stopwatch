package com.example.basictimer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


public class TimerService extends Service {
    public TimerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("service", "onCreate");
        openNotif();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int k = super.onStartCommand(intent, flags, startId);
        Log.i("service", "startCmd");
        return k;
    }

    private void openNotif() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        try {
            NotificationChannel notificationCh = new NotificationChannel(NotifManager.CHANNEL_ID,
                    NotifManager.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationCh);

            Notification ntf = new Notification.Builder(this, NotifManager.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.web)
                    .setContentTitle("Hello <3")
                    .setContentText("00:00.0")
                    .build();
            Log.i("app", "started");
            startForeground(NotifManager.FOREGROUND_ID, ntf, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } catch(Exception e) {
            //
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}