package com.arrg.app.ublock.controller;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.arrg.app.ublock.R;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/*
 * Created by albert on 7/01/2016.
 */

@ReportsCrashes(
        mailTo = "alberto9.24.93@gmail.com",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.AVAILABLE_MEM_SIZE,
                ReportField.BUILD_CONFIG,
                ReportField.THREAD_DETAILS,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT
        },
        mode = ReportingInteractionMode.DIALOG,
        resDialogIcon = R.drawable.play_store_icon,
        resToastText = R.string.crash_toast_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        resDialogEmailPrompt = R.string.crash_user_email_label,
        resDialogOkToast = R.string.crash_dialog_ok_toast
)

public class UBlockApplication extends Application {

    private static UBlockApplication uBlock;

    public Boolean onReadyIdentify = false;
    public Boolean onReadyEnroll = false;
    public Boolean isFeatureEnabled = false;
    public Spass spass;
    public SpassFingerprint spassFingerprint;
    public SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            log("identify finished : reason=" + getEventStatusName(eventStatus));

            onReadyIdentify = false;

            int FingerprintIndex = 0;

            try {
                FingerprintIndex = spassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
                log(ise.getMessage());
            }

            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                log("onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                log("onFinished() : Password authentification Success");
            } else {
                log("onFinished() : Authentification Fail for identify");
            }
        }

        @Override
        public void onReady() {
            log("identify state is ready");
        }

        @Override
        public void onStarted() {
            log("User touched fingerprint sensor!");
        }
    };
    public SpassFingerprint.RegisterListener mRegisterListener = new SpassFingerprint.RegisterListener() {
        @Override
        public void onFinished() {
            onReadyEnroll = false;
            Log.d("Finger", "RegisterListener.onFinished()");
        }
    };

    public static String getEventStatusName(int eventStatus) {
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

    public UBlockApplication getInstance() {
        return uBlock;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        log("OnCreate Application");

        init();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        log("onTerminate Application");
    }

    public void init() {
        uBlock = this;

        spass = new Spass();

        try {
            spass.initialize(this);
        } catch (SsdkUnsupportedException e) {
            log("Exception: " + e);
        } catch (UnsupportedOperationException e) {
            log("Fingerprint Service is not supported in the device");
        }

        isFeatureEnabled = spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);

        if (isFeatureEnabled) {
            spassFingerprint = new SpassFingerprint(this);
            log("Fingerprint Service is supported in the device.");
            log("SDK version : " + spass.getVersionName());
        } else {
            log("Fingerprint Service is not supported in the device.");
        }
    }

    public boolean isFingerPrintEnabled() {
        return spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
    }

    public void log(String message) {
        Log.d(getClass().getSimpleName(), message);
    }
}
