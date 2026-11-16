package com.nasser.tali;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
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
        TextView label = new TextView(this);
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(15);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(22);
        label.setLayoutParams(lp);

        SeekBar bar = new SeekBar(this);
        bar.setMax(max);
        int value = prefs.getInt(key, def);
        bar.setProgress(value);
        label.setText(title + ": " + value + "%");
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

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("tali_prefs", MODE_PRIVATE);
        getWindow().getDecorView().setSystemUiVisibility(0);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(56), dp(24), dp(32));
        root.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xFF123B4D, 0xFF06141C}));

        TextView title = new TextView(this);
        title.setText("تالي");
        title.setTextSize(36);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);

        TextView sub = new TextView(this);
        sub.setText("آية تظهر لك عند كل فتح للجوال");
        sub.setTextSize(14);
        sub.setTextColor(0xB3FFFFFF);
        sub.setGravity(Gravity.CENTER);

        root.addView(title);
        root.addView(sub);

        Button perm = makeButton("السماح بالعرض فوق التطبيقات", false);
        perm.setOnClickListener(v -> startActivity(new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + getPackageName()))));
        LinearLayout.LayoutParams plp = (LinearLayout.LayoutParams) perm.getLayoutParams();
        plp.topMargin = dp(32);
        root.addView(perm);

        TextView hint = new TextView(this);
        hint.setText("الموضع: 0% أعلى الشاشة، 100% أسفلها");
        hint.setTextSize(12);
        hint.setTextColor(0x99FFFFFF);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = dp(28);
        hint.setLayoutParams(hlp);
        root.addView(hint);

        addSlider(root, "شفافية البطاقة", "transparency", 80, 40);
        addSlider(root, "موضع البطاقة", "position", 100, 15);

        Button test = makeButton("تجربة البطاقة", true);
        LinearLayout.LayoutParams tlp = (LinearLayout.LayoutParams) test.getLayoutParams();
        tlp.topMargin = dp(32);
        test.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "امنح الصلاحية أولاً", Toast.LENGTH_LONG).show();
                return;
            }
            stopService(new Intent(this, OverlayService.class));
            startService(new Intent(this, OverlayService.class));
        });
        root.addView(test);

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.addView(root);
        setContentView(sv);
    }
}
