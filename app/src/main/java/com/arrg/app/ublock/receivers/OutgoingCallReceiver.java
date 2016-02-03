package com.arrg.app.ublock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.views.SplashScreenActivity;

public class OutgoingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferencesUtil mPreferencesUtil = new SharedPreferencesUtil(context);
        SharedPreferences settingsPreferences = context.getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        String numberToLaunchResource = context.getString(R.string.number);

        Boolean launch = mPreferencesUtil.getBoolean(settingsPreferences, R.string.enable_icon_on_app_drawer, R.bool.enable_icon_on_app_drawer);
        String launchNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        String storedNumber = mPreferencesUtil.getString(settingsPreferences, numberToLaunchResource, numberToLaunchResource);

        if (!launch && storedNumber.equals(launchNumber)) {
            setResultData(null);

            Intent startHomeScreen = new Intent(Intent.ACTION_MAIN);
            startHomeScreen.addCategory(Intent.CATEGORY_HOME);
            startHomeScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startHomeScreen);

            Intent launchSplashScreen = new Intent(context, SplashScreenActivity.class);
            launchSplashScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchSplashScreen);
        }
    }
}
