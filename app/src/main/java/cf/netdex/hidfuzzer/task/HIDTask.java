package cf.netdex.hidfuzzer.task;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import cf.netdex.hidfuzzer.MainActivity;
import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.util.SUExecute;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/16/2017.
 */

public abstract class HIDTask extends AsyncTask<Void, HIDTask.RunState, Void> {

    static final String DEV_KEYBOARD = "/dev/hidg0";
    static final String DEV_MOUSE = "/dev/hidg1";

    private Context mContext;

    private Shell.Interactive mSU;
    private HIDR mH;

    private String mDesc;

    public HIDTask(Context context, String desc) {
        this.mContext = context;
        this.mDesc = desc;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        mSU = createSU();
        if (mSU != null) {
            mSU.addCommand("chmod 666 " + DEV_KEYBOARD);
            mSU.addCommand("chmod 666 " + DEV_MOUSE);
            mH = new HIDR(mSU, DEV_KEYBOARD, DEV_MOUSE);
            say("Description", mDesc);
            toast("Started " + this.getClass().getSimpleName());
            run();
            toast("Ended " + this.getClass().getSimpleName());
        } else {
            toast("Failed to get SU");
        }
        return null;
    }

    @Override
    public void onProgressUpdate(RunState... s) {
        toast(this.getClass().getSimpleName() + ": " + s[0].name());
    }

    public abstract void run();

    @Override
    protected void onCancelled() {
        cleanup();
    }

    @Override
    protected void onPostExecute(Void result) {
        cleanup();
    }

    public void cleanup() {
        if (mH != null)
            mH.getKeyboardLightListener().kill();
        if (mSU != null) {
            mSU.kill();
            mSU.close();
        }
    }

    public Shell.Interactive getSU() {
        return mSU;
    }

    public HIDR getHIDR() {
        return mH;
    }

    static Shell.Interactive createSU() {
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
        looper(new Runnable() {
            public void run() {
                Toast.makeText(HIDTask.this.getContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    String ask(final String title){
        return ask(title, "");
    }

    String ask(final String title, final String def) {
        final String[] m_Text = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);

        looper(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(title);

                final EditText input = new EditText(mContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(def);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text[0] = input.getText().toString();
                        latch.countDown();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        latch.countDown();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        return m_Text[0];
    }

    void say(final String title, final String msg) {
        final CountDownLatch latch = new CountDownLatch(1);

        looper(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setTitle(title);
                alertDialog.setMessage(msg);
                alertDialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                latch.countDown();
                            }
                        });
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        latch.countDown();
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }

    private void looper(Runnable r) {
        Handler handler = new Handler(this.getContext().getMainLooper());
        handler.post(r);
    }

    public enum RunState {
        RUNNING,
        IDLE,
        STOPPED,
        DONE
    }
}
