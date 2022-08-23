package org.netdex.androidusbscript.lua;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.netdex.androidusbscript.function.DeviceStream;
import org.netdex.androidusbscript.util.FileSystem;
import org.netdex.androidusbscript.function.HidInput.Keyboard.Mod;

import static org.luaj.vm2.LuaValue.NONE;
import static org.netdex.androidusbscript.MainActivity.TAG;
import static org.netdex.androidusbscript.function.HidInput.Keyboard.Key;

import static org.netdex.androidusbscript.function.HidInput.Keyboard.Mod.*;
import static org.netdex.androidusbscript.function.HidInput.Keyboard.Key.*;
import static org.netdex.androidusbscript.task.LuaUsbLibrary.checkbyte;

import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class LuaHidKeyboard extends DeviceStream {

    public LuaHidKeyboard(FileSystem fs, String devicePath) throws IOException {
        super(fs, devicePath);
    }

    /**
     * A        B        C        D        E        F        G        H
     * XXXXXXXX 00000000 XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX
     * <p>
     * A: K modifier mask
     * B: Reserved
     * C: K 1; D: K 2; E: K 3; F: K 4; G: K 5; H: K 6;
     *
     * @param keys HID keyboard bytes
     */
    public void raw(byte... keys) throws IOException, InterruptedException {
        byte[] buffer = new byte[8];
        if (keys.length > 7)
            throw new IllegalArgumentException("Too many parameters in HID report");
        if (keys.length > 0) buffer[0] = keys[0];
        if (keys.length > 1) System.arraycopy(keys, 1, buffer, 2, keys.length - 1);
        this.write(buffer);
    }

    public void press(byte... keys) throws IOException, InterruptedException {
        raw(keys);
        raw();
    }

    public static VarArgFunction chord = new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs args) {
            args.argcheck(args.narg() >= 3, 0, "at least 2 args required");
            LuaHidKeyboard hid = (LuaHidKeyboard) args.arg1().checkuserdata();

            byte mask;
            if (args.arg(2).isint()) {
                mask = checkbyte(args.checkint(2));
            } else {
                mask = 0;
                LuaTable mods = args.checktable(2);
                for (int i = 1; i <= mods.length(); ++i) {
                    mask |= checkbyte(mods.get(i).checkint());
                }
            }

            byte[] a = new byte[args.narg()];
            a[0] = mask;
            for (int i = 3; i <= args.narg(); ++i) {
                a[i - 1] = checkbyte(args.arg(i).checkint());
            }
            try {
                hid.press(a);
            } catch (IOException | InterruptedException e) {
                throw new LuaError(e);
            }
            return NONE;
        }
    };

    public LuaValue read_lock() throws IOException, InterruptedException {
        if (available() < 1)
            return LuaValue.NIL;

        int val = read();
        LuaValue table = LuaValue.tableOf();
        table.set("num_lock", LuaValue.valueOf((val & (1 << 0)) != 0));
        table.set("caps_lock", LuaValue.valueOf((val & (1 << 1)) != 0));
        table.set("scroll_lock", LuaValue.valueOf((val & (1 << 2)) != 0));
        return table;
    }

    /**
     * Sends a string to the keyboard
     *
     * @param s String to send
     * @param d Delay after key press
     */
    public void string(String s, long d) throws IOException, InterruptedException {
        byte lcd = 0;
        for (char c : s.toCharArray()) {
            byte cd = AP_MAP_CODE[c];
            boolean st = AP_MAP_SHIFT[c];
            if (cd == -1)
                throw new IllegalArgumentException("Given string contains illegal characters");
            if (lcd == cd) raw();
            raw(st ? MOD_LSHIFT.code : MOD_NONE.code, cd);
            if (d > 0) {
                Thread.sleep(d);
            }
            lcd = cd;
        }
        raw();
    }

    public void string(String s) throws IOException, InterruptedException {
        string(s, 0);
    }

    /* Begin string to code conversion tables */
    private static final String MP_ALPHA = "abcdefghijklmnopqrstuvwxyz";        // 0x04
    private static final String MP_ALPHA_ALT = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";    // 0x04 SHIFT
    private static final String MP_NUM = "1234567890";                          // 0x1E
    private static final String MP_NUM_ALT = "!@#$%^&*()";                      // 0x1E SHIFT
    private static final String MP_SPEC = " -=[]\\#;'`,./";                     // 0x2C
    private static final String MP_SPEC_ALT = " _+{}| :\"~<>?";                 // 0x2C SHIFT
    private static final String MP_SU_SPEC = "\n";                              // 0X28

    private static final String[] AP_ATT = {MP_ALPHA, MP_ALPHA_ALT, MP_NUM, MP_NUM_ALT, MP_SPEC, MP_SPEC_ALT, MP_SU_SPEC};
    private static final boolean[] AP_SHIFT = {false, true, false, true, false, true, false};
    private static final byte[] AP_OFFSET = {0x04, 0x04, 0x1E, 0x1E, 0x2C, 0x2C, 0x28};

    private static final byte[] AP_MAP_CODE = new byte[128];
    private static final boolean[] AP_MAP_SHIFT = new boolean[128];

    // build fast conversion tables from human readable data
    static {
        for (int i = 0; i < 128; i++) {
            char c = (char) i;
            boolean shift = false;
            byte code = 0;

            int idx = 0;
            while (idx < AP_ATT.length) {
                int tc;
                if ((tc = AP_ATT[idx].indexOf(c)) != -1) {
                    code = (byte) (AP_OFFSET[idx] + tc);
                    shift = AP_SHIFT[idx];
                    break;
                }
                idx++;
            }
            if (idx == AP_ATT.length) {
                AP_MAP_CODE[i] = -1;
            } else {
                AP_MAP_CODE[i] = code;
                AP_MAP_SHIFT[i] = shift;
            }
        }
    }
    /* End string to code conversion tables */
}
