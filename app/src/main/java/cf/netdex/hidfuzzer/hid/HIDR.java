package cf.netdex.hidfuzzer.hid;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import cf.netdex.hidfuzzer.MainActivity;
import cf.netdex.hidfuzzer.util.SUExecute;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/16/2017.
 */

public class HIDR {
    private Shell.Interactive mSU;
    private String mDevKeyboard;
    private String mDevMouse;

    private KeyboardLightListener mKeyboardLightListener;

    public HIDR(Shell.Interactive su, String devKeyboard, String devMouse) {
        this.mSU = su;
        this.mDevKeyboard = devKeyboard;
        this.mDevMouse = devMouse;
        this.mKeyboardLightListener = new KeyboardLightListener();
    }

    public void delay(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException ignored) {
        }
    }

    public int hid_mouse(byte... offset) {
        return HID.hid_mouse(mSU, mDevMouse, offset);
    }

    public int hid_keyboard(byte... keys) {
        return HID.hid_keyboard(mSU, mDevKeyboard, keys);
    }

    public int press_keys(byte... keys) {
        int ec = 0;
        ec |= hid_keyboard(keys);
        ec |= hid_keyboard();
        return ec;
    }

    /* String to code conversion tables */
    private static final String MP_ALPHA = "abcdefghijklmnopqrstuvwxyz";        // 0x04
    private static final String MP_ALPHA_ALT = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";    // 0x04 SHIFT
    private static final String MP_NUM = "1234567890";                          // 0x1E
    private static final String MP_NUM_ALT = "!@#$%^&*()";                      // 0x1E SHIFT
    private static final String MP_SPEC = " -=[]\\#;'`,./";                     // 0x2C
    private static final String MP_SPEC_ALT = " _+{}| :\"~<>?";                 // 0x2C SHIFT

    private static final String[] AP_ATT = {MP_ALPHA, MP_ALPHA_ALT, MP_NUM, MP_NUM_ALT, MP_SPEC, MP_SPEC_ALT};
    private static final boolean[] AP_SHIFT = {false, true, false, true, false, true};
    private static final byte[] AP_OFFSET = {0x04, 0x04, 0x1E, 0x1E, 0x2C, 0x2C};

    public int send_string(String s) {
        int ec = 0;
        for (char c : s.toCharArray()) {
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
            if (idx == AP_ATT.length)
                throw new IllegalArgumentException("Given string contains illegal characters");

            ec |= hid_keyboard(shift ? Input.Keyboard.ModifierMask.LSHIFT.code : 0, code);
        }
        ec |= hid_keyboard();
        return ec;
    }

    public KeyboardLightListener getKeyboardLightListener(){
        return mKeyboardLightListener;
    }

    public class KeyboardLightListener {
        private Process mKeyboardLightProc;
        private InputStream mKeyboardLightStream;

        public int start() {
            if (mKeyboardLightProc != null)
                throw new IllegalArgumentException("Keyboard light proc already running");

            mKeyboardLightProc = SUExecute.execute("cat " + mDevKeyboard);
            if (mKeyboardLightProc != null) {
                mKeyboardLightStream = mKeyboardLightProc.getInputStream();
                return 0;
            } else {
                return 1;
            }
        }

        /**
         * Bitmask of light states:
         * NUM      0x01
         * CAPS     0x02
         * SCROLL   0x04
         *
         * @return bitmask of light states
         */
        public int read() {
            try {
                if (mKeyboardLightStream != null)
                    return mKeyboardLightStream.read();
                return -1;
            } catch (IOException e) {
                Log.d(MainActivity.TAG, "Light stream forcibly terminated");
                return -1;
            }
        }

        public void kill() {
            try {
                mKeyboardLightProc.destroy();
                mKeyboardLightProc = null;
                mKeyboardLightStream.close();
                mKeyboardLightStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}