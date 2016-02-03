package com.arrg.app.ublock.views;

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
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.ThemeUtil;
import com.arrg.app.ublock.util.Util;

public class ThemePreferenceActivity extends ATEActivity implements ColorChooserDialog.ColorCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_preference);

        Util.hideActionBarUp(this, false);

        PreferenceManager.setDefaultValues(this, R.xml.advanced_options_preferences, false);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.container, new ThemeFragment()).commit();
        } else {
            ThemeFragment optionsFragment = (ThemeFragment) getFragmentManager().findFragmentById(R.id.container);
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

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                ThemeUtil.saveTheme(this, selectedColor);
                Util.restartApp(ThemePreferenceActivity.this);
                break;
        }
    }

    public static class ThemeFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.theme_settings_preference);
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
            return false;
        }

        private void setupPreferences() {

            ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
            primaryColorPref.setColor(Config.primaryColor(getActivity(), null), CircleView.shiftColorDown(Config.primaryColor(getActivity(), null)));
            primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((ThemePreferenceActivity) getActivity(), R.string.primary_color)
                            .preselect(Config.primaryColor(getActivity(), null))
                            .show();
                    return true;
                }
            });

            ATEColorPreference primaryDarkColorPref = (ATEColorPreference) findPreference("primary_dark_color");
            primaryDarkColorPref.setColor(Config.primaryColorDark(getActivity(), null), CircleView.shiftColorDown(Config.primaryColorDark(getActivity(), null)));

            ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            accentColorPref.setColor(Config.accentColor(getActivity(), null), CircleView.shiftColorDown(Config.accentColor(getActivity(), null)));

            ATEColorPreference primaryTextColorPref = (ATEColorPreference) findPreference("primary_text_color");
            primaryTextColorPref.setColor(Config.textColorPrimary(getActivity(), null), CircleView.shiftColorDown(Config.textColorPrimary(getActivity(), null)));

            ATEColorPreference secondaryTextColorPref = (ATEColorPreference) findPreference("secondary_text_color");
            secondaryTextColorPref.setColor(Config.textColorSecondary(getActivity(), null), CircleView.shiftColorDown(Config.textColorSecondary(getActivity(), null)));
        }
    }
}
