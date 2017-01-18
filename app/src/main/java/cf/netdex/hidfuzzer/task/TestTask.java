package cf.netdex.hidfuzzer.task;

import android.content.Context;
import android.util.Log;

import cf.netdex.hidfuzzer.hid.HID;
import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/16/2017.
 */

public class TestTask extends HIDTask {

    public TestTask(Context context) {
        super(context);
    }

    @Override
    public void run() {
        Shell.Interactive sh = this.getSU();
        if (sh == null) return;

        final HIDR h = this.getHIDR();
        new Thread() {
            public void run() {
                h.getKeyboardLightListener().start();
                while (!isCancelled()) {
                    Log.d("A", String.format("%02x", h.getKeyboardLightListener().read()));
                }
            }
        }.start();

        while (!isCancelled()) {
            publishProgress(RunState.IDLE);
            // poll until /dev/hidg0 is writable
            while (!isCancelled() && HID.hid_keyboard(sh, DEV_KEYBOARD, (byte) 0, Input.KB.K.VOLUME_UP.c) != 0) {
                h.delay(1000);
            }
            publishProgress(RunState.RUNNING);

            int c = 0;
            while (c == 0 && !isCancelled()) {
                c |= h.send_string("wow carol you're such a meme");
                c |= h.press_keys((byte) 0, Input.KB.K.ENTER.c);
                h.delay(100);
            }
            toast("Disconnected");
        }

    }


}
