package com.arrg.app.ublock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.arrg.app.ublock.services.UBlockService;

public class UBlockBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, UBlockService.class));
    }
}
