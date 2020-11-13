package org.netdex.hidfuzzer.ltask;

import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.luaj.vm2.lib.jse.JsePlatform;
import org.netdex.hidfuzzer.configfs.UsbGadget;
import org.netdex.hidfuzzer.configfs.UsbGadgetParameters;
import org.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionHid;
import org.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionHidParameters;
import org.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionMassStorageParameters;
import org.netdex.hidfuzzer.hid.HIDInterface;
import org.netdex.hidfuzzer.lua.LuaHIDBinding;
import org.netdex.hidfuzzer.util.Command;

import eu.chainfire.libsuperuser.Shell;

import static org.netdex.hidfuzzer.MainActivity.TAG;

/**
 * Created by netdex on 1/16/2017.
 */

public class LuaHIDTask implements Runnable {

    static final String DEV_KEYBOARD = "/dev/hidg0";
    static final String DEV_MOUSE = "/dev/hidg1";

    private AsyncIOBridge aio_;

    private HIDInterface hidInterface_;
    private LuaHIDBinding luaHIDBinding_;

    private String name_;
    private String src_;
    private LuaValue luaChunk_;

    AtomicBoolean cancelled_ = new AtomicBoolean(false);

    public LuaHIDTask(String name, String src, AsyncIOBridge dialogIO) {
        this.name_ = name;
        this.src_ = src;
        this.aio_ = dialogIO;
    }

    private UsbGadgetParameters usbGadgetParams = new UsbGadgetParameters(
            "Samsung",
            "samsung123",
            "0xa4a5",
            "0x0525",
            "Mass Storage Gadget",
            "Configuration 1",
            120
    );

    private UsbGadgetFunctionHidParameters usbGadgetFcnHidParams = new UsbGadgetFunctionHidParameters(
            1,
            1,
            8,
            new byte[]{
                    (byte) 0x05, (byte) 0x01,    /* USAGE_PAGE (Generic Desktop)	          */
                    (byte) 0x09, (byte) 0x06,    /* USAGE (Keyboard)                       */
                    (byte) 0xa1, (byte) 0x01,    /* COLLECTION (Application)               */
                    (byte) 0x05, (byte) 0x07,    /*   USAGE_PAGE (Keyboard)                */
                    (byte) 0x19, (byte) 0xe0,    /*   USAGE_MINIMUM (Keyboard LeftControl) */
                    (byte) 0x29, (byte) 0xe7,    /*   USAGE_MAXIMUM (Keyboard Right GUI)   */
                    (byte) 0x15, (byte) 0x00,    /*   LOGICAL_MINIMUM (0)                  */
                    (byte) 0x25, (byte) 0x01,    /*   LOGICAL_MAXIMUM (1)                  */
                    (byte) 0x75, (byte) 0x01,    /*   REPORT_SIZE (1)                      */
                    (byte) 0x95, (byte) 0x08,    /*   REPORT_COUNT (8)                     */
                    (byte) 0x81, (byte) 0x02,    /*   INPUT (Data,Var,Abs)                 */
                    (byte) 0x95, (byte) 0x01,    /*   REPORT_COUNT (1)                     */
                    (byte) 0x75, (byte) 0x08,    /*   REPORT_SIZE (8)                      */
                    (byte) 0x81, (byte) 0x03,    /*   INPUT (Cnst,Var,Abs)                 */
                    (byte) 0x95, (byte) 0x05,    /*   REPORT_COUNT (5)                     */
                    (byte) 0x75, (byte) 0x01,    /*   REPORT_SIZE (1)                      */
                    (byte) 0x05, (byte) 0x08,    /*   USAGE_PAGE (LEDs)                    */
                    (byte) 0x19, (byte) 0x01,    /*   USAGE_MINIMUM (Num Lock)             */
                    (byte) 0x29, (byte) 0x05,    /*   USAGE_MAXIMUM (Kana)                 */
                    (byte) 0x91, (byte) 0x02,    /*   OUTPUT (Data,Var,Abs)                */
                    (byte) 0x95, (byte) 0x01,    /*   REPORT_COUNT (1)                     */
                    (byte) 0x75, (byte) 0x03,    /*   REPORT_SIZE (3)                      */
                    (byte) 0x91, (byte) 0x03,    /*   OUTPUT (Cnst,Var,Abs)                */
                    (byte) 0x95, (byte) 0x06,    /*   REPORT_COUNT (6)                     */
                    (byte) 0x75, (byte) 0x08,    /*   REPORT_SIZE (8)                      */
                    (byte) 0x15, (byte) 0x00,    /*   LOGICAL_MINIMUM (0)                  */
                    (byte) 0x25, (byte) 0x65,    /*   LOGICAL_MAXIMUM (101)                */
                    (byte) 0x05, (byte) 0x07,    /*   USAGE_PAGE (Keyboard)                */
                    (byte) 0x19, (byte) 0x00,    /*   USAGE_MINIMUM (Reserved)             */
                    (byte) 0x29, (byte) 0x65,    /*   USAGE_MAXIMUM (Keyboard Application) */
                    (byte) 0x81, (byte) 0x00,    /*   INPUT (Data,Ary,Abs)                 */
                    (byte) 0xc0                  /* END_COLLECTION                         */
            }
    );

