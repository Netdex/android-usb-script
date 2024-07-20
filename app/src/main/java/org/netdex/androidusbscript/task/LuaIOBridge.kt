package org.netdex.androidusbscript.task;

/**
 * Created by netdex on 12/30/17.
 */

public interface LuaIOBridge {
    void onLogMessage(String s);

    boolean onConfirm(String title, String message);

    String onPrompt(String title, String message, String hint, String def);
}
