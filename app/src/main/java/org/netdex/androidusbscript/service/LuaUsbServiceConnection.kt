package org.netdex.androidusbscript.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

open class LuaUsbServiceConnection : ServiceConnection {
    var service: LuaUsbService? = null
        private set

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = (binder as LuaUsbService.Binder).service
    }

    override fun onServiceDisconnected(name: ComponentName) {
        service = null
    }
}
