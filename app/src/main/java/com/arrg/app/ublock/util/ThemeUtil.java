package com.arrg.app.ublock.util;

/*
 * Created by albert on 23/12/2015.
 */

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.color.CircleView;
import com.arrg.app.ublock.views.uviews.PatternLockView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mattyork.colours.Colour;

public class ThemeUtil {

    public static void applyTheme(Integer color, View... views) {
        for (View view : views) {
            if (view instanceof PatternLockView) {
                ((PatternLockView) view).setmRegularColor(color);
            } else if (view instanceof CardView) {
                ((CardView) view).setCardBackgroundColor(color);
            } else if (view instanceof FloatingActionButton) {
                ((FloatingActionButton) view).setColorNormal(CircleView.shiftColorDown(color));
                ((FloatingActionButton) view).setColorPressed(color);
            } else if (view instanceof FloatingActionMenu) {
                ((FloatingActionMenu) view).setMenuButtonColorNormal(CircleView.shiftColorDown(color));
                ((FloatingActionMenu) view).setMenuButtonColorPressed(color);
            }
        }
    }

    public static void saveTheme(AppCompatActivity activity, Integer color) {
        Config config = ATE.config(activity, null);

        config.primaryColor(color);
        config.primaryColorDark(CircleView.shiftColorDown(color));
        config.accentColor(CircleView.shiftColorDown(color));
        config.navigationBarColor(CircleView.shiftColorDown(color));

        int[] colorSchemeMonochromatic = Colour.colorSchemeOfType(color, Colour.ColorScheme.ColorSchemeMonochromatic);
        int primaryTextColor = Color.argb(225, Color.red(colorSchemeMonochromatic[0]), Color.green(colorSchemeMonochromatic[0]), Color.blue(colorSchemeMonochromatic[0]));
        int secondaryTextColor = Color.argb(150, Color.red(colorSchemeMonochromatic[0]), Color.green(colorSchemeMonochromatic[0]), Color.blue(colorSchemeMonochromatic[0]));

        config.textColorPrimary(primaryTextColor);
        config.textColorSecondary(secondaryTextColor);

        config.coloredActionBar(true);
        config.coloredNavigationBar(true);
        config.coloredStatusBar(true);
        config.usingMaterialDialogs(true);
        config.navigationViewThemed(true);

        config.lightStatusBarMode(Config.LIGHT_STATUS_BAR_AUTO);
        config.lightToolbarMode(Config.LIGHT_TOOLBAR_AUTO);

        config.navigationViewNormalIcon(CircleView.shiftColorDown(color));
        config.navigationViewNormalText(primaryTextColor);
        config.navigationViewSelectedIcon(CircleView.shiftColorDown(color));
        config.navigationViewSelectedText(primaryTextColor);

        config.apply(activity);
    }
}
