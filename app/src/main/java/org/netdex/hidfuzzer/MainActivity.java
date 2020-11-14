package org.netdex.hidfuzzer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import org.netdex.hidfuzzer.gui.ConfirmDialog;
import org.netdex.hidfuzzer.gui.PromptDialog;
import org.netdex.hidfuzzer.task.AsyncIOBridge;
import org.netdex.hidfuzzer.task.LuaHIDTask;
import org.netdex.hidfuzzer.task.LuaHIDTaskFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "hidfuzzer";
    public static final String SCRIPT_PATH = "scripts";

    // I only made this map because I'm too lazy to make a custom list adapter
    static final HashMap<String, String> fTaskMap = new HashMap<>();
    static final ArrayList<String> fTaskSpinnerItems = new ArrayList<>();

    private LuaHIDTask mRunningTask;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load scripts
        String[] scriptFilePaths;
        try {
            scriptFilePaths = getAssets().list(SCRIPT_PATH);

            for (String filePath : scriptFilePaths) {
                if (filePath.endsWith(".lua")) {
                    fTaskMap.put(filePath, SCRIPT_PATH + File.separator + filePath);
                    fTaskSpinnerItems.add(filePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());

        // sort out the UI
        ToggleButton btnPoll = findViewById(R.id.btnPoll);
        Spinner spnTask = findViewById(R.id.spnTask);
        TextView logView = findViewById(R.id.txtLog);
        ScrollView scrollView = findViewById(R.id.scrollview);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, fTaskSpinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTask.setAdapter(adapter);

        AsyncIOBridge dialogIO = new AsyncIOBridge() {
            @Override
            public void onLogMessage(String s) {
                handler.post(() -> {
                    logView.append(Html.fromHtml(s));
                    logView.append("\n");
                    scrollView.fullScroll(View.FOCUS_DOWN);
                });
            }

            @Override
            public void onLogClear() {
                handler.post(() -> {
                    logView.setText("");
                });
            }

            @Override
            public void onSignal(Signal signal) {
                handler.post(()->{
                    switch(signal){
                        case DONE:
                            btnPoll.setChecked(false);
                            btnPoll.setEnabled(true);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + signal);
                    }
                });
            }

            @Override
            public boolean onConfirm(String title, String prompt) {
                ConfirmDialog dialog = new ConfirmDialog(MainActivity.this, title, prompt);
                return dialog.show();
            }

            @Override
            public String onPrompt(String title, String def) {
                PromptDialog dialog = new PromptDialog(MainActivity.this, title, def);
                return dialog.show();
            }
        };

        LuaHIDTaskFactory luaHIDTaskFactory = new LuaHIDTaskFactory(dialogIO);

        ExecutorService executorService = Executors.newCachedThreadPool();

        btnPoll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String selectedTask = (String) spnTask.getSelectedItem();
                String taskSrcFilePath = fTaskMap.get(selectedTask);
                mRunningTask = luaHIDTaskFactory.createTaskFromLuaFile(this, selectedTask, taskSrcFilePath);
                executorService.execute(mRunningTask);
            } else {
                if (mRunningTask != null) {
                    btnPoll.setEnabled(false);
                    mRunningTask.interrupt();
                }
            }
        });
    }


}
