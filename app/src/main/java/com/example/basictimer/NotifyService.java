package com.example.basictimer;

import static com.example.basictimer.FormatWatch.timestampFormat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;


public class NotifyService extends Service {

    private ServiceConnection mServCon;
    private WatchService mService;

    private NotificationManager mNotifyMgr;
    private NotificationCompat.Builder mNotifyBuild;

    public NotifyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mServCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                WatchService.LocalBinder svBind = (WatchService.LocalBinder)service;
                mService = svBind.getService();
                svBind.setCallback(ServiceComm.FROM_NOTIF, getNotifyCallback());
                mService.setUIState(ServiceComm.FROM_NOTIF, ServiceComm.ACTIVE);
                ServiceConnector.set(mService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
//                createWatchService();
                Log.e("Foreground", "watch service was disconnected");
            }
        };

        Intent svIntent = new Intent(this, WatchService.class);
        svIntent.putExtra(ServiceComm.STARTED_FROM, ServiceComm.FROM_NOTIF);
        bindService(svIntent, mServCon, BIND_IMPORTANT);

        loadChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            updateNotifyBuilder();
            startForeground(NotifyManager.NOTIFICATION_ID, mNotifyBuild.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mService.setUIState(ServiceComm.FROM_NOTIF, ServiceComm.DISABLED);
        if(mServCon != null)
            unbindService(mServCon);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void loadChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        NotificationChannel notificationCh = new NotificationChannel(NotifyManager.CHANNEL_ID,
                NotifyManager.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        mNotifyMgr = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotifyMgr.createNotificationChannel(notificationCh);
    }

    private void updateNotifyBuilder() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        Intent tapInt = new Intent(this, MainActivity.class);
        tapInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent actionIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                tapInt, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        mNotifyBuild = new NotificationCompat.Builder(getApplicationContext(), NotifyManager.CHANNEL_ID)
                .setSmallIcon(R.mipmap.web)
                .setContentTitle(getResources().getString(R.string.notif_paused))
                .setContentText(timestampFormat(0))
                .setContentIntent(actionIntent);
    }

    private void addNotifyAction(int icon, String title, String action, int reqCode) {
        Intent ntfIntent = new Intent(getApplicationContext(), ForegroundReceiver.class);
        ntfIntent.setAction(action);
        PendingIntent ntfPend = PendingIntent.getBroadcast(getApplicationContext(), reqCode,
                ntfIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyBuild.addAction(icon, title, ntfPend);
    }

    private void notifySetPauseAction() {
        mNotifyBuild.setContentTitle(getResources().getString(R.string.notif_running));
        addNotifyAction(R.drawable.pause24px, "Pause", NotifyManager.ACTION_PAUSE, NotifyManager.REQ_PAUSE);
    }

    private void notifySetStartAction() {
        mNotifyBuild.setContentTitle(getResources().getString(R.string.notif_paused));
        addNotifyAction(R.drawable.play24px, "Play", NotifyManager.ACTION_PLAY, NotifyManager.REQ_PLAY);
        addNotifyAction(R.drawable.stop24px, "Stop", NotifyManager.ACTION_STOP, NotifyManager.REQ_STOP);
    }

    private ServiceCallback getNotifyCallback() {
        return new ServiceCallback() {
            @Override
            public void onStart(long dif) {
                updateNotifyBuilder();
                notifySetPauseAction();
                mNotifyBuild.setContentText(timestampFormat(dif));
                mNotifyMgr.notify(NotifyManager.NOTIFICATION_ID, mNotifyBuild.build());
            }

            @Override
            public void onPause(long dif) {
                updateNotifyBuilder();
                notifySetStartAction();
                mNotifyBuild.setContentText(timestampFormat(dif));
                mNotifyMgr.notify(NotifyManager.NOTIFICATION_ID, mNotifyBuild.build());
            }

            @Override
            public void onStop() {
                stopForeground(true);
                stopSelf();
            }

            @Override
            public void onUpdate(long dif) {
                mNotifyBuild.setContentText(timestampFormat(dif));
                mNotifyMgr.notify(NotifyManager.NOTIFICATION_ID, mNotifyBuild.build());
            }
        };
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
        return null;
    }
}