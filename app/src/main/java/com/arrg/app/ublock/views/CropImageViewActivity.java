package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.BlurEffectUtil;
import com.arrg.app.ublock.util.FileUtils;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CropImageViewActivity extends ATEActivity {

    private Bitmap bitmap;
    private Boolean isBlurIntensity = false;
    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;
    private String chosenFile;

    @Bind(R.id.crop_imageView)
    CropImageView cropImageView;

    @Bind(R.id.iv_background)
    ImageView background;

    @Bind(R.id.background_tools)
    LinearLayout backgroundTools;

    @Bind(R.id.blur_effect_intensity)
    LinearLayout blurEffectIntensity;

    @Bind(R.id.seekBar)
    SeekBar seekBar;

    /*@Bind(R.id.blur_effect_intensity_value)
    UTextView blurEffectIntensityValue;*/

    @OnClick({R.id.rotate, R.id.blur, R.id.done})
    public void onMenuItemClick(View view) {
        switch (view.getId()) {
            case R.id.rotate:
                cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                break;
            case R.id.blur:
                applyBlurEffect();
                break;
            case R.id.done:
                new SaveFileTask().execute();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image_view);
        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        preferencesUtil = new SharedPreferencesUtil(this);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        Bundle bundle = getIntent().getExtras();

        chosenFile = bundle.getString(getString(R.string.background));

        bitmap = BitmapFactory.decodeFile(chosenFile);

        background.setImageDrawable(new BitmapDrawable(getResources(), BlurEffectUtil.blur(this, bitmap)));

        cropImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("SeekBar", "Value On Changed: " + progress);

                //blurEffectIntensityValue.setText(String.format("%d", progress / 4));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("SeekBar", "Value On Start: " + seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("SeekBar", "Value On Stop: " + seekBar.getProgress());

                int radius = seekBar.getProgress();

                if (radius == 0) {
                    cropImageView.setImageBitmap(bitmap);
                } else {
                    cropImageView.setImageBitmap(BlurEffectUtil.blur(CropImageViewActivity.this, bitmap, radius));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        FileUtils.deleteFile(chosenFile);

        System.gc();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isBlurIntensity) {
            isBlurIntensity = false;

            YoYo.with(Techniques.FadeOut).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(blurEffectIntensity);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            backgroundTools.setVisibility(View.VISIBLE);
                            blurEffectIntensity.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }, Constants.DURATIONS_OF_ANIMATIONS);

            YoYo.with(Techniques.FadeInUp).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(backgroundTools);
        } else {
            FileUtils.deleteFile(chosenFile);

            Util.open(this, BackgroundSettingsActivity.class, true);
        }
    }

    public void applyBlurEffect() {
        if (!isBlurIntensity) {
            isBlurIntensity = true;

            YoYo.with(Techniques.FadeOutDown).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(backgroundTools);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            backgroundTools.setVisibility(View.INVISIBLE);
                            blurEffectIntensity.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }, Constants.DURATIONS_OF_ANIMATIONS);

            YoYo.with(Techniques.FadeIn).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(blurEffectIntensity);
        }
    }

    class SaveFileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            new MaterialDialog.Builder(CropImageViewActivity.this)
                    .content(R.string.saving_background)
                    .typeface(Util.getTypeface(CropImageViewActivity.this, preferencesUtil, settingsPreferences), Util.getTypeface(CropImageViewActivity.this, preferencesUtil, settingsPreferences))
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String path = File.separator + getString(R.string.app_name) + File.separator + getString(R.string.media) + File.separator + getString(R.string.wallpaper) + File.separator;
                    String fileName = getString(R.string.background_name);

                    File filepath = Environment.getExternalStorageDirectory();

                    File directory = new File(filepath.getAbsolutePath() + path);

                    File file = new File(directory, fileName);

                    FileUtils.copyFile(chosenFile, file.getAbsolutePath());

                    FileUtils.deleteFile(chosenFile);

                    if (Util.saveWallpaper(CropImageViewActivity.this, cropImageView.getCroppedBitmap(), path, fileName, false)) {
                        preferencesUtil.putValue(settingsPreferences, R.string.background, file.getAbsolutePath());
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Util.open(CropImageViewActivity.this, BackgroundSettingsActivity.class, true);
        }
    }
}
