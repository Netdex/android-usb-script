package org.netdex.androidusbscript.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.topjohnwu.superuser.nio.FileSystemManager;

import static org.netdex.androidusbscript.MainActivity.TAG;

public class RootServiceConnection implements ServiceConnection {
    private FileSystemManager remoteFS_ = null;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            remoteFS_ = FileSystemManager.getRemote(service);
        } catch (RemoteException e) {
            Log.e(TAG, "Remote Error", e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        remoteFS_ = null;
    }

    public FileSystemManager getRemoteFS() {
        return remoteFS_;
    }
}
