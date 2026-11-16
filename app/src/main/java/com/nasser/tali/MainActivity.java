package com.nasser.tali;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private TextView progress;

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView makeText(String text, int size, int color, int topMargin) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(topMargin);
        t.setLayoutParams(lp);
        return t;
    }

    private Button makeButton(String text, boolean primary) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setTextSize(16);
        btn.setTextColor(primary ? 0xFF0A2230 : 0xFFFFFFFF);
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(16));
        if (primary) {
            g.setColor(0xFFE9F7F9);
        } else {
            g.setColor(0x1AFFFFFF);
            g.setStroke(dp(1), 0x66FFFFFF);
        }
        btn.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        lp.topMargin = dp(12);
        btn.setLayoutParams(lp);
        return btn;
    }

    private void addSlider(LinearLayout parent, String title, String key, int max, int def) {
        int value = prefs.getInt(key, def);
        TextView label = makeText(title + ": " + value + "%", 15, 0xFFFFFFFF, 22);
        SeekBar bar = new SeekBar(this);
        bar.setMax(max);
        bar.setProgress(value);
        bar.setProgressTintList(ColorStateList.valueOf(0xFFE9F7F9));
        bar.setThumbTintList(ColorStateList.valueOf(0xFFE9F7F9));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                label.setText(title + ": " + p + "%");
                prefs.edit().putInt(key, p).apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar s) {}
            @Override
            public void onStopTrackingTouch(SeekBar s) {}
        });
        parent.addView(label);
        parent.addView(bar);
    }

    private void refreshProgress() {
        QuranRepo repo = QuranRepo.get(this);
        if (repo.size() == 0) {
            progress.setText("ملف المصحف غير موجود في المشروع");
            return;
        }
        int cur = Math.min(prefs.getInt("current", 0), repo.size() - 1);
        progress.setText("الآية القادمة: " + repo.header(cur));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProgress();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("tali_prefs", MODE_PRIVATE);
        getWindow().getDecorView().setSystemUiVisibility(0);

        if (Build.VERSION.SDK_INT >= 33
            && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(56), dp(24), dp(32));
        root.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xFF123B4D, 0xFF06141C}));

        root.addView(makeText("تالي", 36, 0xFFFFFFFF, 0));
        root.addView(makeText("آية تظهر لك عند كل فتح للجوال", 14, 0xB3FFFFFF, 0));
        progress = makeText("", 15, 0xFFE9F7F9, 20);
        root.addView(progress);

        Button perm = makeButton("1) السماح بالعرض فوق التطبيقات", false);
        ((LinearLayout.LayoutParams) perm.getLayoutParams()).topMargin = dp(28);
        perm.setOnClickListener(v -> startActivity(new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + getPackageName()))));
        root.addView(perm);

        Button battery = makeButton("2) استثناء التطبيق من توفير البطارية", false);
        battery.setOnClickListener(v -> {
            try {
                startActivity(new Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:" + getPackageName())));
            } catch (Exception e) {
                Toast.makeText(this, "تعذر فتح الإعداد", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(battery);

        Button on = makeButton("3) تفعيل التطبيق", true);
        on.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "امنح الصلاحية الأولى أولاً", Toast.LENGTH_LONG).show();
                return;
            }
            prefs.edit().putBoolean("enabled", true).apply();
            startForegroundService(new Intent(this, WatchService.class));
            Toast.makeText(this, "تم التفعيل", Toast.LENGTH_SHORT).show();
        });
        root.addView(on);

        Button off = makeButton("إيقاف التطبيق", false);
        off.setOnClickListener(v -> {
            prefs.edit().putBoolean("enabled", false).apply();
            stopService(new Intent(this, WatchService.class));
            Toast.makeText(this, "تم الإيقاف", Toast.LENGTH_SHORT).show();
        });
        root.addView(off);

        root.addView(makeText("الموضع: 0% أعلى الشاشة، 100% أسفلها", 12, 0x99FFFFFF, 28));
        addSlider(root, "شفافية البطاقة", "transparency", 80, 40);
        addSlider(root, "موضع البطاقة", "position", 100, 15);

        Button test = makeButton("تجربة البطاقة", true);
        ((LinearLayout.LayoutParams) test.getLayoutParams()).topMargin = dp(28);
        test.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "امنح الصلاحية أولاً", Toast.LENGTH_LONG).show();
                return;
            }
            stopService(new Intent(this, OverlayService.class));
            startService(new Intent(this, OverlayService.class));
        });
        root.addView(test);

        root.addView(makeText("نص القرآن: مشروع تنزيل (tanzil.net)", 12, 0x80FFFFFF, 28));

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.addView(root);
        setContentView(sv);
    }
}
