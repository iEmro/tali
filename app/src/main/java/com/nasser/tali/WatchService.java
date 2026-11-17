package com.nasser.tali;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

public class WatchService extends Service {
    private static final String CHANNEL = "tali_watch";
    private BroadcastReceiver receiver;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(
            CHANNEL, "تالي", NotificationManager.IMPORTANCE_MIN));
        Notification n = new Notification.Builder(this, CHANNEL)
            .setContentTitle("تالي يعمل")
            .setContentText("ستظهر لك آية عند كل فتح للجوال")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, n);
        }

        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent i) {
                    Toast.makeText(c, "تالي: وصل حدث " + i.getAction(),
                        Toast.LENGTH_SHORT).show();
                    try {
                        c.startService(new Intent(c, OverlayService.class));
                    } catch (Exception e) {
                        Toast.makeText(c, "خطأ: " + e, Toast.LENGTH_LONG).show();
                    }
                }
            };
            IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_USER_PRESENT);
            f.addAction(Intent.ACTION_SCREEN_ON);
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(receiver, f, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(receiver, f);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
