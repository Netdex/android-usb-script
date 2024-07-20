package org.netdex.androidusbscript.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat
import com.topjohnwu.superuser.ipc.RootService
import org.netdex.androidusbscript.MainActivity
import org.netdex.androidusbscript.NotificationBroadcastReceiver
import org.netdex.androidusbscript.R
import org.netdex.androidusbscript.task.LuaUsbTask
import org.netdex.androidusbscript.util.FileSystem
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LuaUsbService : Service() {
    class Binder(val service: LuaUsbService) : android.os.Binder()

    fun interface Callback {
        fun onTaskCompleted(task: LuaUsbTask?)
    }

    private var notificationManager: NotificationManager? = null

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var activeTask: LuaUsbTask? = null
    private var callback: Callback? = null

    private var rootSvcConn: RootServiceConnection?
    private var fs: FileSystem? = null

    init {
        rootSvcConn = object : RootServiceConnection() {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                super.onServiceConnected(name, service)
                fs = FileSystem(remoteFs!!)
            }
        }
    }

    fun submitTask(task: LuaUsbTask): Boolean {
        Timber.d("Submitting Lua USB task '%s'", task.name)
        synchronized(this) {
            if (activeTask != null) return false
            activeTask = task
            executorService.execute { run(task) }
        }
        if (notificationManager != null)
            notificationManager!!.notify(ONGOING_NOTIFICATION_ID, getNotification(task))
        return true
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    fun stopActiveTask() {
        synchronized(this) {
            if (activeTask == null) return
            try {
                Timber.d("Stopping active Lua USB task '%s'", activeTask!!.name)
                activeTask!!.close()
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
    }

    private fun run(task: LuaUsbTask) {
        task.run(fs)
        synchronized(this) {
            activeTask = null
        }
        onTaskCompleted(task)
    }

    private fun onTaskCompleted(task: LuaUsbTask) {
        Timber.d("Lua USB task '%s' completed", task.name)
        if (notificationManager != null)
            notificationManager!!.notify(ONGOING_NOTIFICATION_ID, getNotification(null))
        callback!!.onTaskCompleted(task)
    }

    override fun onBind(intent: Intent): IBinder? {
        Timber.d("Binding Lua USB service")
        return Binder(this)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.d("Unbinding Lua USB service")
        return false
    }

    override fun onCreate() {
        Timber.d("Creating Lua USB service")
        val intent = Intent(this, RootFileSystemService::class.java)
        RootService.bind(intent, rootSvcConn!!)

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ONGOING_NOTIFICATION_ID,
                getNotification(null),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
            )
        }
    }

    override fun onDestroy() {
        Timber.d("Destroying Lua USB service")
        notificationManager = null
        if (rootSvcConn != null) {
            RootService.unbind(rootSvcConn!!)
            rootSvcConn = null
        }
        executorService.shutdownNow()
        try {
            check(executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                "Lua interpreter thread hang"
            }
        } catch (ignored: InterruptedException) {
            throw IllegalStateException("Executor shutdown was interrupted")
        }
    }

    private fun getNotification(task: LuaUsbTask?): Notification {
        val contextText = if (task == null) {
            getString(R.string.service_notif_message_idle)
        } else {
            getString(R.string.service_notif_message, task.name)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(NotificationBroadcastReceiver.ACTION_STOP)
        stopIntent.putExtra(Notification.EXTRA_NOTIFICATION_ID, 0)
        val stopPendingIntent =
            PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.service_notif_title)).setContentText(contextText)
            .setSmallIcon(R.drawable.ic_baseline_usb_24).setOngoing(true)
            .setContentIntent(contentPendingIntent)
        if (task != null) {
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_baseline_usb_24),
                    getString(R.string.cancel),
                    stopPendingIntent
                ).build()
            )
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        val name: CharSequence = getString(R.string.notif_channel_name)
        val description = getString(R.string.notif_channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
    }

    companion object {
        private val CHANNEL_ID: String = LuaUsbService::class.java.name
        const val ONGOING_NOTIFICATION_ID: Int = 1
    }
}