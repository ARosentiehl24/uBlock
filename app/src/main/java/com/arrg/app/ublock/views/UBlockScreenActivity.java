package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.controller.PinButtonClickedListener;
import com.arrg.app.ublock.controller.PinButtonEnum;
import com.arrg.app.ublock.services.UBlockService;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.ThemeUtil;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.uviews.PatternLockView;
import com.arrg.app.ublock.views.uviews.PinLockView;
import com.arrg.app.ublock.views.uviews.UTextView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.samsung.android.sdk.SsdkUnsupportedException;
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

public class UBlockScreenActivity extends ATEActivity {

    @Bind(R.id.cv_fingerprint)
    CardView cvFingerprint;

    @Bind(R.id.cv_pattern)
    CardView cvPattern;

    @Bind(R.id.cv_et_pin)
    CardView cvEtPin;

    @Bind(R.id.cv_pin)
    CardView cvPin;

    @Bind(R.id.et_pin)
    EditText etPin;

    @Bind(R.id.fab_fingerprint)
    FloatingActionButton fabFingerprint;

    @Bind(R.id.fab_settings)
    FloatingActionButton fabSettings;

    @Bind(R.id.iv_app_icon)
    ImageView ivAppIcon;

    @Bind(R.id.pattern)
    PatternLockView patternView;

    @Bind(R.id.pin)
    PinLockView pinLockView;

    @Bind(R.id.tv_app_name)
    UTextView tvAppName;

    @Bind(R.id.vf_unlock_methods)
    ViewFlipper vfUnlockMethods;

