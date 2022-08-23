package org.netdex.androidusbscript.service;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static org.netdex.androidusbscript.MainActivity.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.util.Log;

import com.topjohnwu.superuser.ipc.RootService;

import org.netdex.androidusbscript.MainActivity;
import org.netdex.androidusbscript.NotificationBroadcastReceiver;
import org.netdex.androidusbscript.R;
import org.netdex.androidusbscript.task.LuaUsbTask;
import org.netdex.androidusbscript.util.FileSystem;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class LuaUsbService extends Service {

    public static class Binder extends android.os.Binder {
        private final LuaUsbService service_;

        public Binder(LuaUsbService service) {
            this.service_ = service;
        }

        public LuaUsbService getService() {
            return service_;
        }
    }

    public interface Callback {
        void onTaskCompleted(LuaUsbTask task);
    }

    private static final String CHANNEL_ID = LuaUsbService.class.getName();
    public static final int ONGOING_NOTIFICATION_ID = 1;
    private NotificationManager notificationManager_;

    private final ExecutorService executorService_ = Executors.newSingleThreadExecutor();
    private LuaUsbTask activeTask_ = null;
    private Callback callback_;

    private RootServiceConnection rootSvcConn_;
    private FileSystem fs_;

    public LuaUsbService() {
        rootSvcConn_ = new RootServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                super.onServiceConnected(name, service);
                fs_ = new FileSystem(this.getRemoteFS());
            }
        };
    }

    public boolean submitTask(LuaUsbTask task) {
        Log.v(TAG, "LuaUsbService.submitTask()");
        synchronized (this) {
            if (activeTask_ != null) return false;
            activeTask_ = task;
            executorService_.execute(() -> run(task));
        }
        if (notificationManager_ != null)
            notificationManager_.notify(ONGOING_NOTIFICATION_ID, getNotification(task));
        return true;
    }

    public void setCallback(Callback callback) {
        callback_ = callback;
    }

    public void stopActiveTask() {
        Log.v(TAG, "LuaUsbService.stopActiveTask()");
        synchronized (this) {
            if (activeTask_ == null) return;
            try {
                activeTask_.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void run(LuaUsbTask task) {
        task.run(fs_);
        synchronized (this) {
            activeTask_ = null;
        }
        onTaskCompleted(task);
    }

    private void onTaskCompleted(LuaUsbTask task) {
        Log.v(TAG, "LuaUsbService.onTaskCompleted()");
        if (notificationManager_ != null)
            notificationManager_.notify(ONGOING_NOTIFICATION_ID, getNotification(null));
        callback_.onTaskCompleted(task);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "LuaUsbService.onCreate()");
        return new Binder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "LuaUsbService.onUnbind()");
        return false;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "LuaUsbService.onCreate()");
        Intent intent = new Intent(this, RootFileSystemService.class);
        RootService.bind(intent, rootSvcConn_);

        createNotificationChannel();
        startForeground(ONGOING_NOTIFICATION_ID, getNotification(null));
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "LuaUsbService.onDestroy()");
        notificationManager_ = null;
        if (rootSvcConn_ != null) {
            RootService.unbind(rootSvcConn_);
            rootSvcConn_ = null;
        }
        executorService_.shutdownNow();
        try {
            if (!executorService_.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Lua interpreter thread hang");
            }
        } catch (InterruptedException ignored) {
            throw new IllegalStateException("Executor shutdown was interrupted");
        }
    }

    private Notification getNotification(LuaUsbTask task) {
        String contextText;
        if (task == null) {
            contextText = getString(R.string.service_notif_message_idle);
        } else {
            contextText = getString(R.string.service_notif_message, task.getName());
        }
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(NotificationBroadcastReceiver.ACTION_STOP);
        stopIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
        PendingIntent stopPendingIntent =
                PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.service_notif_title))
                .setContentText(contextText)
                .setSmallIcon(R.drawable.ic_baseline_usb_24)
                .setContentIntent(contentPendingIntent);
        if (task != null) {
            builder.addAction(new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_baseline_usb_24),
                    getString(R.string.cancel),
                    stopPendingIntent).build());
        }

        return builder.build();
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.notif_channel_name);
        String description = getString(R.string.notif_channel_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager_ = getSystemService(NotificationManager.class);
        notificationManager_.createNotificationChannel(channel);
    }
}