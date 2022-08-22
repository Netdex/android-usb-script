package org.netdex.androidusbscript.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.EditText;

import androidx.core.os.HandlerCompat;

import com.google.android.material.textfield.TextInputLayout;

import org.netdex.androidusbscript.R;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class PromptDialog {

    final AlertDialog.Builder builder_;
    final CountDownLatch latch_ = new CountDownLatch(1);
    final AtomicReference<String> result_ = new AtomicReference<>();

    public PromptDialog(Context context, String title, String message, String hint, String def) {
        result_.set(def);

        builder_ = new AlertDialog.Builder(context);
        builder_.setTitle(title);
        if (!message.isEmpty())
            builder_.setMessage(message);

        final EditText editText = new EditText(context);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setText(def);
        editText.setMaxLines(4);

        TextInputLayout textInputLayout = new TextInputLayout(context);
        textInputLayout.setPadding(
                context.getResources().getDimensionPixelOffset(R.dimen.text_margin), 0,
                context.getResources().getDimensionPixelOffset(R.dimen.text_margin), 0);
        textInputLayout.setHint(hint);
        textInputLayout.addView(editText);

        builder_.setView(textInputLayout);

        builder_.setPositiveButton("OK", (dialog, which) -> {
            result_.set(editText.getText().toString());
            latch_.countDown();
        });
        builder_.setOnCancelListener(dialog -> latch_.countDown());
        builder_.setCancelable(false);
    }

    public String show() {
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
