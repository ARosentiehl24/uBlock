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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

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

    public static boolean isSimSupport(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    }

    public static Boolean saveWallpaper(AppCompatActivity activity, Bitmap bitmap, String path, String fileName, Boolean showMessage) {
        // TODO Auto-generated method stub

        OutputStream output;

        // Find the SD Card path
        File filepath = Environment.getExternalStorageDirectory();

        // Create a new folder in SD Card
        File directory = new File(filepath.getAbsolutePath() + path);

        directory.mkdirs();

        // Create a name for the saved image
        File file = new File(directory, fileName);

        //preferencesUtil.putValue(settingsPreferences, R.string.background, file.getAbsolutePath());

        File checkFile = new File(file.getAbsolutePath());

        if (checkFile.exists() && showMessage) {
            // Show a toast message on successful save
            Toast.makeText(activity, activity.getString(R.string.image_saved_correctly) + " " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }

        try {
            output = new FileOutputStream(file);

            // Compress into png format image from 0% - 100%
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

    public static Integer getAverageColor(Drawable drawable) {
        //Setup initial variables
        int hSamples = 40;            //Number of pixels to sample on horizontal axis
        int vSamples = 40;            //Number of pixels to sample on vertical axis
        int sampleSize = hSamples * vSamples; //Total number of pixels to sample
        float[] sampleTotals = {0, 0, 0};   //Holds temporary sum of HSV values

        //If white pixels are included in sample, the average color will
        //  often have an unexpected shade. For this reason, we set a
        //  minimum saturation for pixels to be included in the sample set.
        //  Saturation < 0.1 is very close to white (see http://mkweb.bcgsc.ca/color_summarizer/?faq)
        float minimumSaturation = 0.1f;     //Saturation range is 0...1

        //By the same token, we should ignore transparent pixels
        //  (pixels with low alpha value)
        int minimumAlpha = 200;         //Alpha range is 0...255

        //Get bitmap
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();

        int width = b.getWidth();
        int height = b.getHeight();

        //Loop through pixels horizontally
        float[] hsv = new float[3];

        int sample;

        for (int i = 0; i < width; i += (width / hSamples)) {
            //Loop through pixels vertically
            for (int j = 0; j < height; j += (height / vSamples)) {
                //Get pixel & convert to HSV format
                sample = b.getPixel(i, j);
                Color.colorToHSV(sample, hsv);

                //Check pixel matches criteria to be included in sample
                if ((Color.alpha(sample) > minimumAlpha) && (hsv[1] >= minimumSaturation)) {
                    //Add sample values to total
                    sampleTotals[0] += hsv[0];  //H
                    sampleTotals[1] += hsv[1];  //S
                    sampleTotals[2] += hsv[2];  //V
                } else {
                    Log.v(TAG, "Pixel rejected: Alpha " + Color.alpha(sample) + ", H: " + hsv[0] + ", S:" + hsv[1] + ", V:" + hsv[1]);
                }
            }
        }

        //Divide total by number of samples to get average HSV values
        float[] average = new float[3];

        average[0] = sampleTotals[0] / sampleSize;
        average[1] = sampleTotals[1] / sampleSize;
        average[2] = sampleTotals[2] / sampleSize;

        return Color.HSVToColor(average);
    }

    public static String saveWallpaper(AppCompatActivity activity, Bitmap bitmap) {
// TODO Auto-generated method stub

        OutputStream output;

        // Find the SD Card path
        File filepath = activity.getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE);

        // Create a new folder in SD Card
        File directory = new File(filepath.getAbsolutePath());

        directory.mkdirs();

        // Create a name for the saved image
        File file = new File(directory, activity.getString(R.string.background_name));

        //preferencesUtil.putValue(settingsPreferences, R.string.background, file.getAbsolutePath());

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
