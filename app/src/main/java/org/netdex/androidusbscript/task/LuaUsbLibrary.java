package org.netdex.androidusbscript.task;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.netdex.androidusbscript.configfs.UsbGadget;
import org.netdex.androidusbscript.configfs.function.UsbGadgetFunctionHid;
import org.netdex.androidusbscript.configfs.function.UsbGadgetFunctionMassStorage;
import org.netdex.androidusbscript.function.HidDescriptor;
import org.netdex.androidusbscript.function.HidInput;
import org.netdex.androidusbscript.function.HidKeyboardInterface;
import org.netdex.androidusbscript.function.HidMouseInterface;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 12/28/17.
 */

public class LuaUsbLibrary {

    private final Shell.Threaded su_;
    private final UsbGadget usbGadget_;
    private final LuaIOBridge aio_;
    private final AtomicBoolean cancelled_;

    public LuaUsbLibrary(Shell.Threaded su, UsbGadget usbGadget, LuaIOBridge aio, AtomicBoolean cancelled) {
        this.aio_ = aio;
        this.su_ = su;
        this.usbGadget_ = usbGadget;
        this.cancelled_ = cancelled;
    }

    public void bind(Globals globals) {
        LuaFunction library = new luausb();
        globals.set(library.name(), library.call());
        for (LuaFunction f : new LuaFunction[]{new wait(), new print(), new confirm(), new prompt()}) {
            globals.set(f.name(), f);
        }
        for (HidInput.Keyboard.Key ikk : HidInput.Keyboard.Key.values()) {
            globals.set(ikk.name(), ikk.code);
        }
        for (HidInput.Keyboard.Mod ikm : HidInput.Keyboard.Mod.values()) {
            globals.set(ikm.name(), ikm.code);
        }
        for (HidInput.Mouse.Button imb : HidInput.Mouse.Button.values()) {
            globals.set(imb.name(), imb.code);
        }
    }

