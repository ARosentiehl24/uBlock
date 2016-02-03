package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

public class FingerprintPreferenceSettingsActivity extends ATEActivity {

    private static FingerprintPreferenceSettingsActivity fingerprintSettings = null;
    private Boolean onReadyEnroll = false;
    private SpassFingerprint mSpassFingerprint;
    private SpassFingerprint.RegisterListener mRegisterListener = new SpassFingerprint.RegisterListener() {
        @Override
        public void onFinished() {
            onReadyEnroll = false;
            Log.d("Finger", "RegisterListener.onFinished()");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_preference_settings);

        Util.hideActionBarUp(this, false);

        fingerprintSettings = this;

        startFingerPrintSensor();

        PreferenceManager.setDefaultValues(this, R.xml.fingerprint_settings_preference, false);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.container, new FingerprintFragment()).commit();
        } else {
            FingerprintFragment fingerprintFragment = (FingerprintFragment) getFragmentManager().findFragmentById(R.id.container);
            if (fingerprintFragment != null) {
                fingerprintFragment.setupPreferences();
            }
        }
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

    public void startFingerPrintSensor() {
        Spass mSpass = new Spass();

        try {
            mSpass.initialize(this);
        } catch (SsdkUnsupportedException e) {
            Log.d("Finger", "Exception: " + e);
        } catch (UnsupportedOperationException e) {
            Log.d("Finger", "Fingerprint Service is not supported in the device");
        }

        boolean isFeatureEnabled = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);

        if (isFeatureEnabled) {
            this.mSpassFingerprint = new SpassFingerprint(this);
            Log.d("Finger", "Fingerprint Service is supported in the device.");
            Log.d("Finger", "SDK version : " + mSpass.getVersionName());
        } else {
            Log.d("Finger", "Fingerprint Service is not supported in the device.");
        }
    }

    public void registerNewFingerprint() {
        try {
            if (!onReadyEnroll) {
                onReadyEnroll = true;
                mSpassFingerprint.registerFinger(this, mRegisterListener);
                Log.d("Finger", "Jump to the Enroll screen");
            } else {
                Log.d("Finger", "Please wait and try to register again");
            }
        } catch (UnsupportedOperationException e) {
            Log.d("Finger", "Fingerprint Service is not supported in the device");
        }
    }

    //arcturianos

    public static class FingerprintFragment extends PreferenceFragment {

        private CharSequence[] fingerIndexEntries;
        private FingerprintPreferenceSettingsActivity fingerprintPreferenceSettingsActivity;
        private Integer designatedFinger;
        private Preference fingerprintIndex;
        private Preference numberOfFingerprint;
        private Preference registerFingerprint;
        private SharedPreferences settingsPreferences;
        private SharedPreferencesUtil preferencesUtil;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fingerprint_settings_preference);

            setupSharedPreferences();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (view != null) {
                ListView lv = (ListView) view.findViewById(android.R.id.list);
                lv.setPadding(0, 0, 0, 0);
            }
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupPreferences();
        }

        public void setupSharedPreferences() {
            preferencesUtil = new SharedPreferencesUtil(getActivity());
            settingsPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
        }

        public void setupPreferences() {
            fingerprintPreferenceSettingsActivity = FingerprintPreferenceSettingsActivity.fingerprintSettings;

            Integer numberOfFingerprintRegistered = fingerprintPreferenceSettingsActivity.mSpassFingerprint.getRegisteredFingerprintName().size();
            designatedFinger = preferencesUtil.getInt(settingsPreferences, R.string.designated_finger, 0);

            fingerIndexEntries = new CharSequence[numberOfFingerprintRegistered + 1];
            fingerIndexEntries[0] = getString(R.string.anyone);

            try {
                log("=Fingerprint Name=");
                SparseArray mList = fingerprintPreferenceSettingsActivity.mSpassFingerprint.getRegisteredFingerprintName();
                if (mList == null) {
                    log("Registered fingerprint is not existed.");
                } else {
                    for (int i = 0; i < mList.size(); i++) {
                        int index = mList.keyAt(i);
                        String name = ((String) mList.get(index));
                        fingerIndexEntries[i + 1] = name;
                        log("index " + index + ", Name is " + name);
                    }
                }
            } catch (UnsupportedOperationException e) {
                log("Fingerprint Service is not supported in the device");
            }

            numberOfFingerprint = findPreference(getString(R.string.number_of_fingerprints_registered_key));
            numberOfFingerprint.setSummary(getString(R.string.number_of_fingerprints_registered_description) + " " + numberOfFingerprintRegistered);

            fingerprintIndex = findPreference(getString(R.string.fingerprint_index_to_unlock_key));
            fingerprintIndex.setSummary(getString(R.string.fingerprint_index_to_unlock_description) + " " + (designatedFinger == 0 ? getString(R.string.anyone) : designatedFinger));
            fingerprintIndex.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.fingerprint_index_to_unlock)
                            .items(fingerIndexEntries)
                            .itemsCallbackSingleChoice(designatedFinger, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    designatedFinger = which;

                                    preference.setSummary(getString(R.string.fingerprint_index_to_unlock_description) + " " + (which == 0 ? getString(R.string.anyone) : which));
                                    preferencesUtil.putValue(settingsPreferences, R.string.designated_finger, which);

                                    return true;
                                }
                            })
                            .positiveText(android.R.string.yes)
                            .typeface(Util.getTypeface(getActivity(), preferencesUtil, settingsPreferences), Util.getTypeface(getActivity(), preferencesUtil, settingsPreferences))
                            .build()
                            .show();
                    return false;
                }
            });

            registerFingerprint = findPreference(getString(R.string.register_new_fingerprint_key));
            registerFingerprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    fingerprintPreferenceSettingsActivity.registerNewFingerprint();
                    return false;
                }
            });
        }

        public void log(String log) {
            String TAG = "FingerPrint";
            Log.d(TAG, log);
        }
    }
}
