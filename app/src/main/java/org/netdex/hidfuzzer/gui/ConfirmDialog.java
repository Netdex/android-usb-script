package org.netdex.hidfuzzer.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfirmDialog {

    AlertDialog.Builder builder_;
    CountDownLatch latch_ = new CountDownLatch(1);
    AtomicBoolean result_ = new AtomicBoolean(false);

    public ConfirmDialog(Context context, String title, String prompt) {

        builder_ = new AlertDialog.Builder(context);
        builder_.setTitle(title);

        final TextView txtPrompt = new TextView(context);
        txtPrompt.setPadding(40, 40, 40, 40);
        txtPrompt.setText(prompt);
        builder_.setView(txtPrompt);

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
            mainThreadHandler.post(() -> builder_.show());
            latch_.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result_.get();
    }
}
