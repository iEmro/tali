package com.nasser.tali;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private static final int GOLD = 0xFFC9A55C;
    private static final int IVORY = 0xFFF5EFE0;
    private static final int DARK = 0xFF0B1210;

    private WindowManager wm;
    private View view;

    @Override
    public IBinder onBind(Intent i) { return null; }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private View makeButton(int type, boolean filled, String desc) {
        IconButton b = new IconButton(this, type, filled);
        b.setContentDescription(desc);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(56), dp(56));
        p.setMargins(dp(10), 0, dp(10), 0);
        b.setLayoutParams(p);
        b.setOnClickListener(v -> stopSelf());
        return b;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (view != null) return START_NOT_STICKY;
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        FrameLayout container = new FrameLayout(this);
        container.setPadding(dp(16), dp(8), dp(16), dp(16));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(24), dp(22), dp(24), dp(22));
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xF5142019, 0xF50B1210});
        bg.setCornerRadius(dp(28));
        bg.setStroke(dp(1), 0x99C9A55C);
        card.setBackground(bg);
        card.setElevation(dp(10));

        TextView head = new TextView(this);
        head.setText("الفاتحة  ·  ١");
        head.setTextSize(13);
        head.setTextColor(GOLD);
        head.setLetterSpacing(0.08f);

        View line = new View(this);
        line.setBackgroundColor(0x66C9A55C);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(1));
        lp.setMargins(0, dp(12), 0, dp(14));

        TextView ayah = new TextView(this);
        ayah.setText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ");
        ayah.setTextSize(26);
        ayah.setTextColor(IVORY);
        ayah.setGravity(Gravity.CENTER);
        ayah.setTypeface(Typeface.SERIF);
        ayah.setLineSpacing(0f, 1.4f);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(22);

        row.addView(makeButton(IconButton.CLOSE, false, "إغلاق"));
        row.addView(makeButton(IconButton.CLOCK, false, "تأجيل"));
        row.addView(makeButton(IconButton.CHECK, true, "تم"));

        card.addView(head);
        card.addView(line, lp);
        card.addView(ayah);
        card.addView(row, rowLp);
        container.addView(card);

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP;
        p.y = dp(60);

        view = container;
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

    private static class IconButton extends View {
        static final int CHECK = 0, CLOCK = 1, CLOSE = 2;
        private final int type;
        private final boolean filled;
        private final float d;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        IconButton(Context c, int type, boolean filled) {
            super(c);
            this.type = type;
            this.filled = filled;
            this.d = c.getResources().getDisplayMetrics().density;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float r = Math.min(cx, cy) - 2 * d;

            if (filled) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(GOLD);
                canvas.drawCircle(cx, cy, r, paint);
            } else {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.5f * d);
                paint.setColor(0x99C9A55C);
                canvas.drawCircle(cx, cy, r, paint);
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.4f * d);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(filled ? DARK : (type == CLOSE ? 0xFFB8B2A3 : GOLD));
            float s = r * 0.42f;

            if (type == CHECK) {
                canvas.drawLine(cx - s, cy, cx - s * 0.3f, cy + s * 0.7f, paint);
                canvas.drawLine(cx - s * 0.3f, cy + s * 0.7f, cx + s, cy - s * 0.7f, paint);
            } else if (type == CLOCK) {
                canvas.drawCircle(cx, cy, s * 1.2f, paint);
                canvas.drawLine(cx, cy, cx, cy - s * 0.7f, paint);
                canvas.drawLine(cx, cy, cx + s * 0.55f, cy + s * 0.3f, paint);
            } else {
                canvas.drawLine(cx - s, cy - s, cx + s, cy + s, paint);
                canvas.drawLine(cx + s, cy - s, cx - s, cy + s, paint);
            }
        }
    }
}
