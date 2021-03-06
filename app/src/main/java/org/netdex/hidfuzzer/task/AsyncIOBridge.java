package org.netdex.hidfuzzer.task;

/**
 * Created by netdex on 12/30/17.
 */

public interface AsyncIOBridge {
    void onLogMessage(String s);

    boolean onConfirm(String title, String prompt);

    String onPrompt(String title, String def);
}
