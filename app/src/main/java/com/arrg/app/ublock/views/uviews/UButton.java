package com.arrg.app.ublock.views.uviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.Button;

import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;

/*
 * Created by albert on 30/12/2015.
 */
public class UButton extends Button {

    public UButton(Context context) {
        super(context);
        init(context);
    }

    public UButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
