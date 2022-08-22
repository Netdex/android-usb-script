package org.netdex.androidusbscript.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfirmDialog {

    final AlertDialog.Builder builder_;
    final CountDownLatch latch_ = new CountDownLatch(1);
    final AtomicBoolean result_ = new AtomicBoolean(false);

    public ConfirmDialog(Context context, String title, String message) {
        builder_ = new AlertDialog.Builder(context);
        builder_.setTitle(title);
        if (!message.isEmpty())
            builder_.setMessage(message);

        builder_.setPositiveButton("Yes", (dialog, which) -> {
            result_.set(true);
            latch_.countDown();
        });
        builder_.setNegativeButton("No", (dialog, which) -> {
            result_.set(false);
            latch_.countDown();
        });
        builder_.setOnCancelListener(dialog -> {
            result_.set(false);
            latch_.countDown();
        });
        builder_.setCancelable(false);
    }

    public boolean show() {
        Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
        try {
            mainThreadHandler.post(builder_::show);
            latch_.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result_.get();
    }
}
