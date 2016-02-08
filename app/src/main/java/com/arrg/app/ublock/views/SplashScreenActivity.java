package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.adapters.DotsAdapter;
import com.arrg.app.ublock.controller.PinButtonClickedListener;
import com.arrg.app.ublock.controller.PinButtonEnum;
import com.arrg.app.ublock.controller.UBlockApplication;
import com.arrg.app.ublock.controller.ULinearLayoutManager;
import com.arrg.app.ublock.services.UBlockService;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.ThemeUtil;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.uviews.PatternLockView;
import com.arrg.app.ublock.views.uviews.PinLockView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class SplashScreenActivity extends ATEActivity {

    @Bind(R.id.et_pin)
    EditText etPin;

    @Bind(R.id.pattern)
    PatternLockView patternView;

    @Bind(R.id.pin)
    PinLockView pinLockView;

    @Bind(R.id.rv_dots)
    RecyclerView recyclerView;

    @Bind(R.id.vf_unlock_methods)
    ViewFlipper vfUnlockMethods;

    @OnClick({R.id.fab_fingerprint})
    public void OnClick(View id) {
        switch (id.getId()) {
            case R.id.fab_fingerprint:
                if (preferencesUtil.getInt(settingsPreferences, R.string.designated_finger, 0) == 0) {
                    displayFingerPrintRecognizer();
                } else {
                    displayFingerPrintRecognizerWithIndex();
                }
                break;
        }
    }

    @OnTextChanged(R.id.et_pin)
    public void OnPinChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().equals(storedPin)) {
            playUnlockSound();

            preferencesUtil.putValue(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());

            Util.open(SplashScreenActivity.this, ApplicationsListActivity.class, false);
        }
    }

    private static final String TAG = "SplashScreen";
    public static SplashScreenActivity splashScreenActivity;
    private Boolean enableFingerPrintRecognizer;
    private Boolean onReadyIdentify = false;
    private Boolean isInStealthMode;
    private Boolean isNecessaryShowInput = true;
    private Boolean isSwipeEnabled;
    private DotsAdapter dotsAdapter;
    private GestureDetector gestureDetector;
    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;
    private String storedPin;
    private UBlockApplication uBlockApplication;
    private Vibrator vibrator;

    private SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {

        @Override
        public void onFinished(int eventStatus) {
            Log.d("Finger", "identify finished : reason=" + UBlockApplication.getEventStatusName(eventStatus));

            onReadyIdentify = false;

            int FingerprintIndex = 0;

            try {
                FingerprintIndex = uBlockApplication.spassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
                Log.d("Finger", ise.getMessage());
            }

            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                Log.d("Finger", "onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
                playUnlockSound();

                preferencesUtil.putValue(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());

                Util.open(SplashScreenActivity.this, ApplicationsListActivity.class, false);
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                Log.d("Finger", "onFinished() : Password authentification Success");
            } else {
                Log.d("Finger", "onFinished() : Authentification Fail for identify");
            }
        }

        @Override
        public void onReady() {
            Log.d("Finger", "identify state is ready");
        }

        @Override
        public void onStarted() {
            Log.d("Finger", "User touched fingerprint sensor!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);

        applyCustomTheme();

        uBlockApplication = ((UBlockApplication) getApplicationContext()).getInstance();

        setupSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupInputData();
    }

    @Override
    protected void onStop() {
        super.onStop();

        restartInputPinData();

        isNecessaryShowInput = true;
    }

    @Override
    protected void onDestroy() {
        System.gc();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Util.close(this, true);
    }

    public void applyTheme() {
        splashScreenActivity = this;

        if (!isCustomThemeEnabled()) {
            ATE.config(this, null)
                    .primaryColorRes(R.color.blue_grey_900)
                    .autoGeneratePrimaryDark(true)
                    .accentColorRes(R.color.blue_grey_950)
                    .coloredStatusBar(true)
                    .coloredNavigationBar(true)
                    .navigationViewThemed(true)
                    .navigationViewNormalIconRes(R.color.background_dark)
                    .navigationViewNormalTextRes(R.color.background_dark)
                    .textColorPrimaryRes(R.color.primary_text_default_light)
                    .textColorSecondaryRes(R.color.secondary_text_light)
                    .commit();
        }
    }

    public boolean isCustomThemeEnabled() {
        return ATE.config(this, null).isConfigured();
    }

    public void applyCustomTheme() {
        ThemeUtil.applyTheme(Config.primaryColorDark(this, null), patternView);
    }

    public void setupSharedPreferences() {
        preferencesUtil = new SharedPreferencesUtil(this);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        setupInitialSettings();
    }

    public void setupInitialSettings() {
        if (preferencesUtil.getBoolean(settingsPreferences, R.string.first_install, true)) {
            showIntro();
        }

        enableFingerPrintRecognizer = preferencesUtil.getBoolean(settingsPreferences, R.string.user_fingerprint, R.bool.user_fingerprint);
        isInStealthMode = preferencesUtil.getBoolean(settingsPreferences, R.string.is_pattern_visible, R.bool.is_pattern_visible);
        storedPin = preferencesUtil.getString(settingsPreferences, R.string.user_pin, R.string.default_code);

        gestureDetector = new GestureDetector(this, new CustomGestureDetector());
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        enableFingerPrintIfNecessary();
        setListeners();
    }

    public void showIntro() {
        Util.open(this, AppIntroActivity.class, true);
    }

    public void enableFingerPrintIfNecessary() {
        if (!Util.isSamsungDevice(SplashScreenActivity.this) || !uBlockApplication.isFingerPrintEnabled()) {
            for (int i = 0; i < vfUnlockMethods.getChildCount(); i++) {
                if (vfUnlockMethods.getChildAt(i).getId() == R.id.cv_fingerprint) {
                    vfUnlockMethods.removeViewAt(i);
                    break;
                }
            }
        }
    }

    public void setListeners() {
        patternView.setInStealthMode(!isInStealthMode);
        patternView.setOnPatternListener(new PatternLockView.OnPatternListener() {
            String storedPattern = preferencesUtil.getString(settingsPreferences, R.string.user_pattern, R.string.default_pattern);

            @Override
            public void onPatternDetected(List<PatternLockView.Cell> pattern, String SimplePattern) {
                super.onPatternDetected(pattern, SimplePattern);

                if (SimplePattern.equals(storedPattern)) {
                    playUnlockSound();

                    preferencesUtil.putValue(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());

                    Util.open(SplashScreenActivity.this, ApplicationsListActivity.class, false);
                } else {
                    vibrator.vibrate(Constants.DURATIONS_OF_ANIMATIONS);

                    patternView.setDisplayMode(PatternLockView.DisplayMode.Wrong);
                    YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(patternView);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    patternView.clearPattern();
                                }
                            });
                        }
                    }, Constants.DURATIONS_OF_ANIMATIONS);
                }
            }
        });

        pinLockView.setPinButtonClickedListener(new PinButtonClickedListener() {
            @Override
            public void onButtonClick(PinButtonEnum pinButtonEnum) {
                if (pinButtonEnum == PinButtonEnum.BUTTON_BACK) {
                    if (etPin.getText().length() != 0) {
                        etPin.setText(etPin.getText().toString().substring(0, etPin.getText().length() - 1));
                        etPin.setSelection(etPin.length());

                        dotsAdapter.removeDot(dotsAdapter.getItemCount() - 1);
                    }
                } else if (pinButtonEnum == PinButtonEnum.BUTTON_DONE) {
                    if (etPin.getText().toString().equals(storedPin)) {
                        playUnlockSound();

                        preferencesUtil.putValue(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());

                        Util.open(SplashScreenActivity.this, ApplicationsListActivity.class, false);
                    } else {
                        YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etPin);
                        YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(recyclerView);
                    }
                } else {
                    String pinValue = String.valueOf(pinButtonEnum.getButtonValue());
                    etPin.setText(etPin.getText().toString().concat(pinValue));

                    dotsAdapter.addDot(dotsAdapter.getItemCount());
                }
            }
        });

        vfUnlockMethods.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !gestureDetector.onTouchEvent(event);
            }
        });

        startServiceIfNeeded();
    }

    public void playUnlockSound() {
        try {
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd("sounds/unlock.ogg");
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayInput(int id) {
        switch (id) {
            case R.id.cv_fingerprint:
                if (enableFingerPrintRecognizer) {
                    if (preferencesUtil.getInt(settingsPreferences, R.string.designated_finger, 0) == 0) {
                        displayFingerPrintRecognizer();
                    } else {
                        displayFingerPrintRecognizerWithIndex();
                    }
                }
                break;
        }
    }

    public void displayFingerPrintRecognizer() {
        try {
            if (!uBlockApplication.spassFingerprint.hasRegisteredFinger()) {
                log("Please register finger first");
            } else {
                if (!onReadyIdentify) {
                    onReadyIdentify = true;
                    try {
                        uBlockApplication.spassFingerprint.startIdentifyWithDialog(SplashScreenActivity.this, listener, false);
                        log("Please identify finger to verify you");
                    } catch (IllegalStateException e) {
                        onReadyIdentify = false;
                        log("Exception: " + e);
                    }
                } else {
                    log("Please cancel Identify first");
                }
            }
        } catch (UnsupportedOperationException e) {
            log("Fingerprint Service is not supported in the device");
        }
    }

    public void displayFingerPrintRecognizerWithIndex() {
        try {
            if (!uBlockApplication.spassFingerprint.hasRegisteredFinger()) {
                log("Please register finger first");
            } else {
                if (!onReadyIdentify) {
                    onReadyIdentify = true;
                    if (uBlockApplication.spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_FINGER_INDEX)) {
                        ArrayList<Integer> designatedFingers = new ArrayList<>();
                        designatedFingers.add(preferencesUtil.getInt(settingsPreferences, R.string.designated_finger, 1));
                        try {
                            uBlockApplication.spassFingerprint.setIntendedFingerprintIndex(designatedFingers);
                        } catch (IllegalStateException ise) {
                            log(ise.getMessage());
                        }
                    }
                    try {
                        uBlockApplication.spassFingerprint.startIdentifyWithDialog(SplashScreenActivity.this, listener, false);
                        log("Please identify fingerprint index " + preferencesUtil.getInt(settingsPreferences, R.string.designated_finger, 1) + " to verify you");
                    } catch (IllegalStateException e) {
                        onReadyIdentify = false;
                        log("Exception: " + e);
                    }
                } else {
                    log("Please cancel Identify first");
                }
            }
        } catch (UnsupportedOperationException e) {
            log("Fingerprint Service is not supported in the device");
        }
    }

    public void startServiceIfNeeded() {
        if (!UBlockService.isRunning(SplashScreenActivity.this, UBlockService.class)) {
            Intent i = new Intent(SplashScreenActivity.this, UBlockService.class);
            startService(i);
        }
    }

    public void setupInputData() {
        ArrayList<ImageView> dots = new ArrayList<>();

        dotsAdapter = new DotsAdapter(this, dots, Config.primaryColorDark(this, null));

        recyclerView.setAdapter(dotsAdapter);
        recyclerView.setLayoutManager(new ULinearLayoutManager(this, ULinearLayoutManager.HORIZONTAL, false));

        etPin.setText("");
        isSwipeEnabled = preferencesUtil.getBoolean(settingsPreferences, R.string.enable_swipe_on_ublock_screen, R.bool.enable_swipe_on_ublock_screen);
        patternView.clearPattern();

        if (isNecessaryShowInput) {
            isNecessaryShowInput = false;

            Integer lastMethodUsed = preferencesUtil.getInt(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());
            vfUnlockMethods.setDisplayedChild(lastMethodUsed);
            displayInput(vfUnlockMethods.getCurrentView().getId());
        }
    }

    public void restartInputPinData() {
        dotsAdapter = null;

        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
    }

    public void log(String log) {
        Log.d(TAG, log);
    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 100;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            System.out.println(" in onFling() :: ");

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && isSwipeEnabled) {
                vfUnlockMethods.setInAnimation(SplashScreenActivity.this, R.anim.left_in);
                vfUnlockMethods.setOutAnimation(SplashScreenActivity.this, R.anim.left_out);
                vfUnlockMethods.showNext();

                displayInput(vfUnlockMethods.getCurrentView().getId());
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && isSwipeEnabled) {
                vfUnlockMethods.setInAnimation(SplashScreenActivity.this, R.anim.right_in);
                vfUnlockMethods.setOutAnimation(SplashScreenActivity.this, R.anim.right_out);
                vfUnlockMethods.showPrevious();

                displayInput(vfUnlockMethods.getCurrentView().getId());
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
