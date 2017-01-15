package cf.netdex.hidfuzzer;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/15/2017.
 */

public class FuzzerTask extends AsyncTask<Void, String, Void> {

    private Context mContext;

    public FuzzerTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        final CountDownLatch latch = new CountDownLatch(1);

        final boolean[] root = new boolean[1];
        Shell.Interactive sh = new Shell.Builder()
                .useSU()
                .setWantSTDERR(true)
                .setWatchdogTimeout(5)
                .setMinimalLogging(true)
                .open(new Shell.OnCommandResultListener() {
                    @Override
                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                        if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING) {
                            publishProgress("Failed to open SU");
                            root[0] = false;
                        } else {
                            root[0] = true;
                        }
                        latch.countDown();
                    }
                });
        Random r = new Random();
        try {
            latch.await();
            if (!root[0]) return null;

            while (!isCancelled()) {
                while (HID.hid_keyboard(sh, "/dev/hidg0", (byte) 0, Input.Keyboard.Key.VOLUME_UP.code) != 0) {
                    Thread.sleep(1000);
                }
                publishProgress("Connected");
                byte[] kbuf = new byte[8];
                byte[] mbuf = new byte[4];
                int c = 0;
                while (c == 0) {
                    r.nextBytes(kbuf);
                    r.nextBytes(mbuf);
                    c |= HID.hid_keyboard(sh, "/dev/hidg0", kbuf);
                    c |= HID.hid_mouse(sh, "/dev/hidg1", mbuf);
//                    Thread.sleep(1000);
                }
                publishProgress("Disconnected");
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected void onProgressUpdate(String... s) {
        Toast.makeText(mContext, s[0], Toast.LENGTH_SHORT).show();
    }


}