    private UsbGadgetFunctionMassStorageParameters usbGadgetFcnStorParams = null;

    private UsbGadget usbGadget = null;

    @Override
    public void run() {
        aio_.onLogClear();

        Shell.Threaded su = null;
        try {
            su = Shell.Pool.SU.get();

            if (Command.pathExists(su, DEV_KEYBOARD)) {
                // assume kernel patch exists
                aio_.onLogMessage("Kernel patch detected");

                // TODO we can't actually do this because configfs makes it
            } else {
                if (Command.pathExists(su, "/config")) {
                    aio_.onLogMessage("No kernel patch detected, using configfs");
                    usbGadget = new UsbGadget(usbGadgetParams, "keyboardgadget", "/config");
                    UsbGadgetFunctionHid fcnHid = new UsbGadgetFunctionHid(0, usbGadgetFcnHidParams);
                    usbGadget.addFunction(fcnHid);
//                usbGadgetFcnStorParams =
//                        new UsbGadgetFunctionMassStorageParameters(
//                                /*getContext().getFilesDir().getAbsolutePath() + */"/data/local/tmp/mass_storage-lun0.img",
//                                256);
//                Log.i(TAG, usbGadgetFcnStorParams.file);
//                UsbGadgetFunctionMassStorage fcnStor =
//                        new UsbGadgetFunctionMassStorage(1, usbGadgetFcnStorParams);
//                usbGadget.addFunction(fcnStor);
                    usbGadget.create(su);
                    usbGadget.bind(su);
                } else {
                    Log.e(TAG, "No method exists for accessing hid gadget");
                    return;
                }
            }

            try {
//                su.run("chmod 666 " + DEV_KEYBOARD);
                //mSU.addCommand("chmod 666 " + DEV_MOUSE);

                hidInterface_ = new HIDInterface(su, DEV_KEYBOARD, "" /*DEV_MOUSE*/);
                aio_.onLogMessage("<b>-- Started <i>" + name_ + "</i></b>");

                try {
                    Globals globals = JsePlatform.standardGlobals();
                    luaHIDBinding_ = new LuaHIDBinding(globals, hidInterface_, aio_, cancelled_);
                    luaChunk_ = globals.load(src_);
                    luaChunk_.call();
                } catch (LuaError e) {
                    e.printStackTrace();
                    aio_.onLogMessage("<b>LuaError:</b> " + e.getMessage());
                }
            } finally {
                aio_.onLogMessage("<b>-- Ended <i>" + name_ + "</i></b>");
                if (usbGadget != null) {
                    usbGadget.remove(su);
                    usbGadget = null;
                }

                if (hidInterface_ != null) {
                    hidInterface_.getKeyboardLightListener().kill();
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
            cancelled_.set(true);
            aio_.onSignal(AsyncIOBridge.Signal.DONE);
        }
    }

    public void setCancelled(boolean cancelled) {
        cancelled_.set(cancelled);
    }
}