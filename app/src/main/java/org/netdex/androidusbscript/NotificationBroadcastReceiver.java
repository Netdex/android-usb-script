package org.netdex.androidusbscript;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.netdex.androidusbscript.service.LuaUsbService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_STOP = "org.netdex.androidusbscript.ACTION_STOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity mainActivity = (MainActivity) context;
        if (intent.getAction().equals(ACTION_STOP)) {
            LuaUsbService luaUsbService = mainActivity.getLuaUsbService();
            luaUsbService.stopActiveTask();
        }
    }
}
