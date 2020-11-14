package org.netdex.hidfuzzer.hid;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import org.netdex.hidfuzzer.MainActivity;

import eu.chainfire.libsuperuser.Shell;

/**
 * Wrapper for HID class for ease of usage
 * <p>
 * Created by netdex on 1/16/2017.
 */

public class HIDInterface {
    private final Shell.Threaded su_;
    private final String devicePath_;

//    private final KeyboardLightListener mKeyboardLightListener;

    public HIDInterface(Shell.Threaded su, String devicePath) {
        this.su_ = su;
        this.devicePath_ = devicePath;
//        this.mKeyboardLightListener = new KeyboardLightListener();
    }

    /**
     * Tests if current HID device is connected by sending a dummy key
     */
    public boolean test() throws Shell.ShellDiedException {
        try {
            sendKeyboard((byte) 0, Input.KB.K.VOLUME_UP.c);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Sends mouse command
     *
     * @param offset command byte[] to send, defined in HID.java
     */
    public void sendMouse(byte... offset) {
        HID.sendHIDMouse(su_, devicePath_, offset);
    }

    /**
     * Sends keyboard command
     *
     * @param keys command byte[] to send, defined in HID.java
     */
    public void sendKeyboard(byte... keys) throws Shell.ShellDiedException, IOException {
        HID.sendHIDKeyboard(su_, devicePath_, keys);
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
    public void sendKeyboard(String s, int d) throws Shell.ShellDiedException, IOException, InterruptedException {
        int ec = 0;
        char lc = Character.MIN_VALUE;
        for (char c : s.toCharArray()) {
            byte cd = AP_MAP_CODE[(int) c];
            boolean st = AP_MAP_SHIFT[(int) c];
            if (cd == -1)
                throw new IllegalArgumentException("Given string contains illegal characters");
            if (Character.toLowerCase(c) == Character.toLowerCase(lc)) sendKeyboard();
            sendKeyboard(st ? Input.KB.M.LSHIFT.c : 0, cd);
            if (d != 0){
                Thread.sleep(d);
            }
            lc = c;
        }
        sendKeyboard();
    }

//    public KeyboardLightListener getKeyboardLightListener() {
//        return mKeyboardLightListener;
//    }

    /**
     * Listens for changes in keyboard lights (num lock, caps lock, scroll lock)
     */
//    public class KeyboardLightListener {
//        private Process mKeyboardLightProc;
//        private InputStream mKeyboardLightStream;
//        private int mLastLightState;
//
//        /**
//         * Begins keyboard light listening process
//         *
//         * @return error code
//         */
//        public int start() {
//            if (mKeyboardLightProc != null)
//                throw new IllegalArgumentException("KB light proc already running");
//            try {
//                mKeyboardLightProc = Runtime.getRuntime().exec("cat " + devicePath_);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (mKeyboardLightProc != null) {
//                mKeyboardLightStream = mKeyboardLightProc.getInputStream();
//                return 0;
//            } else {
//                return 1;
//            }
//        }
//
//        /**
//         * Bitmask of light states:
//         * NUM      0x01
//         * CAPS     0x02
//         * SCROLL   0x04
//         *
//         * @return bitmask of light states
//         */
//        public int read() {
//            try {
//                if (mKeyboardLightStream != null)
//                    return mLastLightState = mKeyboardLightStream.read();
//                return -1;
//            } catch (IOException e) {
//                Log.d(MainActivity.TAG, "Light stream forcibly terminated");
//                return -1;
//            }
//        }
//
//        /**
//         * Checks for availability of new data from keyboard light stream
//         *
//         * @return bytes available to read, or -1 if null
//         */
//        public int available() {
//            if (mKeyboardLightStream != null) {
//                try {
//                    return mKeyboardLightStream.available();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return -1;
//        }
//
//        /**
//         * Kill the listener process and tidy up
//         */
//        public void kill() {
//            if (mKeyboardLightStream != null) {
//                try {
//                    mKeyboardLightStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mKeyboardLightStream = null;
//            }
//
//            // close the stream before killing the process
//            if (mKeyboardLightProc != null) {
//                mKeyboardLightProc.destroy();
//                mKeyboardLightProc = null;
//            }
//        }
//
//        public int getLastLightState() {
//            return mLastLightState;
//        }
//    }
}