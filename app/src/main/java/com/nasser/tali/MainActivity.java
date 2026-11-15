package com.nasser.tali;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);

        Button perm = new Button(this);
        perm.setText("1) السماح بالعرض فوق التطبيقات");
        perm.setOnClickListener(v -> startActivity(new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + getPackageName()))));

        Button test = new Button(this);
        test.setText("2) تجربة الآية العائمة");
        test.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "امنح الصلاحية أولاً",
                    Toast.LENGTH_LONG).show();
                return;
            }
            startService(new Intent(this, OverlayService.class));
        });

        root.addView(perm);
        root.addView(test);
        setContentView(root);
    }
}
