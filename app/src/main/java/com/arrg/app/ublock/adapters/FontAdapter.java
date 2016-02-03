package com.arrg.app.ublock.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.model.Font;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/*
 * Created by albert on 3/01/2016.
 */
public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {

    private AppCompatActivity activity;
    private ArrayList<Font> fontArrayList;
    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;

    public FontAdapter(AppCompatActivity activity, ArrayList<Font> fontArrayList, SharedPreferences settingsPreferences, SharedPreferencesUtil preferencesUtil) {
        this.activity = activity;
        this.fontArrayList = fontArrayList;
        this.settingsPreferences = settingsPreferences;
        this.preferencesUtil = preferencesUtil;
    }

    @Override
    public FontAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        View fonts = inflater.inflate(R.layout.font_list_row, parent, false);

        return new ViewHolder(fonts);
    }

    @Override
    public void onBindViewHolder(FontAdapter.ViewHolder holder, int position) {
        Font font = fontArrayList.get(position);

        holder.font.setText(font.getFontName());
        holder.font.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/" + font.getFontName() + ".ttf"));
    }

    @Override
    public int getItemCount() {
        return fontArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.tv_font_name)
        TextView font;

        public ViewHolder(View itemView) {
            super(itemView);
            ATE.apply(itemView, null);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Font font = fontArrayList.get(getLayoutPosition());

            preferencesUtil.putValue(settingsPreferences, R.string.selected_font, font.getFontName());

            Util.restartApp(activity);
        }
    }
}
