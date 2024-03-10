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
import android.service.notification.StatusBarNotification;
import android.util.Log;


public class NotifyService extends Service {

    private NotificationManager mNotification;

    public NotifyService() {
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification ntf = new Notification.Builder(this, NotifManager.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.web)
                    .setContentTitle("Hello")
                    .setContentText("22:00.0")
                    .build();
            mNotification.notify(NotifManager.NOTIFICATION_ID, ntf);
        }
        dispNotif(mNotification);
        return k;
    }

    private void openNotif() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        try {
            NotificationChannel notificationCh = new NotificationChannel(NotifManager.CHANNEL_ID,
                    NotifManager.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            mNotification = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
            mNotification.createNotificationChannel(notificationCh);
            dispNotif(mNotification);

            Notification ntf = new Notification.Builder(this, NotifManager.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.web)
                    .setContentTitle("Hello <3")
                    .setContentText("00:00.0")
                    .build();

            Log.i("app", "started");
            startForeground(NotifManager.NOTIFICATION_ID, ntf, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } catch(Exception e) {
            //
        }
    }

    private void dispNotif(NotificationManager ntMngr) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return;

        Log.i("notif", "---------");
        StatusBarNotification[] notifs = ntMngr.getActiveNotifications();
        for(StatusBarNotification n : notifs) {
            Log.i("notif", "id"+ n.getId());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}