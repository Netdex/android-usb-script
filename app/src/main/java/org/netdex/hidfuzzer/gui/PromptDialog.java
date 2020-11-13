package org.netdex.hidfuzzer.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.EditText;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class PromptDialog {

    AlertDialog.Builder builder_;
    CountDownLatch latch_ = new CountDownLatch(1);
    AtomicReference<String> result_ = new AtomicReference<>();

    public PromptDialog(Context context, String title, String def) {
        result_.set(def);

        builder_ = new AlertDialog.Builder(context);
        builder_.setTitle(title);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(def);
        builder_.setView(input);

        builder_.setPositiveButton("OK", (dialog, which) -> {
            result_.set(input.getText().toString());
            latch_.countDown();
        });
        builder_.setOnCancelListener(dialog -> latch_.countDown());
        builder_.setCancelable(false);
    }

    public String show() {
        Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
        try {
            mainThreadHandler.post(() -> builder_.show());
            latch_.await();
        } catch (InterruptedException ignored) {
        }
        return result_.get();
    }

}
