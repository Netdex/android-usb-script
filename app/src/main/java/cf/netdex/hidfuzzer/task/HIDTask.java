package cf.netdex.hidfuzzer.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import cf.netdex.hidfuzzer.MainActivity;
import cf.netdex.hidfuzzer.util.Func;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/16/2017.
 */

public abstract class HIDTask extends AsyncTask<Void, HIDTask.RunState, Void> {

    static final String DEV_KEYBOARD = "/dev/hidg0";
    static final String DEV_MOUSE = "/dev/hidg1";

    private Context mContext;
    private Func<RunState> mUpdate;

    public HIDTask(Context context, Func<RunState> update) {
        this.mContext = context;
        this.mUpdate = update;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        publishProgress(RunState.STOPPED);
        run();
        publishProgress(RunState.STOPPED);
        return null;
    }

    public abstract void run();

    @Override
    protected void onProgressUpdate(RunState... s) {
        mUpdate.run(s);
    }

    @Override
    protected void onCancelled() {
        onProgressUpdate(RunState.STOPPED);
    }

    Shell.Interactive getSU() {
        try {        // setup su shell
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
                                Log.e(MainActivity.TAG, "Failed to open SU");
                                root[0] = false;
                            } else {
                                root[0] = true;
                            }
                            latch.countDown();
                        }
                    });
            latch.await();
            if (!root[0]) return null;
            return sh;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void toast(final String s) {
        Handler handler = new Handler(this.getContext().getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(HIDTask.this.getContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public enum RunState {
        RUNNING,
        IDLE,
        STOPPED
    }
}