    @OnClick({R.id.fab_fingerprint, R.id.fab_settings})
    public void OnClick(View id) {
        switch (id.getId()) {
            case R.id.fab_fingerprint:
                if (preferencesUtil.getInt(settingsPreferences, R.string.designated_finger, 0) == 0) {
                    displayFingerPrintRecognizer();
                } else {
                    displayFingerPrintRecognizerWithIndex();
                }
                break;
            case R.id.fab_settings:
                if (!openSettings) {
                    openSettings = true;

                    YoYo.with(Techniques.FadeOutDown).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(ivAppIcon);
                    YoYo.with(Techniques.FadeOutDown).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvAppName);

                    YoYo.with(Techniques.FadeInUp).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(ivAppIcon);
                    YoYo.with(Techniques.FadeInUp).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvAppName);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ivAppIcon.setImageResource(R.drawable.play_store_icon);
                                    tvAppName.setText(R.string.action_settings);

                                    if (vfUnlockMethods.getCurrentView().getId() == R.id.cv_fingerprint) {
                                        if (preferencesUtil.getInt(settingsPreferences, R.string.designated_finger, 0) == 0) {
                                            displayFingerPrintRecognizer();
                                        } else {
                                            displayFingerPrintRecognizerWithIndex();
                                        }
                                    }
                                }
                            });
                        }
                    }, Constants.DURATIONS_OF_ANIMATIONS);
                }
                break;
        }
    }

    @OnTextChanged(R.id.et_pin)
    public void OnPinChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().equals(storedPin)) {
            playUnlockSound();

            preferencesUtil.putValue(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());

            if (openSettings) {
                Intent applicationListIntent = new Intent(this, ApplicationsListActivity.class);

                Bundle bundle = new Bundle();
                bundle.putBoolean(getString(R.string.was_open_from_ublock_screen), true);

                applicationListIntent.putExtras(bundle);

                Util.openInverse(this, applicationListIntent, false);
            } else {
                services.unLockApp(getIntent().getStringExtra(getString(R.string.activityOnTop)));
                Util.closeInverse(this, true);
            }
        }
    }

    private static final String TAG = "uBlockScreen";
    private Boolean enableFingerPrintRecognizer;
    private Boolean openSettings = false;
    private Boolean onReadyIdentify = false;
    private Boolean isNecessaryShowInput = true;
    private Boolean isSwipeEnabled;
    private GestureDetector gestureDetector;
    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;
    private Spass spass;
    private SpassFingerprint spassFingerprint;
    private String storedPin;
    private UBlockService services = UBlockService.UBLOCK;
    private Vibrator vibrator;

    private SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {

        @Override
        public void onFinished(int eventStatus) {
            Log.d("Finger", "identify finished : reason=" + getEventStatusName(eventStatus));

            onReadyIdentify = false;

            int FingerprintIndex = 0;

            try {
                FingerprintIndex = spassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
                Log.d("Finger", ise.getMessage());
            }

            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                Log.d("Finger", "onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
                playUnlockSound();

                preferencesUtil.putValue(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());

                if (openSettings) {
                    Intent applicationListIntent = new Intent(UBlockScreenActivity.this, ApplicationsListActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putBoolean(getString(R.string.was_open_from_ublock_screen), true);

                    applicationListIntent.putExtras(bundle);

                    Util.openInverse(UBlockScreenActivity.this, applicationListIntent, false);
                } else {
                    services.unLockApp(getIntent().getStringExtra(getString(R.string.activityOnTop)));
                    Util.closeInverse(UBlockScreenActivity.this, true);
                }
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

    private static String getEventStatusName(int eventStatus) {
        switch (eventStatus) {
            case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                return "STATUS_AUTHENTIFICATION_SUCCESS";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                return "STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS";
            case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                return "STATUS_TIMEOUT";
            case SpassFingerprint.STATUS_SENSOR_FAILED:
                return "STATUS_SENSOR_ERROR";
            case SpassFingerprint.STATUS_USER_CANCELLED:
                return "STATUS_USER_CANCELLED";
            case SpassFingerprint.STATUS_QUALITY_FAILED:
                return "STATUS_QUALITY_FAILED";
            case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                return "STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
            default:
                return "STATUS_AUTHENTIFICATION_FAILED";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ublock_screen);
        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setupSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();

        spassFingerprint = new SpassFingerprint(UBlockScreenActivity.this);

        etPin.setText("");
        patternView.clearPattern();

        if (isNecessaryShowInput) {
            isNecessaryShowInput = false;

            Integer lastMethodUsed = preferencesUtil.getInt(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());
            vfUnlockMethods.setDisplayedChild(lastMethodUsed);
            displayInput(vfUnlockMethods.getCurrentView().getId());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        isNecessaryShowInput = true;

        finish();
    }

    @Override
    protected void onDestroy() {
        System.gc();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (openSettings) {
            openSettings = false;

            YoYo.with(Techniques.FadeOutDown).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(ivAppIcon);
            YoYo.with(Techniques.FadeOutDown).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvAppName);

            YoYo.with(Techniques.FadeInUp).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(ivAppIcon);
            YoYo.with(Techniques.FadeInUp).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvAppName);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getIntent().getStringExtra(getString(R.string.activityOnTop)), 0);
                                ivAppIcon.setImageDrawable(applicationInfo.loadIcon(getPackageManager()));
                                tvAppName.setText(applicationInfo.loadLabel(getPackageManager()));
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }, Constants.DURATIONS_OF_ANIMATIONS);
        } else {
            Intent startHomeScreen = new Intent(Intent.ACTION_MAIN);
            startHomeScreen.addCategory(Intent.CATEGORY_HOME);
            Util.openInverse(this, startHomeScreen, true);
        }
    }

    public void setupSharedPreferences() {
        preferencesUtil = new SharedPreferencesUtil(this);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        new StartVariablesTask().execute();

        setupInitialSettings();
    }

    public void setupInitialSettings() {
        enableFingerPrintRecognizer = preferencesUtil.getBoolean(settingsPreferences, R.string.user_fingerprint, R.bool.user_fingerprint);
        gestureDetector = new GestureDetector(this, new CustomGestureDetector());
        isSwipeEnabled = preferencesUtil.getBoolean(settingsPreferences, R.string.enable_swipe_on_ublock_screen, R.bool.enable_swipe_on_ublock_screen);
        patternView.setInStealthMode(!preferencesUtil.getBoolean(settingsPreferences, R.string.is_pattern_visible, R.bool.is_pattern_visible));
        spass = new Spass();
        storedPin = preferencesUtil.getString(settingsPreferences, R.string.user_pin, R.string.default_code);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!Util.isSamsungDevice(UBlockScreenActivity.this) || !isFingerPrintEnabled()) {
                            for (int i = 0; i < vfUnlockMethods.getChildCount(); i++) {
                                if (vfUnlockMethods.getChildAt(i).getId() == R.id.cv_fingerprint) {
                                    vfUnlockMethods.removeViewAt(i);
                                    break;
                                }
                            }
                        }

                        applyTheme();
                        setListeners();
                    }
                });
            }
        }).start();
    }

    public void applyTheme() {
        if (preferencesUtil.getString(settingsPreferences, R.string.background, null) != null) {
            Integer glassColor = ContextCompat.getColor(UBlockScreenActivity.this, R.color.glass_25);

            cvEtPin.setCardElevation(0);
            cvFingerprint.setCardElevation(0);
            cvPattern.setCardElevation(0);
            cvPin.setCardElevation(0);

            fabFingerprint.setBackgroundTintList(ColorStateList.valueOf(glassColor));
            fabFingerprint.setElevation(0);
            fabFingerprint.setTag("");

            fabSettings.setBackgroundTintList(ColorStateList.valueOf(glassColor));
            fabSettings.setElevation(0);
            fabSettings.setTag("");

            String chosenWallpaper = preferencesUtil.getString(settingsPreferences, R.string.background, null);

            Bitmap bitmap = BitmapFactory.decodeFile(chosenWallpaper);

            getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));

            ThemeUtil.applyTheme(glassColor, cvEtPin, cvFingerprint, cvPattern, cvPin);
        } else {
            getWindow().setBackgroundDrawable(new ColorDrawable(Config.primaryColorDark(this, null)));

            ThemeUtil.applyTheme(Config.primaryColor(this, null), cvEtPin, cvFingerprint, cvPattern, cvPin);
        }
    }

    public boolean isFingerPrintEnabled() {
        try {
            spass.initialize(UBlockScreenActivity.this);
        } catch (SsdkUnsupportedException e) {
            Log.d("Finger", "Exception: " + e);
        } catch (UnsupportedOperationException e) {
            Log.d("Finger", "Fingerprint Service is not supported in the device");
        }

        Boolean isFeatureEnabled = spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);

        if (isFeatureEnabled) {
            spassFingerprint = new SpassFingerprint(UBlockScreenActivity.this);
            log("Fingerprint Service is supported in the device.");
            log("SDK version : " + spass.getVersionName());

            return true;
        } else {
            log("Fingerprint Service is not supported in the device.");

            return false;
        }
    }

    public void setListeners() {
        patternView.setOnPatternListener(new PatternLockView.OnPatternListener() {
            String storedPattern = preferencesUtil.getString(settingsPreferences, R.string.user_pattern, R.string.default_pattern);

            @Override
            public void onPatternDetected(List<PatternLockView.Cell> pattern, String SimplePattern) {
                super.onPatternDetected(pattern, SimplePattern);

                if (SimplePattern.equals(storedPattern)) {
                    playUnlockSound();

                    preferencesUtil.putValue(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());

                    if (openSettings) {
                        Intent applicationListIntent = new Intent(UBlockScreenActivity.this, ApplicationsListActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putBoolean(getString(R.string.was_open_from_ublock_screen), true);

                        applicationListIntent.putExtras(bundle);

                        Util.openInverse(UBlockScreenActivity.this, applicationListIntent, false);
                    } else {
                        services.unLockApp(getIntent().getStringExtra(getString(R.string.activityOnTop)));
                        Util.closeInverse(UBlockScreenActivity.this, true);
                    }
                } else {
                    vibrator.vibrate(Constants.DURATIONS_OF_ANIMATIONS);

                    patternView.setDisplayMode(PatternLockView.DisplayMode.Wrong);
                    YoYo.with(Techniques.Tada).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(patternView);

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
                    }
                } else if (pinButtonEnum == PinButtonEnum.BUTTON_DONE) {
                    if (etPin.getText().toString().equals(storedPin)) {
                        playUnlockSound();

                        preferencesUtil.putValue(settingsPreferences, R.string.last_unlock_method, vfUnlockMethods.getDisplayedChild());

                        if (openSettings) {
                            Intent applicationListIntent = new Intent(UBlockScreenActivity.this, ApplicationsListActivity.class);

                            Bundle bundle = new Bundle();
                            bundle.putBoolean(getString(R.string.was_open_from_ublock_screen), true);

                            applicationListIntent.putExtras(bundle);

                            Util.openInverse(UBlockScreenActivity.this, applicationListIntent, false);
                        } else {
                            services.unLockApp(getIntent().getStringExtra(getString(R.string.activityOnTop)));
                            Util.closeInverse(UBlockScreenActivity.this, true);
                        }
                    } else {
                        YoYo.with(Techniques.Tada).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etPin);
                    }
                } else {
                    String pinValue = String.valueOf(pinButtonEnum.getButtonValue());
                    etPin.setText(etPin.getText().toString().concat(pinValue));
                }
            }
        });

        vfUnlockMethods.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !gestureDetector.onTouchEvent(event);
            }
        });
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
            if (!spassFingerprint.hasRegisteredFinger()) {
                log("Please register finger first");
            } else {
                if (!onReadyIdentify) {
                    onReadyIdentify = true;
                    try {
                        spassFingerprint.startIdentifyWithDialog(UBlockScreenActivity.this, listener, false);
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
            if (!spassFingerprint.hasRegisteredFinger()) {
                log("Please register finger first");
            } else {
                if (!onReadyIdentify) {
                    onReadyIdentify = true;
                    if (spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_FINGER_INDEX)) {
                        ArrayList<Integer> designatedFingers = new ArrayList<>();
                        designatedFingers.add(preferencesUtil.getInt(settingsPreferences, R.string.designated_finger, 1));
                        try {
                            spassFingerprint.setIntendedFingerprintIndex(designatedFingers);
                        } catch (IllegalStateException ise) {
                            log(ise.getMessage());
                        }
                    }
                    try {
                        spassFingerprint.startIdentifyWithDialog(UBlockScreenActivity.this, listener, false);
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

    public void log(String log) {
        Log.d(TAG, log);
    }

    class StartVariablesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getIntent().getStringExtra(getString(R.string.activityOnTop)), 0);
                        ivAppIcon.setImageDrawable(applicationInfo.loadIcon(getPackageManager()));
                        tvAppName.setText(applicationInfo.loadLabel(getPackageManager()));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        }
    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            System.out.println(" in onFling() :: ");

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && isSwipeEnabled) {
                vfUnlockMethods.setInAnimation(UBlockScreenActivity.this, R.anim.left_in);
                vfUnlockMethods.setOutAnimation(UBlockScreenActivity.this, R.anim.left_out);
                vfUnlockMethods.showNext();

                displayInput(vfUnlockMethods.getCurrentView().getId());
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && isSwipeEnabled) {
                vfUnlockMethods.setInAnimation(UBlockScreenActivity.this, R.anim.right_in);
                vfUnlockMethods.setOutAnimation(UBlockScreenActivity.this, R.anim.right_out);
                vfUnlockMethods.showPrevious();

                displayInput(vfUnlockMethods.getCurrentView().getId());
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}