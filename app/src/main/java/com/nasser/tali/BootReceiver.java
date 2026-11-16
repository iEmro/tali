package com.nasser.tali;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) return;
        boolean enabled = c.getSharedPreferences("tali_prefs", Context.MODE_PRIVATE)
            .getBoolean("enabled", false);
        if (!enabled) return;
        try {
            c.startForegroundService(new Intent(c, WatchService.class));
        } catch (Exception e) {
            // ignore
        }
    }
}
