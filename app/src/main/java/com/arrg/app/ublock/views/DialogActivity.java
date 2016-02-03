package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.uviews.UTextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogActivity extends AppCompatActivity {

    private SharedPreferencesUtil preferencesUtil;
    private SharedPreferences lockedAppsPreferences;
    private String appPackage;

    @Bind(R.id.iv_app_icon)
    ImageView icon;

    @Bind(R.id.tv_app_name)
    UTextView name;

    @OnClick({R.id.button_ok, R.id.button_no})
    public void onClick(View id) {
        switch (id.getId()) {
            case R.id.button_ok:
                preferencesUtil.putValue(lockedAppsPreferences, appPackage, true);
                Util.close(this, true);
                break;
            case R.id.button_no:
                Util.close(this, true);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        ButterKnife.bind(this);
        Util.hideActionBar(this);

        preferencesUtil = new SharedPreferencesUtil(this);
        lockedAppsPreferences = getSharedPreferences(Constants.LOCKED_APPS_PREFERENCES, Context.MODE_PRIVATE);

        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getIntent().getStringExtra(getString(R.string.app_installed)), 0);

            Drawable appIcon = applicationInfo.loadIcon(getPackageManager());
            String appName = (String) applicationInfo.loadLabel(getPackageManager());
            appPackage = getIntent().getStringExtra(getString(R.string.app_installed));

            icon.setImageDrawable(appIcon);
            name.setText(appName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        setFinishOnTouchOutside(false);
    }

    @Override
    protected void onDestroy() {
        System.gc();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }
}
