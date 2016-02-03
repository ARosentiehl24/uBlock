package com.arrg.app.ublock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.services.UBlockService;

/*
 * Created by albert on 13/10/2015.
 */

public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String RESTART_SERVICE = Constants.PACKAGE_NAME + ".RESTART_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(RESTART_SERVICE)) {
            if (!UBlockService.isRunning(context, UBlockService.class)) {
                context.startService(new Intent(context, UBlockService.class));
            }
        }
    }
}
