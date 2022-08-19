package org.netdex.androidusbscript.task;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.netdex.androidusbscript.configfs.UsbGadget;

import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/16/2017.
 */

public class LuaUsbTask implements Runnable {

    private final String name_;
    private final String src_;
    private final LuaIOBridge ioBridge_;
    private Shell.Threaded su_;

    public LuaUsbTask(String name, String src, LuaIOBridge ioBridge) {
        this.name_ = name;
        this.src_ = src;
        this.ioBridge_ = ioBridge;
    }

    @Override
    public void run() {
        try {
            su_ = Shell.Pool.SU.get();

            UsbGadget usbGadget;
            usbGadget = new UsbGadget("hidf", "/config");

            try {
                ioBridge_.onLogMessage("<b>-- Started <i>" + name_ + "</i></b>");

                try {
                    Globals globals = JsePlatform.standardGlobals();
                    globals.load(new StringReader("package.path = '/assets/scripts/?.lua;'"),
                            "initAndroidPath").call();
                    LuaUsbLibrary luaUsbLibrary = new LuaUsbLibrary(su_, usbGadget, ioBridge_);
                    luaUsbLibrary.bind(globals);
                    LuaValue luaChunk_ = globals.load(src_);
                    luaChunk_.call();
                } catch (LuaError e) {
                    if (e.getCause() instanceof Shell.ShellDiedException) {
                        throw (Shell.ShellDiedException) e.getCause();
                    } else if (!(e.getCause() instanceof InterruptedException)) {
                        e.printStackTrace();
                        ioBridge_.onLogMessage("<b>LuaError:</b> " +
                                e.getMessage().replace("\n", "<br>"));
                    }
                }
            } finally {
                ioBridge_.onLogMessage("<b>-- Ended <i>" + name_ + "</i></b>");

                if (usbGadget.isBound(su_)) {
                    usbGadget.unbind(su_);
                }
                if (usbGadget.isCreated(su_)) {
                    usbGadget.remove(su_);
                }
            }
        } catch (Shell.ShellDiedException e) {
            if (su_ == null) {
                ioBridge_.onLogMessage("<b>Could not obtain superuser privileges!</b>");
            } else {
                ioBridge_.onLogMessage("<b>SU shell has unexpectedly died!</b>");
            }
        } finally {
            if (su_ != null) {
                su_.close();
            }
        }
    }

    public String getName() {
        return name_;
    }
}