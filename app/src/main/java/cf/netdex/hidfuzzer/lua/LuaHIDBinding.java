package cf.netdex.hidfuzzer.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import cf.netdex.hidfuzzer.hid.Input;
import cf.netdex.hidfuzzer.ltask.HIDTask;

import static org.luaj.vm2.LuaValue.*;

/**
 * Created by netdex on 12/28/17.
 */

public class LuaHIDBinding {

    private HIDTask task;

    public LuaHIDBinding(HIDTask task) {
        this.task = task;
    }

    public void bind(Globals globals) {
        LuaValue libHid = tableOf();
        LuaValue libIo = tableOf();

        LuaFunction hidFuncs[] = {
                new delay(), new test(), new hid_mouse(), new hid_keyboard(), new press_keys(),
                new send_string(), new cancelled(),
        };
        for (LuaFunction f : hidFuncs) {
            libHid.set(f.name(), f);
        }

        LuaFunction ioFuncs[] = {
                new log(), new should(), new ask(), new say()
        };
        for (LuaFunction f : ioFuncs) {
            libIo.set(f.name(), f);
        }

        // all constant enum values (please tell me if there is a better way to do this)
        LuaValue input = tableOf();
        LuaTable mouse = tableOf();
        LuaTable keyboard = tableOf();
        LuaTable mouseButtons = tableOf();
        for (Input.M.B imb : Input.M.B.values()) {
            mouseButtons.set(imb.name(), imb.code);
        }
        LuaTable keyModifiers = tableOf();
        for (Input.KB.M ikm : Input.KB.M.values()) {
            keyModifiers.set(ikm.name(), ikm.c);
        }
        LuaTable keyCodes = tableOf();
        for (Input.KB.K ikk : Input.KB.K.values()) {
            keyCodes.set(ikk.name(), ikk.c);
        }
        mouse.set("b", mouseButtons);
        keyboard.set("m", keyModifiers);
        keyboard.set("k", keyCodes);
        input.set("m", mouse);
        input.set("kb", keyboard);
        libHid.set("input", input);

        globals.set("hid", libHid);
        globals.set("dio", libIo);
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
            return valueOf(task.getHIDR().test());
        }
    }

    class hid_mouse extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            byte a[] = new byte[args.narg()];
            for (int i = 1; i <= args.narg(); ++i) {
                a[i] = (byte) args.arg(i).checkint();
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
                a[i] = (byte) args.arg(i).checkint();
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
                a[i] = (byte) args.arg(i).checkint();
            }
            task.getHIDR().press_keys(a);
            return NONE;
        }
    }

    class send_string extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue string, LuaValue delay) {
            if(delay.isnil()) delay = valueOf(0);
            String s = string.checkjstring();
            int d = delay.checkint();
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
            String d = defaults.tojstring();
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

}
