package cf.netdex.hidfuzzer.task;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import cf.netdex.hidfuzzer.hid.HID;
import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import cf.netdex.hidfuzzer.util.Func;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/16/2017.
 */

public class TestTask extends HIDTask {

    public TestTask(Context context, Func<RunState> update) {
        super(context, update);
    }

    @Override
    public void run() {
        Shell.Interactive sh = this.getSU();
        if (sh == null) return;

        final HIDR h = new HIDR(sh, DEV_KEYBOARD, DEV_MOUSE);
        new Thread(){
            public void run(){
                h.getKeyboardLightListener().start();
                while(!isCancelled()){
                    Log.d("A", String.format("%02x", h.getKeyboardLightListener().read()));
                }
                Log.d("A", "ded");
            }
        }.start();
        try {
            while (!isCancelled()) {
                publishProgress(RunState.IDLE);
                // poll until /dev/hidg0 is writable
                while (HID.hid_keyboard(sh, DEV_KEYBOARD, (byte) 0, Input.Keyboard.Key.VOLUME_UP.code) != 0) {
                    Thread.sleep(1000);
                }
                publishProgress(RunState.RUNNING);
                toast("Connected");

                int c = 0;
                while (c == 0 && !isCancelled()) {
//                    c |= h.send_string("go FUCK yourself test123");
                    Thread.sleep(5000);
                }
                toast("Disconnected");
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        // tidy up sh so no running tasks are left
        h.getKeyboardLightListener().kill();
        sh.kill();
        sh.close();
    }

    @Override
    protected void onCancelled() {
        onProgressUpdate(RunState.STOPPED);
    }

}
