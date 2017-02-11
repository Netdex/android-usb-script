package cf.netdex.hidfuzzer.task;

import android.content.Context;

import cf.netdex.hidfuzzer.hid.HID;
import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 2/10/2017.
 * <p>
 * Task for receiving data encoded in parallel from flickering the keyboard lights.
 * <p>
 * Uses numlock light as the clock signal, and uses capslock and scrolllock lights for 2 data lines.
 * The client sends the binary message by sending HID output reports to the hid device (the phone),
 * or by simulating keypresses to trigger the output reports.
 * The binary data is sent two bits at a time per cycle, the serial task then puts the bits together
 * and strings them together into a message, using newline (0x10) as the acknowledgement of the end
 * of message.
 * Achieves ~5 bytes/second, an average of 11 keypresses are required to transfer each byte.
 * This is not optimal, in fact an implementation with just toggling the lights singularly would
 * probably only require 6, but is more error-prone. The idea also just occurred to me as I was
 * typing out this documentation so I guess it's too late to implement it.
 * <p>
 * Faster speeds (10 kb/s) could be achieved with feature requests, but that would require modifying
 * the kernel further, which would make this no longer compatible with the hid-keyboard-gadget patch.
 * <p>
 * Example client code in c# available here:
 * https://my.mixtape.moe/qlojlx.txt
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
                                buffer.append((char) parcel);
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