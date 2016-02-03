package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.uviews.PatternLockView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PatternSettingsActivity extends ATEActivity {

    private Boolean isValidPattern = false;
    private Integer intents = 1;
    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;
    private String userPattern;
    private Vibrator vibrator;

    @Bind(R.id.b_ok)
    Button bOk;

    @Bind(R.id.pattern)
    PatternLockView patternView;

    @Bind(R.id.tv_pattern_request)
    TextView tvPatternRequest;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @OnClick({R.id.b_ok, R.id.b_reset})
    public void OnClick(View id) {
        switch (id.getId()) {
            case R.id.b_ok:
                if (isValidPattern) {
                    preferencesUtil.putValue(settingsPreferences, R.string.user_pattern, userPattern);
                    Toast.makeText(PatternSettingsActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
                    PatternSettingsActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    PatternSettingsActivity.this.finish();
                }
                break;
            case R.id.b_reset:
                intents = 1;
                isValidPattern = false;
                userPattern = "";

                bOk.setEnabled(false);
                patternView.setEnabled(true);

                YoYo.with(Techniques.FadeOut).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(patternView);
                YoYo.with(Techniques.FadeIn).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(patternView);
                YoYo.with(Techniques.FadeOut).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvPatternRequest);
                YoYo.with(Techniques.FadeIn).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvPatternRequest);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                patternView.clearPattern();
                                tvPatternRequest.setText(R.string.new_pattern_request);
                            }
                        });
                    }
                }, Constants.DURATIONS_OF_ANIMATIONS);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        Util.hideActionBarUp(this, false);

        setupSharedPreferences();
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

    public void setupSharedPreferences() {
        preferencesUtil = new SharedPreferencesUtil(this);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        connectViews();
    }

    public void connectViews() {
        patternView.setmRegularColor(Config.primaryColorDark(this, null));

        setListenersToViews();
    }

    public void setListenersToViews() {
        bOk.setEnabled(isValidPattern);
        userPattern = "";

        patternView.setOnPatternListener(new PatternLockView.OnPatternListener() {
            @Override
            public void onPatternDetected(List<PatternLockView.Cell> pattern, String SimplePattern) {
                super.onPatternDetected(pattern, SimplePattern);

                if (intents == 1) {
                    userPattern = SimplePattern;
                    intents = 2;

                    YoYo.with(Techniques.FadeOut).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(patternView);
                    YoYo.with(Techniques.FadeIn).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(patternView);
                    YoYo.with(Techniques.FadeOut).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvPatternRequest);
                    YoYo.with(Techniques.FadeIn).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvPatternRequest);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    patternView.clearPattern();
                                    tvPatternRequest.setText(R.string.repeat_pattern_request);
                                }
                            });
                        }
                    }, Constants.DURATIONS_OF_ANIMATIONS);
                } else if (intents == 2) {
                    if (userPattern.equals(SimplePattern)) {
                        isValidPattern = true;

                        bOk.setEnabled(true);
                        patternView.setEnabled(false);
                        patternView.setDisplayMode(PatternLockView.DisplayMode.Correct);

                        YoYo.with(Techniques.FadeOut).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvPatternRequest);
                        YoYo.with(Techniques.FadeIn).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvPatternRequest);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvPatternRequest.setText(R.string.pattern_correct_message);
                                    }
                                });
                            }
                        }, Constants.DURATIONS_OF_ANIMATIONS);
                    } else {
                        vibrator.vibrate(Constants.DURATIONS_OF_ANIMATIONS);

                        patternView.setDisplayMode(PatternLockView.DisplayMode.Wrong);
                        YoYo.with(Techniques.Tada).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(patternView);
                        YoYo.with(Techniques.FadeOut).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvPatternRequest);
                        YoYo.with(Techniques.FadeIn).duration(Constants.DURATIONS_OF_ANIMATIONS).delay(Constants.DURATIONS_OF_ANIMATIONS).playOn(tvPatternRequest);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        patternView.clearPattern();
                                        tvPatternRequest.setText(R.string.patterns_doesnt_match_message);
                                    }
                                });
                            }
                        }, Constants.DURATIONS_OF_ANIMATIONS);
                    }
                }
            }
        });
    }
}
