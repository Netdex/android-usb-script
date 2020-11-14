package org.netdex.hidfuzzer.lua;

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
import org.netdex.hidfuzzer.configfs.UsbGadget;
import org.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionHid;
import org.netdex.hidfuzzer.hid.HIDInterface;
import org.netdex.hidfuzzer.hid.Input;
import org.netdex.hidfuzzer.task.AsyncIOBridge;

import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 12/28/17.
 */

public class LuaHIDBinding {

    private final Globals globals_;
    private final Shell.Threaded su_;
    private final UsbGadget usbGadget_;
    private final AsyncIOBridge aio_;

//    private LuaThread mKBLiteRoutine;
//    private boolean kbliteStarted = false;

    public LuaHIDBinding(Globals globals_, Shell.Threaded su, UsbGadget usbGadget, AsyncIOBridge aio) {
        this.aio_ = aio;
        this.su_ = su;
        this.usbGadget_ = usbGadget;
        this.globals_ = globals_;

        LuaFunction library = new luausb();
        globals_.set(library.name(), library.call());
    }

    class luausb extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            LuaValue library = tableOf();
            library.set("create", new create());
            return library;
        }

        class create extends VarArgFunction {

            @Override
            public Varargs invoke(Varargs args) {
                LuaTable library = tableOf();
                for (LuaFunction f : new LuaFunction[]{
                        new delay(), new log(), new should(), new ask()}) {
                    library.set(f.name(), f);
                }
//                LuaTable mouseButtons = tableOf();
//                for (Input.M.B imb : Input.M.B.values()) {
//                    mouseButtons.set(imb.name(), imb.code);
//                }
//                library.set("mouse", mouseButtons);

                LuaTable dev = tableOf();
                for (int i = 1; i <= args.narg(); ++i) {
                    LuaTable config = args.arg(i).checktable();
                    String type = config.get("type").checkjstring();
                    int id = config.get("id").checkint();

                    LuaValue device = null;
                    if (type.equals("keyboard")) {
                        UsbGadgetFunctionHid fcnHid = new UsbGadgetFunctionHid(
                                id, UsbGadgetFunctionHid.Parameters.DEFAULT);
                        usbGadget_.addFunction(fcnHid);
                        device = new keyboard(id).call();
                    } else {
                        throw new LuaError(String.format("Invalid function type \"%s\"", type));
                    }
                    dev.set(i, device);
                }
                try {
                    usbGadget_.create(su_);
                    usbGadget_.bind(su_);
                } catch (Shell.ShellDiedException e) {
                    throw new LuaError(e);
                }
                library.set("dev", dev);
                return library;
            }
        }

        class delay extends OneArgFunction {
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

        class log extends OneArgFunction {

            @Override
            public LuaValue call(LuaValue arg) {
                String msg = arg.tojstring();
                aio_.onLogMessage(msg);
                return NIL;
            }
        }

        class should extends TwoArgFunction {

            @Override
            public LuaValue call(LuaValue title, LuaValue message) {
                String t = title.tojstring();
                String m = message.tojstring();
                return valueOf(aio_.onConfirm(t, m));
            }
        }

        class ask extends TwoArgFunction {

            @Override
            public LuaValue call(LuaValue title, LuaValue defaults) {
                String t = title.tojstring();
                String d = defaults.isnil() ? "" : defaults.tojstring();
                return valueOf(aio_.onPrompt(t, d));
            }
        }

        class keyboard extends ZeroArgFunction {
            private final HIDInterface hid_;

            public keyboard(int id) {
                String devicePath = String.format("/dev/hidg%d", id);
                this.hid_ = new HIDInterface(su_, devicePath);
            }

            @Override
            public LuaValue call() {
                LuaValue library = tableOf();
                for (Input.KB.K ikk : Input.KB.K.values()) {
                    library.set(ikk.name(), ikk.c);
                }
                for (Input.KB.M ikm : Input.KB.M.values()) {
                    library.set(ikm.name(), ikm.c);
                }
                for (LuaFunction f : new LuaFunction[]{
                        new test(), new hid_mouse(), new hid_keyboard(),
                        new press_keys(), new send_string()}) {
                    library.set(f.name(), f);
                }
                return library;
            }

            class test extends ZeroArgFunction {
                @Override
                public LuaValue call() {
                    try {
                        return valueOf(hid_.test());
                    } catch (Shell.ShellDiedException e) {
                        throw new LuaError(e);
                    }
                }
            }

            class hid_mouse extends VarArgFunction {
                @Override
                public Varargs invoke(Varargs args) {
                    byte[] a = new byte[args.narg()];
                    for (int i = 1; i <= args.narg(); ++i) {
                        a[i - 1] = (byte) args.arg(i).checkint();
                    }
                    hid_.sendMouse(a);
                    return NONE;
                }
            }

            class hid_keyboard extends VarArgFunction {
                @Override
                public Varargs invoke(Varargs args) {
                    byte[] a = new byte[args.narg()];
                    for (int i = 1; i <= args.narg(); ++i) {
                        a[i - 1] = (byte) args.arg(i).checkint();
                    }
                    try {
                        hid_.sendKeyboard(a);
                    } catch (Shell.ShellDiedException | IOException e) {
                        throw new LuaError(e);
                    }
                    return NONE;
                }
            }

            class press_keys extends VarArgFunction {
                @Override
                public Varargs invoke(Varargs args) {
                    byte[] a = new byte[args.narg()];
                    for (int i = 1; i <= args.narg(); ++i) {
                        a[i - 1] = (byte) args.arg(i).checkint();
                    }
                    try {
                        hid_.pressKeys(a);
                    } catch (Shell.ShellDiedException | IOException e) {
                        throw new LuaError(e);
                    }
                    return NONE;
                }
            }

            class send_string extends TwoArgFunction {

                @Override
                public LuaValue call(LuaValue string, LuaValue delay) {
                    String s = string.checkjstring();
                    int d = delay.isnil() ? 0 : delay.checkint();
                    try {
                        hid_.sendKeyboard(s, d);
                    } catch (Shell.ShellDiedException | IOException | InterruptedException e) {
                        throw new LuaError(e);
                    }
                    return NIL;
                }
            }
        }
    }


