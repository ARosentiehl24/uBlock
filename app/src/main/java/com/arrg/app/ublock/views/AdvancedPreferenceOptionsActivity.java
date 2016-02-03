package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.uviews.USwitchPreference;

public class AdvancedPreferenceOptionsActivity extends ATEActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_preference_options);

        Util.hideActionBarUp(this, false);

        PreferenceManager.setDefaultValues(this, R.xml.advanced_options_preferences, false);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.container, new AdvancedOptionsFragment()).commit();
        } else {
            AdvancedOptionsFragment optionsFragment = (AdvancedOptionsFragment) getFragmentManager().findFragmentById(R.id.container);
            if (optionsFragment != null) {
                optionsFragment.setupPreferences();
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

    public static class AdvancedOptionsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private SharedPreferences settingsPreferences;
        private SharedPreferencesUtil preferencesUtil;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.advanced_options_preferences);

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

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            preferencesUtil.putValue(settingsPreferences, preference.getKey(), Boolean.valueOf(newValue.toString()));

            if (preference.getKey().equals(getString(R.string.enable_icon_on_app_drawer))) {
                if (!((Boolean) newValue)) {
                    showDialogMessage(R.string.icon_on_app_drawer_message, new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Util.startHomeScreenActivity(getActivity());
                        }
                    });
                }
                applicationOnAppDrawer((Boolean) newValue);
            } else if (preference.getKey().equals(getString(R.string.enable_notification_on_status_bar))) {
                notificationOnStatusBar((Boolean) newValue);
            }

            return true;
        }

        public void setupSharedPreferences() {
            preferencesUtil = new SharedPreferencesUtil(getActivity());
            settingsPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
        }

        private void setupPreferences() {
            USwitchPreference lockAppsAfterScreenOff = (USwitchPreference) findPreference(getString(R.string.lock_apps_after_screen_off));
            lockAppsAfterScreenOff.setOnPreferenceChangeListener(this);

            USwitchPreference swipeOnUBlockScreen = (USwitchPreference) findPreference(getString(R.string.enable_swipe_on_ublock_screen));
            swipeOnUBlockScreen.setOnPreferenceChangeListener(this);


            USwitchPreference fingerprintOnTop = (USwitchPreference) findPreference(getString(R.string.user_fingerprint));
            fingerprintOnTop.setOnPreferenceChangeListener(this);

            if (!Util.isSamsungDevice(getActivity()) || !Util.isFingerprintEnabled(getActivity())) {
                fingerprintOnTop.setEnabled(false);
                fingerprintOnTop.setPersistent(false);
                fingerprintOnTop.setShouldDisableView(true);
                fingerprintOnTop.setSummary(getString(R.string.fingerprint_service_is_not_supported));
                fingerprintOnTop.setSummaryOn(getString(R.string.fingerprint_service_is_not_supported));
                fingerprintOnTop.setSummaryOff(getString(R.string.fingerprint_service_is_not_supported));
            }

            USwitchPreference iconOnAppDrawer = (USwitchPreference) findPreference(getString(R.string.enable_icon_on_app_drawer));
            iconOnAppDrawer.setOnPreferenceChangeListener(this);

            USwitchPreference dialogWhenNewAppAreInstalled = (USwitchPreference) findPreference(getString(R.string.show_dialog_when_new_apps_are_installed));
            dialogWhenNewAppAreInstalled.setOnPreferenceChangeListener(this);

            USwitchPreference notificationOnStatusBar = (USwitchPreference) findPreference(getString(R.string.enable_notification_on_status_bar));
            notificationOnStatusBar.setOnPreferenceChangeListener(this);

            USwitchPreference isPatternVisible = (USwitchPreference) findPreference(getString(R.string.is_pattern_visible));
            isPatternVisible.setOnPreferenceChangeListener(this);
        }

        public void applicationOnAppDrawer(boolean isChecked) {
            if (isChecked) {
                Intent intent = new Intent(Constants.PACKAGE_NAME + ".SHOW_APPLICATION");
                getActivity().sendBroadcast(intent);
            } else {
                Intent intent = new Intent(Constants.PACKAGE_NAME + ".HIDE_APPLICATION");
                getActivity().sendBroadcast(intent);
            }
        }

        public void notificationOnStatusBar(boolean isChecked) {
            if (isChecked) {
                Intent intent = new Intent(Constants.PACKAGE_NAME + ".ENABLE_NOTIFICATION");
                getActivity().sendBroadcast(intent);
            } else {
                Intent intent = new Intent(Constants.PACKAGE_NAME + ".DISABLE_NOTIFICATION");
                getActivity().sendBroadcast(intent);
            }
        }

        public void showDialogMessage(int message, MaterialDialog.SingleButtonCallback buttonCallback) {
            new MaterialDialog.Builder(getActivity()).title(android.R.string.dialog_alert_title).content(message).cancelable(false).positiveText(android.R.string.ok).onPositive(buttonCallback).show();
        }
    }
}