    class wait extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            long d = arg.checklong();
            try {
                if (cancelled_.get()) throw new InterruptedException();
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

    class confirm extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue title, LuaValue message) {
            String t = title.tojstring();
            String m = message.tojstring();
            return valueOf(aio_.onConfirm(t, m));
        }
    }

    class prompt extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue title, LuaValue defaults) {
            String t = title.tojstring();
            String d = defaults.isnil() ? "" : defaults.tojstring();
            return valueOf(aio_.onPrompt(t, d));
        }
    }


    /**
     * luausb library generator function
     */
    class luausb extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            LuaValue library = tableOf();
            library.set("create", new create());
            library.set("state", new state());
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
             *                         TODO
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
                            device = new keyboard(id).call();
                            break;
                        }
                        case "mouse": {
                            UsbGadgetFunctionHid fcnHid = new UsbGadgetFunctionHid(
                                    id, HidDescriptor.MOUSE.getParameters());
                            usbGadget_.addFunction(fcnHid);
                            device = new mouse(id).call();
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
                            device = new storage(id).call();
                            break;
                        default:
                            throw new LuaError(String.format("Invalid function type \"%s\"", type));
                    }
                    devices[i - 1] = device;
                }

                // MITIGATION: Windows seems to memoize usb configurations by serial number
                // (not across reboots). This causes undefined behavior when the configuration
                // changes. Generate a serial number based on the configuration.
                String serial = usbGadget_.serial();
                UsbGadget.Parameters gadgetParameters = new UsbGadget.Parameters(
                        "The Linux Foundation",
                        serial,
                        "0x1d6b",
                        "0x0105",
                        "FunctionFS Gadget",
                        "Composite");
                try {
                    usbGadget_.create(su_, gadgetParameters);
                    usbGadget_.bind(su_);
                } catch (Shell.ShellDiedException e) {
                    throw new LuaError(e);
                }
                return LuaValue.varargsOf(devices);
            }
        }

        class state extends ZeroArgFunction {
            @Override
            public LuaValue call() {
                try {
                    return valueOf(UsbGadget.getUDCState(su_, UsbGadget.getSystemUDC(su_)));
                } catch (Shell.ShellDiedException e) {
                    throw new LuaError(e);
                }
            }
        }

        class keyboard extends ZeroArgFunction {
            private final HidKeyboardInterface hid_;

            public keyboard(int id) {
                String devicePath = String.format("/dev/hidg%d", id);
                this.hid_ = new HidKeyboardInterface(su_, devicePath);
            }

            @Override
            public LuaValue call() {
                LuaValue library = tableOf();
                for (LuaFunction f : new LuaFunction[]{
                        new raw(), new chord(), new press(), new string()}) {
                    library.set(f.name(), f);
                }
                return library;
            }

            class raw extends VarArgFunction {
                @Override
                public Varargs invoke(Varargs args) {
                    byte[] a = new byte[args.narg()];
                    for (int i = 1; i <= args.narg(); ++i) {
                        a[i - 1] = checkbyte(args.arg(i).checkint());
                    }
                    try {
                        hid_.sendKeyboard(a);
                    } catch (Shell.ShellDiedException | IOException e) {
                        throw new LuaError(e);
                    }
                    return NONE;
                }
            }

            class chord extends VarArgFunction {
                @Override
                public Varargs invoke(Varargs args) {
                    args.argcheck(args.narg() >= 2, 0, "at least 2 args required");

                    byte mask;
                    if (args.arg1().isint()) {
                        mask = checkbyte(args.checkint(1));
                    } else {
                        mask = 0;
                        LuaTable mods = args.checktable(1);
                        for (int i = 1; i <= mods.length(); ++i) {
                            mask |= checkbyte(mods.get(i).checkint());
                        }
                    }

                    byte[] a = new byte[args.narg()];
                    a[0] = mask;
                    for (int i = 2; i <= args.narg(); ++i) {
                        a[i - 1] = checkbyte(args.arg(i).checkint());
                    }
                    try {
                        hid_.pressKeys(a);
                    } catch (Shell.ShellDiedException | IOException e) {
                        throw new LuaError(e);
                    }
                    return NONE;
                }
            }

            class press extends VarArgFunction {
                @Override
                public Varargs invoke(Varargs args) {
                    byte[] a = new byte[args.narg() + 1];
                    a[0] = HidInput.Keyboard.Mod.MOD_NONE.code;
                    for (int i = 1; i <= args.narg(); ++i) {
                        a[i] = checkbyte(args.arg(i).checkint());
                    }
                    try {
                        hid_.pressKeys(a);
                    } catch (Shell.ShellDiedException | IOException e) {
                        throw new LuaError(e);
                    }
                    return NONE;
                }

            }

            class string extends TwoArgFunction {

                @Override
                public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    String string = arg1.checkjstring();
                    long delay = arg2.optlong(0);
                    try {
                        hid_.sendKeyboard(string, delay);
                    } catch (Shell.ShellDiedException | IOException | InterruptedException e) {
                        throw new LuaError(e);
                    }
                    return NIL;
                }
            }
        }

        class mouse extends ZeroArgFunction {

            private final HidMouseInterface hid_;

            public mouse(int id) {
                String devicePath = String.format("/dev/hidg%d", id);
                this.hid_ = new HidMouseInterface(su_, devicePath);
            }

            @Override
            public LuaValue call() {
                LuaValue library = tableOf();
                for (LuaFunction f : new LuaFunction[]{
                        new raw(), new click(), new move(), new scroll()}) {
                    library.set(f.name(), f);
                }
                return library;
            }

            class raw extends VarArgFunction {
                @Override
                public Varargs invoke(Varargs args) {
                    byte[] a = new byte[args.narg()];
                    for (int i = 1; i <= args.narg(); ++i) {
                        a[i - 1] = (byte) args.arg(i).checkint();
                    }
                    try {
                        hid_.sendMouse(a);
                    } catch (Shell.ShellDiedException | IOException e) {
                        throw new LuaError(e);
                    }
                    return NONE;
                }
            }

            class click extends TwoArgFunction {

                @Override
                public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    byte mask = checkbyte(arg1.checkint());
                    long duration = arg2.optlong(0);
                    try {
                        hid_.click(mask, duration);
                    } catch (Shell.ShellDiedException | IOException | InterruptedException e) {
                        throw new LuaError(e);
                    }
                    return NIL;
                }
            }

            class move extends TwoArgFunction {

                @Override
                public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    byte dx = checkbyte(arg1.checkint());
                    byte dy = checkbyte(arg2.checkint());
                    try {
                        hid_.move(dx, dy);
                    } catch (Shell.ShellDiedException | IOException e) {
                        throw new LuaError(e);
                    }
                    return NIL;
                }
            }

            class scroll extends OneArgFunction {

                @Override
                public LuaValue call(LuaValue arg) {
                    byte offset = checkbyte(arg.checkint());
                    try {
                        hid_.scroll(offset);
                    } catch (Shell.ShellDiedException | IOException e) {
                        throw new LuaError(e);
                    }
                    return NIL;
                }
            }
        }

        class storage extends ZeroArgFunction {
            public storage(int id) {

            }

            @Override
            public LuaValue call() {
                return NIL;
            }
        }
    }

    public static byte checkbyte(int n) {
        if (n < Byte.MIN_VALUE || n > Byte.MAX_VALUE)
            throw new LuaError("bad argument: byte expected");
        return (byte) n;
    }
}