//    class kblite_begin extends ZeroArgFunction {
//
//        @Override
//        public LuaValue call() {
//            if (mKBLiteRoutine != null) throw new IllegalStateException("kblite already enabled!");
//            mKBLiteRoutine = new LuaThread(globals_, new kblite_coroutine_func(globals_));
//            kbliteStarted = true;
//            Varargs result = mKBLiteRoutine.resume(NONE);
//            return result.arg1();
//        }
//    }

//    class kblite_stop extends ZeroArgFunction {
//
//        @Override
//        public LuaValue call() {
//            kbliteStarted = false;
//            mKBLiteRoutine = null;
//            hid_.getKeyboardLightListener().kill();
//            return NIL;
//        }
//    }

    //    class kblite_get extends ZeroArgFunction {
//
//        @Override
//        public LuaValue call() {
//            if (!kbliteStarted) throw new LuaError("kblite not started!");
//            return mKBLiteRoutine.resume(NONE).arg(2);
//        }
//    }
//
//    class kblite_available extends ZeroArgFunction {
//
//        @Override
//        public LuaValue call() {
//            int val = hid_.getKeyboardLightListener().available();
//            return valueOf(val > 0);
//        }
//    }
//
//    class kblite_coroutine_func extends ZeroArgFunction {
//
//        private final Globals globals;
//
//        kblite_coroutine_func(Globals globals) {
//            this.globals = globals;
//        }
//
//        @Override
//        public LuaValue call() {
//            HIDInterface.KeyboardLightListener kll = hid_.getKeyboardLightListener();
//            kll.start();
//            globals.yield(NONE);
//            while (kll.available() >= 0) {
//                int val = kll.read();
//                boolean num = (val & 0x1) != 0;
//                boolean caps = (val & 0x2) != 0;
//                boolean scroll = (val & 0x4) != 0;
//                LuaTable result = tableOf();
//                result.set("num", valueOf(num));
//                result.set("caps", valueOf(caps));
//                result.set("scroll", valueOf(scroll));
//                globals.yield(varargsOf(new LuaValue[]{result}));
//            }
//            kbliteStarted = false;
//            return NIL;
//        }
//    }
//


}
