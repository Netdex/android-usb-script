package org.netdex.androidusbscript.task;

import static org.netdex.androidusbscript.MainActivity.TAG;

import android.util.Log;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.netdex.androidusbscript.configfs.*;
import org.netdex.androidusbscript.configfs.function.*;
import org.netdex.androidusbscript.function.HidDescriptor;
import org.netdex.androidusbscript.function.HidInput.*;
import org.netdex.androidusbscript.lua.*;
import org.netdex.androidusbscript.util.FileSystem;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by netdex on 12/28/17.
 */

public class LuaUsbLibrary implements AutoCloseable {
    private final FileSystem fs_;
    private final UsbGadget usbGadget_;
    private final LuaIOBridge aio_;
    private final List<Closeable> devHandles_ = new ArrayList<>();

    public LuaUsbLibrary(FileSystem fs, UsbGadget usbGadget, LuaIOBridge aio) {
        this.fs_ = fs;
        this.aio_ = aio;
        this.usbGadget_ = usbGadget;
    }

    public void bind(Globals globals) {
        LuaFunction library = new luausb();
        globals.set(library.name(), library.call());
        for (LuaFunction f : new LuaFunction[]{new wait(), new print(), new confirm(), new prompt()}) {
            globals.set(f.name(), f);
        }
        for (Keyboard.Key ikk : Keyboard.Key.values()) {
            globals.set(ikk.name(), ikk.code);
        }
        for (Keyboard.Mod ikm : Keyboard.Mod.values()) {
            globals.set(ikm.name(), ikm.code);
        }
        for (Mouse.Button imb : Mouse.Button.values()) {
            globals.set(imb.name(), imb.code);
        }
    }

    @Override
    public void close() throws IOException {
        Log.v(TAG, "LuaUsbLibrary.close()");
        for (Closeable hndl : devHandles_) {
            hndl.close();
        }
    }

    static class wait extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            long d = arg.checklong();
            try {
                Thread.sleep(d);
            } catch (InterruptedException e) {
                throw new LuaError(e);
            }
            return NIL;
        }
    }

    class print extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            String msg = arg.tojstring();
            aio_.onLogMessage(msg);
            return NIL;
        }
    }

    class confirm extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            LuaTable table = arg.checktable();
            String title = table.get("title").optjstring("Confirm");
            String prompt = table.get("message").optjstring("");
            return valueOf(aio_.onConfirm(title, prompt));
        }
    }

    class prompt extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            LuaTable table = arg.checktable();
            String title = table.get("title").optjstring("Prompt");
            String prompt = table.get("message").optjstring("");
            String hint = table.get("hint").optjstring("Text");
            String def = table.get("default").optjstring("");

            return valueOf(aio_.onPrompt(title, prompt, hint, def));
        }
    }


    /**
     * luausb library generator function
     */
    class luausb extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            LuaValue library = tableOf();
            for (LuaFunction f : new LuaFunction[]{new create(), new state()}) {
                library.set(f.name(), f);
            }
            return library;
        }

        class create extends VarArgFunction {

            /**
             * Creates a USB composite device composed of multiple gadgets.
             * This function may only be called once within a script.
             *
             * @param args The configuration for each gadget part of the USB composite device.
             *             A configuration is a table which must have at least the following
             *             properties:
             *             <p>
             *             type: string = (keyboard|storage)
             *             id: integer = [n >= 0]
             *             <p>
             *             Depending on type, the configuration can have additional properties:
             * @return A library binding for interacting with the created gadgets.
             * The binding is a table with at least the following properties:
             * <p>
             * dev: table - An array with an device binding for each provided configuration.
             * <p>
             * It also contains various library functions.
             */
            @Override
            public Varargs invoke(Varargs args) {
                int numDevices = args.narg();
                LuaValue[] devices = new LuaValue[numDevices];
                for (int i = 1; i <= numDevices; ++i) {
                    LuaTable config = args.arg(i).checktable();
                    String type = config.get("type").checkjstring();
                    int id = config.get("id").checkint();

                    LuaValue device;
                    switch (type) {
                        case "keyboard": {
                            UsbGadgetFunctionHid fcnHid = new UsbGadgetFunctionHid(
                                    id, HidDescriptor.KEYBOARD.getParameters());
                            usbGadget_.addFunction(fcnHid);
                            device = keyboard(id);
                            break;
                        }
                        case "mouse": {
                            UsbGadgetFunctionHid fcnHid = new UsbGadgetFunctionHid(
                                    id, HidDescriptor.MOUSE.getParameters());
                            usbGadget_.addFunction(fcnHid);
                            device = mouse(id);
                            break;
                        }
                        case "storage":
                            String file = config.get("file").optjstring("/data/local/tmp/mass_storage-lun0.img");
                            boolean ro = config.get("ro").optboolean(false);
                            long size = config.get("size").optlong(256);

                            UsbGadgetFunctionMassStorage.Parameters storParam =
                                    new UsbGadgetFunctionMassStorage.Parameters(
                                            file, ro, true, false, false, true, size);
                            UsbGadgetFunctionMassStorage fcnStor = new UsbGadgetFunctionMassStorage(
                                    id, storParam);
                            usbGadget_.addFunction(fcnStor);
                            device = NIL;
                            break;
                        default:
                            throw new LuaError(String.format("Invalid function type \"%s\"", type));
                    }
                    devices[i - 1] = device;
                }

                // MITIGATION: Windows seems to memoize usb configurations by serial number
                // (not across reboots). This causes undefined behavior when the configuration
                // changes. Generate a serial number based on the configuration.
                UsbGadget.Parameters gadgetParameters = new UsbGadget.Parameters(
                        "The Linux Foundation",
                        usbGadget_.serial(),
                        "0x1d6b",
                        "0x0105",
                        "FunctionFS Gadget",
                        "Composite");
                try {
                    usbGadget_.add(fs_, gadgetParameters);
                    usbGadget_.bind(fs_);
                } catch (IOException e) {
                    throw new LuaError(e);
                }

                return LuaValue.varargsOf(devices);
            }
        }

        class state extends ZeroArgFunction {
            @Override
            public LuaValue call() {
                try {
                    return valueOf(UsbGadget.getUDCState(fs_, UsbGadget.getSystemUDC(fs_)));
                } catch (IOException e) {
                    throw new LuaError(e);
                }
//                return valueOf("unknown");
            }
        }
    }

    public LuaValue keyboard(int id) {
        try {
            LuaHidKeyboard hid = new LuaHidKeyboard(fs_, "/dev/hidg" + id);
            devHandles_.add(hid);
            return CoerceJavaToLua.coerce(hid);
        } catch (IOException e) {
            throw new LuaError(e);
        }
    }


    public LuaValue mouse(int id) {
        try {
            LuaHidMouse hid = new LuaHidMouse(fs_, "/dev/hidg" + id);
            devHandles_.add(hid);
            return CoerceJavaToLua.coerce(hid);
        } catch (IOException e) {
            throw new LuaError(e);
        }
    }

    public static byte checkbyte(int n) {
        if (n < Byte.MIN_VALUE || n > Byte.MAX_VALUE)
            throw new LuaError("bad argument: byte expected");
        return (byte) n;
    }
}
