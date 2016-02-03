package com.arrg.app.ublock.views;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.services.UBlockService;
import com.arrg.app.ublock.util.FileUtils;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.ThemeUtil;
import com.arrg.app.ublock.util.Util;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.sw926.imagefileselector.ImageFileSelector;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BackgroundSettingsActivity extends ATEActivity {

    private ImageFileSelector mImageFileSelector;
    private SharedPreferencesUtil preferencesUtil;
    private SharedPreferences settingsPreferences;
    private static final String CAMERA = "camera";
    private static final String GALLERY = "gallery";
    private UBlockService services = UBlockService.UBLOCK;

    @Bind(R.id.fab_menu)
    FloatingActionMenu fabMenu;

    @Bind(R.id.fab_current)
    FloatingActionButton fabCurrent;

    @Bind(R.id.fab_choose)
    FloatingActionButton fabChoose;

    @Bind(R.id.fab_photo)
    FloatingActionButton fabPhoto;

    @Bind(R.id.fab_reset)
    FloatingActionButton fabReset;

    @Bind(R.id.iv_background)
    ImageView background;

    @OnClick({R.id.fab_current, R.id.fab_choose, R.id.fab_photo, R.id.fab_reset})
    public void OnClick(View id) {
        switch (id.getId()) {
            case R.id.fab_current:
                setSystemWallpaper();
                break;
            case R.id.fab_choose:
                chooseCustomBackground(GALLERY);
                break;
            case R.id.fab_photo:
                chooseCustomBackground(CAMERA);
                break;
            case R.id.fab_reset:
                resetBackground();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_settings);
        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setupSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isCustomBackgroundSelected()) {
            String chosenWallpaper = preferencesUtil.getString(settingsPreferences, R.string.background, null);

            Bitmap bitmap = BitmapFactory.decodeFile(chosenWallpaper);
            background.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    protected void onDestroy() {
        System.gc();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageFileSelector.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (fabMenu.isOpened()) {
            fabMenu.close(true);
        } else {
            Util.close(this, true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mImageFileSelector.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageFileSelector.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mImageFileSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setupSharedPreferences() {
        preferencesUtil = new SharedPreferencesUtil(this);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        applyTheme();
    }

    public void applyTheme() {
        ThemeUtil.applyTheme(Config.primaryColor(this, null), fabMenu, fabCurrent, fabChoose, fabPhoto, fabReset);

        setupInitialVariables();
    }

    public void setupInitialVariables() {
        fabReset.setEnabled(isCustomBackgroundSelected());

        mImageFileSelector = new ImageFileSelector(this);
        mImageFileSelector.setCallback(new ImageFileSelector.Callback() {
            @Override
            public void onSuccess(String chosenFile) {
                Intent editImageIntent = new Intent(BackgroundSettingsActivity.this, CropImageViewActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.background), chosenFile);

                editImageIntent.putExtras(bundle);

                Util.openInverse(BackgroundSettingsActivity.this, editImageIntent, true);
            }

            @Override
            public void onError() {

            }
        });
    }

    public boolean isCustomBackgroundSelected() {
        return (preferencesUtil.getString(settingsPreferences, R.string.background, null) != null);
    }

    public void setSystemWallpaper() {
        new AsyncTask<Void, Void, Void>() {

            private Intent editImageIntent;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                new MaterialDialog.Builder(BackgroundSettingsActivity.this)
                        .content(R.string.saving_background)
                        .typeface(Util.getTypeface(BackgroundSettingsActivity.this, preferencesUtil, settingsPreferences), Util.getTypeface(BackgroundSettingsActivity.this, preferencesUtil, settingsPreferences))
                        .progress(true, 0)
                        .show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(BackgroundSettingsActivity.this);

                editImageIntent = new Intent(BackgroundSettingsActivity.this, CropImageViewActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.background), Util.saveWallpaper(BackgroundSettingsActivity.this, ((BitmapDrawable) wallpaperManager.getDrawable()).getBitmap()));

                editImageIntent.putExtras(bundle);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Util.openInverse(BackgroundSettingsActivity.this, editImageIntent, true);
            }
        }.execute();
    }

    public void chooseCustomBackground(String SOURCE) {
        fabMenu.close(true);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        mImageFileSelector.setOutPutImageSize(width, height);
        mImageFileSelector.setQuality(80);

        preferencesUtil.putValue(settingsPreferences, "open_camera_gallery", true);

        switch (SOURCE) {
            case GALLERY:
                mImageFileSelector.selectImage(this);
                break;
            case CAMERA:
                mImageFileSelector.takePhoto(this);
                break;
        }
    }

    public void resetBackground() {
        background.setScaleType(ImageView.ScaleType.FIT_XY);
        background.setImageDrawable(new ColorDrawable(Config.primaryColorDark(this, null)));

        preferencesUtil.deleteValue(settingsPreferences, R.string.background);

        fabReset.setEnabled(isCustomBackgroundSelected());

        FileUtils.deleteFile(preferencesUtil.getString(settingsPreferences, R.string.background, null));
    }
}


                    /*Log.d("MoveToVaultTask", new File(getActivity().getFilesDir().getAbsolutePath()).getAbsolutePath());

                    for (File file : new File(getActivity().getFilesDir().getAbsolutePath()).listFiles()) {
                        if (preferencesUtil.exists(settingsPreferences, file.getName())) {
                            Log.d("MoveToVaultTaskRestore", "Copy: " + file.getName() + " from " + file.getAbsolutePath() + " to " + preferencesUtil.getString(settingsPreferences, file.getName(), null));
                            Log.d("MoveToVaultTaskRestore", "Delete: " + file.getAbsolutePath());
                            Log.d("MoveToVaultTaskRestore", "DeleteKey: " + file.getName());

                            FileUtils.copyFile(file.getAbsolutePath(), preferencesUtil.getString(settingsPreferences, file.getName(), null));
                            FileUtils.deleteFile(file);

                            preferencesUtil.deleteValue(settingsPreferences, file.getName());

                            uFileAdapter.addFile(new UFile(preferencesUtil.getString(settingsPreferences, file.getName(), null)), uFileAdapter.getItemCount());
                        }
                    }*/
