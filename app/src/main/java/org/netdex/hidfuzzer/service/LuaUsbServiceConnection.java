package org.netdex.hidfuzzer.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.netdex.hidfuzzer.MainActivity;
import org.netdex.hidfuzzer.task.LuaUsbTask;

public class LuaUsbServiceConnection implements ServiceConnection {

    private LuaUsbTask task_;
    private LuaUsbService.TaskCompletedCallback callback_;

    public LuaUsbServiceConnection(LuaUsbTask task, LuaUsbService.TaskCompletedCallback callback) {
        this.task_ = task;
        this.callback_ = callback;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        LuaUsbService service = ((LuaUsbService.Binder) binder).getService();
        service.submit(task_, callback_);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
