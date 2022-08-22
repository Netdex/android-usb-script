package org.netdex.androidusbscript.task;

import static org.netdex.androidusbscript.MainActivity.TAG;

import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.netdex.androidusbscript.configfs.UsbGadget;
import org.netdex.androidusbscript.util.FileSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;


/**
 * Created by netdex on 1/16/2017.
 */

public class LuaUsbTask {

    private final String name_;
    private final String src_;
    private final LuaIOBridge ioBridge_;

    public LuaUsbTask(String name, String src, LuaIOBridge ioBridge) {
        this.name_ = name;
        this.src_ = src;
        this.ioBridge_ = ioBridge;
    }

    public void run(FileSystem fs) {
        try {
            UsbGadget usbGadget = new UsbGadget("hidf", "/config");

            ioBridge_.onLogMessage("<b>-- Started <i>" + name_ + "</i></b>");
            try {
                try (LuaUsbLibrary luaUsbLibrary = new LuaUsbLibrary(fs, usbGadget, ioBridge_)) {
                    Globals globals = JsePlatform.standardGlobals();
                    globals.load(new StringReader("package.path = '/assets/scripts/?.lua;'"),
                            "initAndroidPath").call();
                    luaUsbLibrary.bind(globals);
                    LuaValue luaChunk_ = globals.load(src_);
                    luaChunk_.call();
                } catch (LuaError e) {
                    if (e.getCause() instanceof IOException) {
                        throw (IOException) e.getCause();
                    } else if (!(e.getCause() instanceof InterruptedException)) {
                        ioBridge_.onLogMessage(getExceptionMessage(e));
                    }
                }
            } finally {
                ioBridge_.onLogMessage("<b>-- Ended <i>" + name_ + "</i></b>");

                if (usbGadget.isCreated(fs)) {
                    if (usbGadget.isBound(fs)) {
                        usbGadget.unbind(fs);
                    } else {
                        Log.w(TAG, "USB gadget is not bound on task end");
                    }
                    usbGadget.remove(fs);
                } else {
                    Log.w(TAG, "USB gadget is not created on task end");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ioBridge_.onLogMessage(getExceptionMessage(e));
        }
    }

    public String getName() {
        return name_;
    }

    private String getExceptionMessage(Exception e) {
        if (e instanceof LuaError) {
            return "<b>" + e.getClass().getName() + "</b>:<br>" + e.getMessage().replace("\n", "<br>");
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrace = sw.toString();
            return "<b>Unhandled exception:</b><br>" + stackTrace.trim().replace("\n", "<br>");
        }
    }
}