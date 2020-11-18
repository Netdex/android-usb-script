package org.netdex.hidfuzzer.function;

import org.netdex.hidfuzzer.util.Command;

import java.io.IOException;
import java.util.Arrays;

import eu.chainfire.libsuperuser.Shell;

public class HidKeyboardInterface {
    private final Shell.Threaded su_;
    private final String devicePath_;

    public HidKeyboardInterface(Shell.Threaded su, String devicePath) {
        this.su_ = su;
        this.devicePath_ = devicePath;
    }

    /**
     * Sends keyboard command
     *
     * @param keys command byte[] to send, defined in HID.java
     */
    public void sendKeyboard(byte... keys) throws Shell.ShellDiedException, IOException {
        sendKeyboardHID(su_, devicePath_, keys);
    }

    /**
     * Presses keys and releases it
     *
     * @param keys command byte[] to send, defined in HID.java
     */
    public void pressKeys(byte... keys) throws Shell.ShellDiedException, IOException {
        sendKeyboard(keys);
        sendKeyboard();
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

    /**
     * Sends a string to the keyboard with no delay
     *
     * @param s String to send
     */
    public void sendKeyboard(String s) throws Shell.ShellDiedException, IOException, InterruptedException {
        sendKeyboard(s, 0);
    }

    /**
     * Sends a string to the keyboard
     *
     * @param s String to send
     * @param d Delay after key press
     */
    public void sendKeyboard(String s, long d) throws Shell.ShellDiedException, IOException, InterruptedException {
        byte lcd = 0;
        for (char c : s.toCharArray()) {
            byte cd = AP_MAP_CODE[(int) c];
            boolean st = AP_MAP_SHIFT[(int) c];
            if (cd == -1)
                throw new IllegalArgumentException("Given string contains illegal characters");
            if (lcd == cd) sendKeyboard();
            sendKeyboard(st ? HidInput.Keyboard.Mod.MOD_LSHIFT.code : 0, cd);
            if (d > 0) {
                Thread.sleep(d);
            }
            lcd = cd;
        }
        sendKeyboard();
    }

    /**
     * A        B        C        D        E        F        G        H
     * XXXXXXXX 00000000 XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX
     * <p>
     * A: K modifier mask
     * B: Reserved
     * C: K 1; D: K 2; E: K 3; F: K 4; G: K 5; H: K 6;
     *
     * @param sh   SUExtensions shell
     * @param dev  KB device (/dev/hidg0)
     * @param keys HID keyboard bytes
     */
    public static void sendKeyboardHID(Shell.Threaded sh, String dev, byte... keys) throws Shell.ShellDiedException, IOException {
        byte[] buffer = new byte[8];
        if (keys.length > 7)
            throw new IllegalArgumentException("Cannot send more than 6 keys");
        Arrays.fill(buffer, (byte) 0);
        if (keys.length > 0) buffer[0] = keys[0];
        if (keys.length > 1) System.arraycopy(keys, 1, buffer, 2, keys.length - 1);
        Command.write(sh, dev, buffer);
    }
}
