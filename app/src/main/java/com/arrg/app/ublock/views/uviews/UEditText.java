package com.arrg.app.ublock.views.uviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import com.afollestad.appthemeengine.views.ATEEditText;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;

/*
 * Created by albert on 4/01/2016.
 */
public class UEditText extends ATEEditText {

    public UEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public UEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        if (!isInEditMode()) {
            SharedPreferences settings = context.getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferencesUtil util = new SharedPreferencesUtil(context);

            setTypeface(Util.getTypeface(context, util, settings));
        }
    }
}
