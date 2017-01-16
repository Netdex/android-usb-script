package cf.netdex.hidfuzzer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/15/2017.
 */

class FuzzerTask extends AsyncTask<Void, FuzzerTask.FuzzState, Void> {

    private Context mContext;

    FuzzerTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        publishProgress(FuzzState.STOPPED);

        // setup su shell
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
                            toast("Failed to open SU");
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
                publishProgress(FuzzState.IDLE);
                // poll until /dev/hidg0 is writable
                while (HID.hid_keyboard(sh, "/dev/hidg0", (byte) 0, Input.Keyboard.Key.VOLUME_UP.code) != 0) {
                    Thread.sleep(1000);
                }
                publishProgress(FuzzState.FUZZING);
                toast("Connected");

                // fuzzing begins here
                byte[] kbuf = new byte[8];
                byte[] mbuf = new byte[4];
                int c = 0;
                while (c == 0 && !isCancelled()) {
                    r.nextBytes(kbuf);
                    r.nextBytes(mbuf);
                    c |= HID.hid_keyboard(sh, "/dev/hidg0", kbuf);
                    c |= HID.hid_mouse(sh, "/dev/hidg1", mbuf);
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
        publishProgress(FuzzState.STOPPED);
        return null;
    }

    @Override
    protected void onCancelled() {
        onProgressUpdate(FuzzState.STOPPED);
    }

    @Override
    protected void onProgressUpdate(FuzzState... s) {

    }

    private void toast(final String s) {
        Handler handler = new Handler(mContext.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    enum FuzzState {
        FUZZING,
        IDLE,
        STOPPED
    }

}
