package com.nasser.tali;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int SOFT = 0xFFE9F7F9;
    private static final int DEEP = 0xFF0A2230;
    private static final int EDGE = 0x66FFFFFF;

    private Dialog dialog;

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
        if (dialog != null) return START_NOT_STICKY;

        SharedPreferences sp = getSharedPreferences("tali_prefs", MODE_PRIVATE);
        int transparency = sp.getInt("transparency", 40);
        int position = sp.getInt("position", 15);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        boolean blur = Build.VERSION.SDK_INT >= 31 && wm.isCrossWindowBlurEnabled();

        int alpha = Math.round(255 * (100 - transparency) / 100f);
        if (!blur) alpha = Math.max(alpha, 0xE6);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(24), dp(22), dp(24), dp(22));

        int top = (alpha << 24) | 0x175066;
        int bottom = (alpha << 24) | 0x0A2230;
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, new int[]{top, bottom});
        bg.setCornerRadius(dp(28));
        bg.setStroke(dp(1), EDGE);

        TextView head = new TextView(this);
        head.setText("الفاتحة  ·  ١");
        head.setTextSize(13);
        head.setTextColor(0xE6FFFFFF);
        head.setLetterSpacing(0.06f);
        head.setPadding(dp(16), dp(5), dp(16), dp(5));
        GradientDrawable pill = new GradientDrawable();
        pill.setCornerRadius(dp(20));
        pill.setStroke(dp(1), EDGE);
        pill.setColor(0x1AFFFFFF);
        head.setBackground(pill);

        TextView ayah = new TextView(this);
        ayah.setText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ");
        ayah.setTextSize(27);
        ayah.setTextColor(WHITE);
        ayah.setGravity(Gravity.CENTER);
        ayah.setTypeface(Typeface.SERIF);
        ayah.setLineSpacing(0f, 1.45f);
        LinearLayout.LayoutParams ayahLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        ayahLp.topMargin = dp(18);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(24);

        row.addView(makeButton(IconButton.CLOSE, false, "إغلاق"));
        row.addView(makeButton(IconButton.CLOCK, false, "تأجيل"));
        row.addView(makeButton(IconButton.CHECK, true, "تم"));

        card.addView(head);
        card.addView(ayah, ayahLp);
        card.addView(row, rowLp);

        dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(card);

        Window w = dialog.getWindow();
        w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        w.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        w.setDimAmount(0f);
        w.setBackgroundDrawable(bg);
        w.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        int screenH = getResources().getDisplayMetrics().heightPixels;
        int room = screenH - dp(260);
        WindowManager.LayoutParams a = w.getAttributes();
        a.y = Math.max(0, Math.round(room * position / 100f));
        w.setAttributes(a);
        int width = getResources().getDisplayMetrics().widthPixels - dp(32);
        w.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        if (blur) {
            w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            w.setBackgroundBlurRadius(dp(30));
        }

        dialog.show();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
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
                paint.setColor(SOFT);
                canvas.drawCircle(cx, cy, r, paint);
            } else {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0x14FFFFFF);
                canvas.drawCircle(cx, cy, r, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.5f * d);
                paint.setColor(EDGE);
                canvas.drawCircle(cx, cy, r, paint);
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.4f * d);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(filled ? DEEP : (type == CLOSE ? 0xCCFFFFFF : WHITE));
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
