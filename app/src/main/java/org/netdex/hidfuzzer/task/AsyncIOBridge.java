package org.netdex.hidfuzzer.task;

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
