package cf.netdex.hidfuzzer.lua;

import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import cf.netdex.hidfuzzer.ltask.HIDTask;

import static cf.netdex.hidfuzzer.MainActivity.TAG;
import static org.luaj.vm2.LuaValue.*;

/**
 * Created by netdex on 12/28/17.
 */

public class LuaHIDBinding {

    private HIDTask task;

    private LuaThread mKBLiteRoutine;
    private boolean kbliteStarted = false;

    private Globals globals;

    public LuaHIDBinding(HIDTask task) {
        this.task = task;
    }

    public void bind(Globals globals) {
        this.globals = globals;

        // put all lua bindings into scope
        LuaFunction hidFuncs[] = {
                new delay(), new test(), new hid_mouse(), new hid_keyboard(), new press_keys(),
                new send_string(), new cancelled(), new log(), new should(), new ask(), new say(), new progress()
        };
        for (LuaFunction f : hidFuncs) {
            globals.set(f.name(), f);
        }

        // all constant enum values (please tell me if there is a better way to do this)
        LuaTable mouseButtons = tableOf();
        for (Input.M.B imb : Input.M.B.values()) {
            mouseButtons.set(imb.name(), imb.code);
        }
        LuaTable keyCodes = tableOf();
        for (Input.KB.K ikk : Input.KB.K.values()) {
            keyCodes.set(ikk.name(), ikk.c);
        }
        for (Input.KB.M ikm : Input.KB.M.values()) {
            keyCodes.set(ikm.name(), ikm.c);
        }
        globals.set("ms", mouseButtons);
        globals.set("kb", keyCodes);

        // table for keyboard light coroutine
        LuaTable kbl = tableOf();
        kbl.set("get", new kblite_get());
        kbl.set("available", new kblite_available());
        kbl.set("begin", new kblite_begin());
//        kbl.set("stop", new kblite_stop());
        globals.set("kbl", kbl);
    }

    class kblite_begin extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            if(mKBLiteRoutine != null) throw new IllegalStateException("kblite already enabled!");
            mKBLiteRoutine = new LuaThread(globals, new kblite_coroutine_func(globals));
            kbliteStarted = true;
            Varargs result = mKBLiteRoutine.resume(NONE);
            return result.arg1();
        }
    }
//
//    class kblite_stop extends ZeroArgFunction {
//
//        @Override
//        public LuaValue call() {
//            kbliteStarted = false;
//            mKBLiteRoutine = null;
//            task.getHIDR().getKeyboardLightListener().kill();
//            return NIL;
//        }
//    }

    class kblite_get extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            if(!kbliteStarted) throw new LuaError("kblite not started!");
            return mKBLiteRoutine.resume(NONE).arg(2);
        }
    }

    class kblite_available extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            int val = task.getHIDR().getKeyboardLightListener().available();
            return valueOf(val > 0);
        }
    }

    class kblite_coroutine_func extends ZeroArgFunction {

        private Globals globals;

        kblite_coroutine_func(Globals globals) {
            this.globals = globals;
        }

        @Override
        public LuaValue call() {
            HIDR.KeyboardLightListener kll = task.getHIDR().getKeyboardLightListener();
            kll.start();
            globals.yield(NONE);
            while (kll.available() >= 0) {
                int val = kll.read();
                boolean num = (val & 0x1) != 0;
                boolean caps = (val & 0x2) != 0;
                boolean scroll = (val & 0x4) != 0;
                LuaTable result = tableOf();
                result.set("num", valueOf(num));
                result.set("caps", valueOf(caps));
                result.set("scroll", valueOf(scroll));
                globals.yield(varargsOf(new LuaValue[]{result}));
            }
            kbliteStarted = false;
            return NIL;
        }
    }

    class delay extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            long d = arg.checklong();
            task.getHIDR().delay(d);
            return NIL;
        }
    }

    class test extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return valueOf(task.getHIDR().test() == 0);
        }
    }

    class hid_mouse extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            byte a[] = new byte[args.narg()];
            for (int i = 1; i <= args.narg(); ++i) {
                a[i - 1] = (byte) args.arg(i).checkint();
            }
            task.getHIDR().hid_mouse(a);
            return NONE;
        }
    }

    class hid_keyboard extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            byte a[] = new byte[args.narg()];
            for (int i = 1; i <= args.narg(); ++i) {
                a[i - 1] = (byte) args.arg(i).checkint();
            }
            task.getHIDR().hid_keyboard(a);
            return NONE;
        }
    }

    class press_keys extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            byte a[] = new byte[args.narg()];
            for (int i = 1; i <= args.narg(); ++i) {
                a[i - 1] = (byte) args.arg(i).checkint();
            }
            task.getHIDR().press_keys(a);
            return NONE;
        }
    }

    class send_string extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue string, LuaValue delay) {
            String s = string.checkjstring();
            int d = delay.isnil() ? 0 : delay.checkint();
            task.getHIDR().send_string(s, d);
            return NIL;
        }
    }

    class cancelled extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            return valueOf(task.isCancelled());
        }
    }

    class log extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            String msg = arg.tojstring();
            task.getIO().log(msg);
            return NIL;
        }
    }

    class should extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue title, LuaValue message) {
            String t = title.tojstring();
            String m = message.tojstring();
            return valueOf(task.getIO().should(t, m));
        }
    }

    class ask extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue title, LuaValue defaults) {
            String t = title.tojstring();
            String d = defaults.isnil() ? "" : defaults.tojstring();
            return valueOf(task.getIO().ask(t, d));
        }
    }

    class say extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue title, LuaValue message) {
            String t = title.tojstring();
            String m = message.tojstring();
            task.getIO().say(t, m);
            return NIL;
        }
    }

    class progress extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue status) {
            try {
                HIDTask.RunState state = Enum.valueOf(HIDTask.RunState.class, status.tojstring().toUpperCase());
                task.progress(state);
            } catch (Exception e) {

            }
            return NIL;
        }
    }

}
