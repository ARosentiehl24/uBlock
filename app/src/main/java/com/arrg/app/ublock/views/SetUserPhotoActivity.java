package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.BlurEffectUtil;
import com.arrg.app.ublock.util.FileUtils;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetUserPhotoActivity extends ATEActivity {

    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;
    private String chosenFile;

    @Bind(R.id.crop_imageView)
    CropImageView cropImageView;

    @Bind(R.id.iv_background)
    ImageView background;

    @OnClick({R.id.btn_accept})
    public void OnClick(View id) {
        switch (id.getId()) {
            case R.id.btn_accept:
                new SaveFileTask().execute();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_user_photo);
        ButterKnife.bind(this);

        View window = getWindow().getDecorView();
        window.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        preferencesUtil = new SharedPreferencesUtil(this);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        Bundle bundle = getIntent().getExtras();

        chosenFile = bundle.getString(getString(R.string.background));

        Bitmap bitmap = BitmapFactory.decodeFile(chosenFile);

        background.setImageDrawable(new BitmapDrawable(getResources(), BlurEffectUtil.blur(this, bitmap, 25.f, 0.25f)));

        cropImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
    }

    @Override
    protected void onDestroy() {
        System.gc();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        FileUtils.deleteFile(chosenFile);

        Util.closeInverse(this, true);
    }

    class SaveFileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            new MaterialDialog.Builder(SetUserPhotoActivity.this)
                    .content(R.string.saving_background)
                    .typeface(Util.getTypeface(SetUserPhotoActivity.this, preferencesUtil, settingsPreferences), Util.getTypeface(SetUserPhotoActivity.this, preferencesUtil, settingsPreferences))
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String fileName = getString(R.string.user_picture_name);

                    File file = new File(getExternalFilesDir(null), fileName);

                    FileUtils.deleteFile(chosenFile);

                    if (Util.saveWallpaper(cropImageView.getCroppedBitmap(), file)) {
                        preferencesUtil.putValue(settingsPreferences, R.string.user_picture_preference, file.getAbsolutePath());
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Util.closeInverse(SetUserPhotoActivity.this, true);
        }
    }
}
