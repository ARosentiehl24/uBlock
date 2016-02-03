package com.arrg.app.ublock.controller;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.PatternSettingsActivity;
import com.arrg.app.ublock.views.PinSettingsActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/*
 * A simple {@link Fragment} subclass.
 */
public class SlideSettingsFragment extends Fragment {

    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;

    public SlideSettingsFragment() {
        // Required empty public constructor
    }

    @OnClick({R.id.btn_pattern, R.id.btn_pin})
    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.btn_pattern:
                Util.open(getActivity(), PatternSettingsActivity.class, false);
                break;
            case R.id.btn_pin:
                Util.open(getActivity(), PinSettingsActivity.class, false);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesUtil = new SharedPreferencesUtil(getActivity());
        settingsPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_slide_settings, container, false);

        ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("Fragment", "OnResume");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ATE.apply(this, null);
    }

    public boolean patternWasConfigured() {
        return (preferencesUtil.getString(settingsPreferences, R.string.user_pattern, null) != null);
    }

    public boolean pinWasConfigured() {
        return (preferencesUtil.getString(settingsPreferences, R.string.user_pin, null) != null);
    }
}
