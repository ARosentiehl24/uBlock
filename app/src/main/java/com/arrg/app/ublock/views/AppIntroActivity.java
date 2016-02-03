package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.controller.SlideSettingsFragment;
import com.arrg.app.ublock.controller.SlideUBlockFragment;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.github.paolorotolo.appintro.AppIntro2;

public class AppIntroActivity extends AppIntro2 {

    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;

    @Override
    public void init(Bundle savedInstanceState) {
        Util.hideActionBar(this);

        preferencesUtil = new SharedPreferencesUtil(this);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        addSlide(new SlideUBlockFragment());
        addSlide(new SlideSettingsFragment());

        getPager().setBackgroundColor(Config.primaryColorDark(this, null));

        setProgressButtonEnabled(true);
        setScrollDurationFactor(4);
        setSwipeLock(true);
        setVibrate(true);
        setVibrateIntensity(25);

        setFadeAnimation();
        //setCustomTransformer(new ZoomOutPageTransformer());
    }

    @Override
    public void onDonePressed() {
        if (patternWasConfigured() && pinWasConfigured()) {
            new AlertDialogWrapper.Builder(this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.repeat_slides_messages)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getPager().setCurrentItem(0);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Util.open(AppIntroActivity.this, SplashScreenActivity.class, true);
                            preferencesUtil.putValue(settingsPreferences, R.string.first_install, false);
                        }
                    }).show();
        } else {
            Toast.makeText(this, R.string.configure_initial_settings, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }

    public boolean patternWasConfigured() {
        return (preferencesUtil.getString(settingsPreferences, R.string.user_pattern, null) != null);
    }

    public boolean pinWasConfigured() {
        return (preferencesUtil.getString(settingsPreferences, R.string.user_pin, null) != null);
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float verMargin = pageHeight * (1 - scaleFactor) / 2;
                float horMargin = pageWidth * (1 - scaleFactor) / 2;

                if (position < 0) {
                    view.setTranslationX(horMargin - verMargin / 2);
                } else {
                    view.setTranslationX(-horMargin + verMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
