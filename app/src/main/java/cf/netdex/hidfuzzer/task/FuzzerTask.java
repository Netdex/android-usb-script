package cf.netdex.hidfuzzer.task;

import android.content.Context;

import java.util.Random;

import cf.netdex.hidfuzzer.hid.HID;
import cf.netdex.hidfuzzer.hid.Input;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/15/2017.
 */

public class FuzzerTask extends HIDTask {

    public FuzzerTask(Context context) {
        super(context, "Fuzzes the HID interface.");
    }

    @Override
    public void run() {
        Shell.Interactive sh = this.getSU();
        if (sh == null) return;
        Random r = new Random();
        try {
            while (!isCancelled()) {
                publishProgress(RunState.IDLE);
                // poll until /dev/hidg0 is writable
                while (!isCancelled() && HID.hid_keyboard(sh, DEV_KEYBOARD, (byte) 0, Input.KB.K.VOLUME_UP.c) != 0) {
                    Thread.sleep(1000);
                }
                publishProgress(RunState.RUNNING);

                // fuzzing begins here
                byte[] kbuf = new byte[7];
                byte[] mbuf = new byte[4];
                int c = 0;
                while (c == 0 && !isCancelled()) {
                    r.nextBytes(kbuf);
                    r.nextBytes(mbuf);
                    c |= HID.hid_keyboard(sh, DEV_KEYBOARD, kbuf);
                    c |= HID.hid_mouse(sh, DEV_MOUSE, mbuf);
//                    c |= HID.hid_mouse(sh, "/dev/hidg1", (byte) 0, (byte) 10);
//                    Thread.sleep(1000);
                }
                log("Disconnected");
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
