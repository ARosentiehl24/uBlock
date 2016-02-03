package com.arrg.app.ublock.views.uviews;

/*
 * Created by albert on 23/12/2015.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.TextView;

import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;

public class UTextView extends TextView {

    public UTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public UTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UTextView(Context context) {
        super(context);
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