package cf.netdex.hidfuzzer.ltask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import cf.netdex.hidfuzzer.R;

/**
 * Created by netdex on 12/30/17.
 */

public class DialogIO {
    private WeakReference<Context> mContext;
    private WeakReference<ScrollView> mScrollView;
    private WeakReference<TextView> mLogView;
    private WeakReference<ToggleButton> mToggleButton;

    public DialogIO(Context context){
        this.mContext = new WeakReference<Context>(context);
        this.mLogView = new WeakReference<>(((Activity) context).findViewById(R.id.txtLog));
        this.mScrollView = new WeakReference<>(((Activity) context).findViewById(R.id.scrollview));
        this.mToggleButton = new WeakReference<>(((Activity) context).findViewById(R.id.btnPoll));
    }

    /**
     * Puts a log on screen
     *
     * @param s Log message to send
     */
    public void log(final String s) {
        looper(() -> {
//                Toast.makeText(HIDTask.this.getContext(), s, Toast.LENGTH_SHORT).show();
            SpannableStringBuilder ssb = new SpannableStringBuilder(mLogView.get().getText());
            ssb.append(Html.fromHtml(s)).append("\n");
            mLogView.get().setText(ssb, TextView.BufferType.SPANNABLE);

            mScrollView.get().post(() -> mScrollView.get().fullScroll(View.FOCUS_DOWN));
        });
    }

    public void clear() {
        looper(() -> {
            mLogView.get().setText("");
        });
    }

    public boolean should(final String title, final String prompt) {
        final boolean[] mResult = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(1);

        looper(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
            builder.setTitle(title);

            final TextView txtPrompt = new TextView(mContext.get());
            txtPrompt.setPadding(40, 40, 40, 40);
            txtPrompt.setText(prompt);
            builder.setView(txtPrompt);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mResult[0] = true;
                    latch.countDown();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mResult[0] = false;
                    latch.countDown();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mResult[0] = false;
                    latch.countDown();
                }
            });
            builder.setCancelable(false);
            builder.show();
        });
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        return mResult[0];
    }

    String ask(final String title) {
        return ask(title, "");
    }

    /**
     * Prompts user for information
     *
     * @param title Title of prompt
     * @param def   Default value of prompt
     * @return prompt response
     */
    public String ask(final String title, final String def) {
        final String[] m_Text = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);

        looper(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
            builder.setTitle(title);

            final EditText input = new EditText(mContext.get());
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
        });
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        return m_Text[0];
    }

    /**
     * Pops an alert dialog
     *
     * @param title Title of alert
     * @param msg   Message of alert
     */
    public void say(final String title, final String msg) {
        final CountDownLatch latch = new CountDownLatch(1);

        looper(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext.get());
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

    /**
     * Runs code in context thread
     *
     * @param r Runnable to run
     */
    private void looper(Runnable r) {
        Handler handler = new Handler(mContext.get().getMainLooper());
        handler.post(r);
    }

    public WeakReference<Context> getContext() {
        return mContext;
    }

    public WeakReference<ScrollView> getScrollView() {
        return mScrollView;
    }

    public WeakReference<TextView> getLogView() {
        return mLogView;
    }

    public WeakReference<ToggleButton> getModeButton() {
        return mToggleButton;
    }
}
