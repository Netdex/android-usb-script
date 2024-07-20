package org.netdex.androidusbscript.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.topjohnwu.superuser.nio.FileSystemManager
import timber.log.Timber

open class RootServiceConnection : ServiceConnection {
    var remoteFs: FileSystemManager? = null
        private set

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        try {
            remoteFs = FileSystemManager.getRemote(service)
        } catch (e: RemoteException) {
            Timber.e(e)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        remoteFs = null
    }
}
