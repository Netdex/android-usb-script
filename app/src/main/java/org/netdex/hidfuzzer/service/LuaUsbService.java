package org.netdex.hidfuzzer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import org.netdex.hidfuzzer.R;
import org.netdex.hidfuzzer.task.LuaUsbTask;

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

    public interface TaskCompletedCallback {
        void onTaskCompleted();
    }

    private static final String CHANNEL_ID = LuaUsbService.class.getName();
    public static final int ONGOING_NOTIFICATION_ID = 1;

    private final ExecutorService executorService_ = Executors.newSingleThreadExecutor();

    public LuaUsbService() {

    }

    public void submit(LuaUsbTask task, TaskCompletedCallback callback) {
        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle(getText(R.string.service_notif_title))
                        .setContentText(getString(R.string.service_notif_message, task.getName()))
                        .setSmallIcon(R.drawable.ic_baseline_usb_24)
                        .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);

        executorService_.execute(() -> {
            task.run();
            stopForeground(true);
            callback.onTaskCompleted();
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        executorService_.shutdownNow();
        try {
            executorService_.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO handle more gracefully
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onCreate() {
        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
}