package cf.netdex.hidfuzzer.task;

import android.content.Context;

import cf.netdex.hidfuzzer.hid.HID;
import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 2/10/2017.
 */

public class SerialTransferDebugTask extends HIDTask {

    public SerialTransferDebugTask(Context context) {
        super(context, "For testing serial transfer over flickering keyboard lights.");
    }

    @Override
    public void run() {
        Shell.Interactive sh = this.getSU();
        if (sh == null) return;

        final HIDR h = this.getHIDR();
        new Thread() {
            public void run() {
                h.getKeyboardLightListener().start();
                StringBuilder buffer = new StringBuilder();
                byte parcel = 0;
                int idx = 0;

                while (!isCancelled()) {
                    int kv = h.getKeyboardLightListener().read();
                    if ((kv & 0x1) == 1) {
                        byte data = (byte) (kv >> 1);
                        parcel |= data << (idx * 2);
                        //log("pk " + (kv >> 1) + " | " + Integer.toBinaryString(parcel) + " " + parcel);
                        idx++;
                        if (idx == 4) {
                            if (parcel == '\n') {
                                log(buffer.toString());
                                buffer.setLength(0);
                            } else {
                                buffer.append((char)parcel);
                                //log("recv " + (char)parcel);
                            }
                            idx = 0;
                            parcel = 0;
                        }
                    }
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
                c |= h.test();
                h.delay(2000);
            }
            log("Disconnected");
        }

    }


}