package org.netdex.hidfuzzer.ltask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.netdex.hidfuzzer.R;

/**
 * Created by netdex on 12/30/17.
 */

public class AsyncIOBridge {

    public enum Signal {
        DONE
    }

    public AsyncIOBridge() {
    }

    public void onLogMessage(String s) {
    }

    public void onLogClear() {
    }

    public void onSignal(Signal signal) {
    }

    public boolean onConfirm(String title, String prompt) {
        return false;
    }

    public String onPrompt(String title, String def) {
        return "";
    }

}
