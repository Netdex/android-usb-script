package org.netdex.androidusbscript.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.topjohnwu.superuser.ipc.RootService;

import org.netdex.androidusbscript.R;
import org.netdex.androidusbscript.task.LuaUsbTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.netdex.androidusbscript.MainActivity.TAG;

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

    public interface TaskCompletedCallback {
        void onTaskCompleted();
    }

    private static final String CHANNEL_ID = LuaUsbService.class.getName();
    public static final int ONGOING_NOTIFICATION_ID = 1;

    private final ExecutorService executorService_ = Executors.newSingleThreadExecutor();
    private LuaUsbTask activeTask_ = null;
    private RootServiceConnection rootSvcConn_;

    public LuaUsbService() {
        rootSvcConn_ = new RootServiceConnection();
    }

    public void submit(LuaUsbTask task, TaskCompletedCallback callback) {
        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle(getText(R.string.service_notif_title))
                        .setContentText(getString(R.string.service_notif_message, task.getName()))
                        .setSmallIcon(R.drawable.ic_baseline_usb_24)
                        .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        executorService_.execute(() -> run(task, callback));
    }

    public void run(LuaUsbTask task, TaskCompletedCallback callback) {
        if (activeTask_ != null)
            throw new IllegalStateException("Previous task did not terminate");
        activeTask_ = task;
        task.run();
        activeTask_ = null;
        callback.onTaskCompleted();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (activeTask_ != null) activeTask_.setCancelled(true);
        executorService_.shutdownNow();
        try {
            if (executorService_.awaitTermination(5, TimeUnit.SECONDS)) {
                return false;
            }
        } catch (InterruptedException ignored) {
        }
        activeTask_.terminate();
        throw new IllegalStateException("Interpreter thread is hung");
    }

    @Override
    public void onCreate() {
        Intent intent = new Intent(this, RootFileSystemService.class);
        RootService.bind(intent, rootSvcConn_);

        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        if (rootSvcConn_ != null) {
            RootService.unbind(rootSvcConn_);
        }
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.notif_channel_name);
        String description = getString(R.string.notif_channel_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}