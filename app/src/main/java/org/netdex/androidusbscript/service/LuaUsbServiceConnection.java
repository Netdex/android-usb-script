package org.netdex.androidusbscript.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class LuaUsbServiceConnection implements ServiceConnection {
    private LuaUsbService service_;

    public LuaUsbServiceConnection() {
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service_ = ((LuaUsbService.Binder) binder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service_ = null;
    }

    public LuaUsbService getService(){
        return service_;
    }
}
