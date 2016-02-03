package com.arrg.app.ublock.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.afollestad.appthemeengine.ATEActivity;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.adapters.FontAdapter;
import com.arrg.app.ublock.model.Font;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.uviews.DividerItemDecoration;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FontActivity extends ATEActivity {

    @Bind(R.id.rv_fonts)
    RecyclerView rvFonts;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font);
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
        SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(this);
        SharedPreferences settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        FontAdapter fontAdapter = new FontAdapter(this, fontArrayList(), settingsPreferences, preferencesUtil);

        rvFonts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        rvFonts.setAdapter(fontAdapter);
        rvFonts.setHasFixedSize(true);
        rvFonts.setLayoutManager(new LinearLayoutManager(this));
    }

    public ArrayList<Font> fontArrayList() {
        ArrayList<Font> fonts = new ArrayList<>();

        fonts.add(new Font("ComingSoon"));
        fonts.add(new Font("DancingScript"));
        fonts.add(new Font("DroidSans"));
        fonts.add(new Font("Follow"));
        fonts.add(new Font("Roboto"));
        fonts.add(new Font("Samsungsans"));
        fonts.add(new Font("Segan"));
        fonts.add(new Font("SouciSansNF"));
        fonts.add(new Font("Teen"));
        fonts.add(new Font("Wind"));

        return fonts;
    }
}
