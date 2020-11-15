package org.netdex.hidfuzzer.task;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.netdex.hidfuzzer.configfs.UsbGadget;
import org.netdex.hidfuzzer.util.Command;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/16/2017.
 */

public class LuaUsbTask implements Runnable {

    private String name_;
    private String src_;
    private AsyncIoBridge aio_;

    public LuaUsbTask(String name, String src, AsyncIoBridge ioBridge) {
        this.name_ = name;
        this.src_ = src;
        this.aio_ = ioBridge;
    }

    @Override
    public void run() {
        aio_.onLogClear();

        Shell.Threaded su = null;

        try {
            su = Shell.Pool.SU.get();

            UsbGadget usbGadget;
            if (Command.pathExists(su, "/config")) {
                usbGadget = new UsbGadget(UsbGadget.Parameters.DEFAULT, "hidfuzzer", "/config");
            } else {
                aio_.onLogMessage("No method exists for accessing hid gadget");
                return;
            }

            try {
                aio_.onLogMessage("<b>-- Started <i>" + name_ + "</i></b>");

                try {
                    Globals globals = JsePlatform.standardGlobals();
                    LuaUsbLibrary luaUsbLibrary = new LuaUsbLibrary(globals, su, usbGadget, aio_);
                    LuaValue luaChunk_ = globals.load(src_);
                    luaChunk_.call();
                } catch (LuaError e) {
                    if (!(e.getCause() instanceof InterruptedException)) {
                        e.printStackTrace();
                        aio_.onLogMessage("<b>LuaError:</b> " +
                                e.getMessage().replace("\n", "<br>"));
                    }
                }
            } finally {
                aio_.onLogMessage("<b>-- Ended <i>" + name_ + "</i></b>");

                if (usbGadget.isBound(su)) {
                    usbGadget.unbind(su);
                    usbGadget.remove(su);
                }
            }
        } catch (Shell.ShellDiedException e) {
            if (su == null) {
                aio_.onLogMessage("<b>Could not obtain superuser privileges!</b>");
            } else {
                aio_.onLogMessage("<b>SU shell has unexpectedly died!</b>");
            }
        } finally {
            if (su != null) {
                su.close();
            }
        }
    }

    public String getName() {
        return name_;
    }
}