package com.astralauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private static final int COLS = 4;
    private static final int DOCK_COUNT = 4;
    private GridLayout grid;
    private LinearLayout dock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grid = findViewById(R.id.appGrid);
        dock = findViewById(R.id.dock);

        populateApps();
        fillDock();
    }

    private List<ResolveInfo> queryApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        // Исключаем сам лаунчер из списка
        List<ResolveInfo> filtered = new ArrayList<>();
        for (ResolveInfo ai : apps) {
            if (ai.activityInfo != null && ai.activityInfo.packageName.equals(getPackageName())) continue;
            filtered.add(ai);
        }
        Collections.sort(filtered, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return a.loadLabel(getPackageManager()).toString().toLowerCase()
                        .compareTo(b.loadLabel(getPackageManager()).toString().toLowerCase());
            }
        });
        return filtered;
    }

    private void populateApps() {
        List<ResolveInfo> apps = queryApps();
        int col = 0;
        grid.removeAllViews();

        for (ResolveInfo ai : apps) {
            final ResolveInfo app = ai;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(2), dp(4), dp(2), dp(4));

            ImageView icon = new ImageView(this);
            icon.setImageDrawable(app.loadIcon(getPackageManager()));
            int iconSize = dp(50);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(iconSize, iconSize);
            ilp.setMargins(0, 0, 0, dp(4));
            icon.setLayoutParams(ilp);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

            TextView label = new TextView(this);
            label.setText(app.loadLabel(getPackageManager()));
            label.setTextColor(Color.WHITE);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            label.setMaxLines(1);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END);
            label.setGravity(Gravity.CENTER);
            label.setShadowLayer(dp(1), 0, dp(1), Color.BLACK);
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            item.addView(icon);
            item.addView(label);

            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    launch(app.activityInfo.packageName, app.activityInfo.name);
                }
            });

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(col, 1f);
            lp.setMargins(dp(6), dp(10), dp(6), dp(10));
            item.setLayoutParams(lp);
            grid.addView(item);

            col = (col + 1) % COLS;
        }
    }

    private void fillDock() {
        List<ResolveInfo> apps = queryApps();
        dock.removeAllViews();
        // Док: телефон, почта, браузер, "Astra" — подбираем по имени
        for (int i = 0; i < apps.size() && dock.getChildCount() < DOCK_COUNT; i++) {
            final ResolveInfo ai = apps.get(i);
            ImageView iv = new ImageView(this);
            iv.setImageDrawable(ai.loadIcon(getPackageManager()));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(46), dp(46));
            lp.setMargins(dp(8), 0, dp(8), 0);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    launch(ai.activityInfo.packageName, ai.activityInfo.name);
                }
            });
            dock.addView(iv);
        }
    }

    private void launch(String pkg, String activityName) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setClassName(pkg, activityName);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        try {
            startActivity(i);
        } catch (Exception e) {
            // игнорируем
        }
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateApps();
        fillDock();
    }
}