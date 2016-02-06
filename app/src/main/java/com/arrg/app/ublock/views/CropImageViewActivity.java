package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CropImageViewActivity extends ATEActivity {

    private Bitmap bitmap;
    private Boolean isBlurIntensity = false;
    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;
    private String chosenFile;

    private Runnable hideViewsRunnable = new Runnable() {
        @Override
        public void run() {
            blurEffectRadiusIntensity.setVisibility(View.INVISIBLE);
        }
    };

    @Bind(R.id.crop_imageView)
    CropImageView cropImageView;

    @Bind(R.id.iv_background)
    ImageView background;

    @Bind(R.id.blur_effect_radius)
    LinearLayout blurEffectRadiusIntensity;

    @Bind(R.id.seekBar_radius)
    SeekBar seekBarRadius;

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

        background.setImageDrawable(new BitmapDrawable(getResources(), BlurEffectUtil.blur(this, bitmap, 25.f, 0.25f)));

        cropImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

        seekBarRadius.setProgress(0);
        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final int seekBarProgress = progress;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                float radius = (float) seekBarProgress;

                                if (seekBarProgress == 0) {
                                    cropImageView.setImageBitmap(bitmap);
                                } else {
                                    cropImageView.setImageBitmap(BlurEffectUtil.blur(CropImageViewActivity.this, bitmap, radius));
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

            new Handler().postDelayed(hideViewsRunnable, Constants.DURATIONS_OF_ANIMATIONS);

            YoYo.with(Techniques.FadeOutDown).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(blurEffectRadiusIntensity);
        } else {
            FileUtils.deleteFile(chosenFile);

            Util.open(this, BackgroundSettingsActivity.class, true);
        }
    }

    public void applyBlurEffect() {
        if (!isBlurIntensity) {
            isBlurIntensity = true;

            blurEffectRadiusIntensity.setVisibility(View.VISIBLE);

            YoYo.with(Techniques.FadeInUp).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(blurEffectRadiusIntensity);
        } else {
            isBlurIntensity = false;

            new Handler().postDelayed(hideViewsRunnable, Constants.DURATIONS_OF_ANIMATIONS);

            YoYo.with(Techniques.FadeOutDown).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(blurEffectRadiusIntensity);
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
                    String fileName = getString(R.string.background_name);

                    File file = new File(getExternalFilesDir(null), fileName);

                    FileUtils.deleteFile(chosenFile);

                    if (Util.saveWallpaper(cropImageView.getCroppedBitmap(), file)) {
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
