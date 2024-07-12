package org.netdex.androidusbscript.task;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.netdex.androidusbscript.configfs.UsbGadget;
import org.netdex.androidusbscript.util.FileSystem;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;

import timber.log.Timber;


/**
 * Created by netdex on 1/16/2017.
 */

public class LuaUsbTask implements Closeable {

    private final String name_;
    private final String src_;
    private final LuaIOBridge ioBridge_;

    private LuaUsbLibrary luaUsbLibrary_;
    private Thread taskThread_;

    public LuaUsbTask(String name, String src, LuaIOBridge ioBridge) {
        this.name_ = name;
        this.src_ = src;
        this.ioBridge_ = ioBridge;
    }

    public void run(FileSystem fs) {
        taskThread_ = Thread.currentThread();

        try {
            UsbGadget usbGadget = new UsbGadget("hidf", "/config");

            ioBridge_.onLogMessage("<b>-- Started <i>" + name_ + "</i></b>");
            try {
                try {
                    luaUsbLibrary_ = new LuaUsbLibrary(fs, usbGadget, ioBridge_);
                    Globals globals = JsePlatform.standardGlobals();
                    globals.load(new StringReader("package.path = '/assets/lib/?.lua;'"),
                            "initAndroidPath").call();
                    luaUsbLibrary_.bind(globals);
                    LuaValue luaChunk_ = globals.load(src_);
                    luaChunk_.call();
                } catch (LuaError e) {
                    if (!(e.getCause() instanceof InterruptedException)) {
                        e.printStackTrace();
                        ioBridge_.onLogMessage(getExceptionMessage(e));
                    }
                } finally {
                    if (luaUsbLibrary_ != null) {
                        luaUsbLibrary_.close();
                        luaUsbLibrary_ = null;
                    }
                }
            } finally {
                ioBridge_.onLogMessage("<b>-- Ended <i>" + name_ + "</i></b>");

                if (usbGadget.isCreated(fs)) {
                    if (usbGadget.isBound(fs)) {
                        usbGadget.unbind(fs);
                    } else {
                        Timber.w("USB gadget is not bound on task end");
                    }
                    usbGadget.remove(fs);
                } else {
                    Timber.w("USB gadget is not created on task end");
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
            return "<b>Unhandled exception:</b><br>" + e.toString().replace("\n", "<br>");
        }
    }

    @Override
    public void close() throws IOException {
        if (luaUsbLibrary_ != null) {
            luaUsbLibrary_.close();
            luaUsbLibrary_ = null;
        }
        taskThread_.interrupt();
    }
}