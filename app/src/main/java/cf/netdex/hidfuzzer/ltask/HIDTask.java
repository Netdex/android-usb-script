package cf.netdex.hidfuzzer.ltask;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cf.netdex.hidfuzzer.MainActivity;
import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.lua.LuaHIDBinding;
import eu.chainfire.libsuperuser.Shell;

import static cf.netdex.hidfuzzer.MainActivity.TAG;

/**
 * Created by netdex on 1/16/2017.
 */

public class HIDTask extends AsyncTask<Void, HIDTask.RunState, Void> {

    static final String DEV_KEYBOARD = "/dev/hidg0";
    static final String DEV_MOUSE = "/dev/hidg1";

    private WeakReference<Context> mContext;

    // TODO: remove hacky behavior for updating log view
    private DialogIO mUserIO;

    private Shell.Interactive mSU;
    private HIDR mH;
    private LuaHIDBinding mLuaHIDBinding;

    private String mName;
    private String mSrc;
    private Globals mLuaGlobals;
    private LuaValue mLuaChunk;


    public HIDTask(Context context, String name, String src) {
        this.mContext = new WeakReference<>(context);
        this.mName = name;
        this.mSrc = src;

        mUserIO = new DialogIO(context);
    }

    @Override
    protected void onPreExecute() {
        try {
            mLuaHIDBinding = new LuaHIDBinding(this);
            mLuaGlobals = LuaTaskLoader.createGlobals(this);
            mLuaChunk = LuaTaskLoader.loadChunk(mLuaGlobals, mSrc);
        } catch (LuaError e) {
            mUserIO.log("<b>LuaError:</b> " + e.getMessage());
            this.cancel(true);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        // I don't know why but apparently you can't initialize SU shell on UI thread
        mSU = createSU();
        if (mSU == null) {
            mUserIO.log("<b>! failed to obtain su !</b>");
            return null;
        }
        mSU.addCommand("chmod 666 " + DEV_KEYBOARD);
        mSU.addCommand("chmod 666 " + DEV_MOUSE);

        mH = new HIDR(mSU, DEV_KEYBOARD, DEV_MOUSE);
        mUserIO.clear();
        mUserIO.log("<b>-- started <i>" + mName + "</i></b>");
        run();
        mUserIO.log("<b>-- ended <i>" + mName + "</i></b>");
        return null;
    }

    public void progress(RunState s) {
        mUserIO.log(mName + ": <b>" + s.name() + "</b>");
    }

    public void run() {
        try {
            mLuaChunk.call();
        } catch (LuaError e) {
            e.printStackTrace();
            mUserIO.log("<b>LuaError:</b> " + e.getMessage());
        }
    }

    @Override
    protected void onCancelled() {
        cleanup();
    }

    @Override
    protected void onPostExecute(Void result) {
        cleanup();
    }

    private void cleanup() {
        if (mH != null)
            mH.getKeyboardLightListener().kill();
        if (mSU != null) {
            mSU.kill();
            mSU.close();
        }
        // For some reason, you must setChecked before setEnabled or the setEnabled will have no effect
        mUserIO.getModeButton().get().setChecked(false);
        mUserIO.getModeButton().get().setEnabled(true);
    }

    private static Shell.Interactive createSU() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final boolean[] root = new boolean[1];
            Shell.Interactive sh = new Shell.Builder()
                    .useSU()
                    .setWantSTDERR(true)
                    .setWatchdogTimeout(5)
                    .setMinimalLogging(true)
                    .open((commandCode, exitCode, output) -> {
                        if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING) {
                            Log.e(TAG, "Failed to open SU");
                            root[0] = false;
                        } else {
                            root[0] = true;
                        }
                        latch.countDown();
                    });
            latch.await();
            if (!root[0]) return null;
            return sh;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getContext() {
        return mContext.get();
    }

    Shell.Interactive getSU() {
        return mSU;
    }

    public HIDR getHIDR() {
        return mH;
    }

    public DialogIO getIO() {
        return mUserIO;
    }

    public LuaHIDBinding getLuaHIDBinding() {
        return mLuaHIDBinding;
    }

    public enum RunState {
        RUNNING,
        IDLE,
        STOPPED,
        DONE
    }
}