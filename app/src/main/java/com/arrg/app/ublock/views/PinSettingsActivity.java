package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATEActivity;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class PinSettingsActivity extends ATEActivity {

    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;

    @Bind(R.id.etConfirmPin)
    EditText etConfirmPin;

    @Bind(R.id.etPin)
    EditText etPin;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @OnCheckedChanged(R.id.cbPin)
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            etConfirmPin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etPin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            etConfirmPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        etConfirmPin.setSelection(etConfirmPin.length());
        etPin.setSelection(etPin.length());
    }

    @OnClick(R.id.fab)
    public void onClick(View view) {
        if (etPin.getText().toString().length() == 0 || etConfirmPin.getText().toString().length() == 0) {
            YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etConfirmPin);
            YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etPin);
            Toast.makeText(PinSettingsActivity.this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
        } else {
            if (etPin.getText().toString().equals(etConfirmPin.getText().toString())) {
                preferencesUtil.putValue(settingsPreferences, R.string.user_pin, etPin.getText().toString());
                Toast.makeText(PinSettingsActivity.this, R.string.done, Toast.LENGTH_SHORT).show();

                Util.close(PinSettingsActivity.this, true);
            } else {
                YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etConfirmPin);
                YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etPin);
                Toast.makeText(PinSettingsActivity.this, R.string.wrong_pin_is_not_equal, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnEditorAction(R.id.etConfirmPin)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)) {
            if (etPin.getText().toString().length() == 0 || etConfirmPin.getText().toString().length() == 0) {
                YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etConfirmPin);
                YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etPin);
                Toast.makeText(PinSettingsActivity.this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
            } else {
                if (etPin.getText().toString().equals(etConfirmPin.getText().toString())) {
                    preferencesUtil.putValue(settingsPreferences, R.string.user_pin, etPin.getText().toString());
                    Toast.makeText(PinSettingsActivity.this, R.string.done, Toast.LENGTH_SHORT).show();

                    Util.close(PinSettingsActivity.this, true);
                } else {
                    YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etConfirmPin);
                    YoYo.with(Techniques.Shake).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(etPin);
                    Toast.makeText(PinSettingsActivity.this, R.string.wrong_pin_is_not_equal, Toast.LENGTH_SHORT).show();
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_settings);
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
    }
}
