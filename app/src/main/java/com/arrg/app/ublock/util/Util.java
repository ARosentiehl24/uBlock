package com.arrg.app.ublock.util;

/*
 * Created by albert on 23/12/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.arrg.app.ublock.R;
import com.arrg.app.ublock.views.SplashScreenActivity;
import com.jaredrummler.android.device.DeviceName;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Util {

    protected final static String TAG = "Util";

    public static void close(AppCompatActivity activity, Boolean finish) {
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (finish) {
            activity.finish();
        }
    }

    public static void closeInverse(AppCompatActivity activity, Boolean finish) {
        if (finish) {
            activity.finish();
        }
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void open(AppCompatActivity activity, Class aClass, Boolean finish) {
        activity.startActivity(new Intent(activity, aClass));
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (finish) {
            activity.finish();
        }
    }

    public static void open(FragmentActivity activity, Class aClass, Boolean finish) {
        activity.startActivity(new Intent(activity, aClass));
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (finish) {
            activity.finish();
        }
    }

    public static void open(FragmentActivity activity, Intent intent, Boolean finish) {
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (finish) {
            activity.finish();
        }
    }

    public static void open(AppCompatActivity activity, Intent intent, Boolean finish) {
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (finish) {
            activity.finish();
        }
    }

    public static void openInverse(AppCompatActivity activity, Intent intent, Boolean finish) {
        activity.startActivity(intent);
        if (finish) {
            activity.finish();
        }
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void hideActionBar(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public static void hideActionBarUp(AppCompatActivity activity, Boolean value) {
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(value);
        }
    }

    public static void restartApp(Activity activity) {
        Intent intent = new Intent(activity, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void startHomeScreenActivity(Activity activity) {
        Intent startHomeScreen = new Intent(Intent.ACTION_MAIN);
        startHomeScreen.addCategory(Intent.CATEGORY_HOME);
        activity.startActivity(startHomeScreen);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.finish();
    }

    public static Boolean hasNavBar(Resources resources) {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    public static Boolean isSamsungDevice(Context context) {
        return DeviceName.getDeviceInfo(context).manufacturer.toUpperCase().contains("Samsung".toUpperCase());
    }

    public static Boolean isFingerprintEnabled(Activity activity) {
        Spass spass = new Spass();

        try {
            spass.initialize(activity);
        } catch (SsdkUnsupportedException e) {
            Log.d("Finger", "Exception: " + e);
        } catch (UnsupportedOperationException e) {
            Log.d("Finger", activity.getString(R.string.fingerprint_service_is_not_supported));
        }

        Boolean isFeatureEnabled = spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);

        if (isFeatureEnabled) {
            Log.d("Finger", "Fingerprint Service is supported in the device.");
            Log.d("Finger", "SDK version : " + spass.getVersionName());

            return true;
        } else {
            Log.d("Finger", "Fingerprint Service is not supported in the device.");

            return false;
        }
    }

    public static Boolean isSimSupport(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    }

    public static Boolean saveWallpaper(Bitmap bitmap, File picture) {
        // TODO Auto-generated method stub

        try {
            OutputStream output = new FileOutputStream(picture);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.flush();
            output.close();

            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return false;
        }
    }

    public static String getInfoToSendBugReport(AppCompatActivity activity) {
        return "App: " + activity.getString(R.string.app_name) + "\n" +
                "Version: " + AppUtils.getVerName(activity) + "\n" +
                "Device: " + DeviceName.getDeviceName();
    }

    public static String saveWallpaper(AppCompatActivity activity, Bitmap bitmap) {
// TODO Auto-generated method stub

        OutputStream output;

        // Create a name for the saved image
        File file = new File(activity.getExternalCacheDir(), activity.getString(R.string.background_name));

        File checkFile = new File(file.getAbsolutePath());

        try {
            output = new FileOutputStream(file);

            // Compress into png format image from 0% - 100%
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.flush();
            output.close();

            return checkFile.getAbsolutePath();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return null;
        }
    }

    public static Typeface getTypeface(Context context, SharedPreferencesUtil util, SharedPreferences preferences) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/" + util.getString(preferences, R.string.selected_font, R.string.custom_font) + ".ttf");
    }
}
