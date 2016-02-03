package com.arrg.app.ublock.views.uviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;

/*
 * Created by albert on 3/01/2016.
 */
public class UPreference extends Preference {

    public UPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public UPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        SharedPreferences settings = getContext().getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferencesUtil util = new SharedPreferencesUtil(getContext());

        TextView title = (TextView) view.findViewById(android.R.id.title);
        TextView summary = (TextView) view.findViewById(android.R.id.summary);

        title.setTextColor(Config.textColorPrimary(getContext(), null));
        title.setTypeface(Util.getTypeface(getContext(), util, settings));
        summary.setTextColor(Config.textColorSecondary(getContext(), null));
        summary.setTypeface(Util.getTypeface(getContext(), util, settings));
    }
}
