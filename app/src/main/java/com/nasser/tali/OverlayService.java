package com.nasser.tali;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm;
    private View view;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (view != null) return START_NOT_STICKY;
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.parseColor("#F2FFFFFF"));
        box.setPadding(40, 40, 40, 40);

        TextView t = new TextView(this);
        t.setText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ");
        t.setTextSize(24);
        t.setTextColor(Color.BLACK);
        t.setGravity(Gravity.CENTER);

        Button done = new Button(this);
        done.setText("تم");
        done.setOnClickListener(v -> stopSelf());

        Button later = new Button(this);
        later.setText("تأجيل");
        later.setOnClickListener(v -> stopSelf());

        box.addView(t);
        box.addView(done);
        box.addView(later);

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP;
        p.y = 200;

        view = box;
        wm.addView(view, p);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (view != null) {
            wm.removeView(view);
            view = null;
        }
        super.onDestroy();
    }
}
