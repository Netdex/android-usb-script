package cf.netdex.hidfuzzer.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import cf.netdex.hidfuzzer.hid.HID;
import cf.netdex.hidfuzzer.hid.Input;
import cf.netdex.hidfuzzer.util.Func;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/15/2017.
 */

public class FuzzerTask extends HIDTask {

    public FuzzerTask(Context context, Func<RunState> update) {
        super(context, update);
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
                while (HID.hid_keyboard(sh, DEV_KEYBOARD, (byte) 0, Input.Keyboard.Key.VOLUME_UP.code) != 0) {
                    Thread.sleep(1000);
                }
                publishProgress(RunState.RUNNING);
                toast("Connected");

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
                toast("Disconnected");
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        // tidy up sh so no running tasks are left
        sh.kill();
        sh.close();
    }


}
