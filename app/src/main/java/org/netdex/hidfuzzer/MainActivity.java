package org.netdex.hidfuzzer;

import android.content.Intent;
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
import org.netdex.hidfuzzer.service.LuaUsbService;
import org.netdex.hidfuzzer.service.LuaUsbServiceConnection;
import org.netdex.hidfuzzer.task.AsyncIoBridge;
import org.netdex.hidfuzzer.task.LuaUsbTask;
import org.netdex.hidfuzzer.task.LuaUsbTaskFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "hidfuzzer";
    public static final String SCRIPT_PATH = "scripts";

    // I only made this map because I'm too lazy to make a custom list adapter
    static final HashMap<String, String> fTaskMap = new HashMap<>();
    static final ArrayList<String> fTaskSpinnerItems = new ArrayList<>();

    private LuaUsbTask mRunningTask;

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

        AsyncIoBridge dialogIO = new AsyncIoBridge() {
            @Override
            public void onLogMessage(String s) {
                handler.post(() -> {
                    logView.append(Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT));
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

        AtomicReference<LuaUsbServiceConnection> activeServiceConn = new AtomicReference<>();
        LuaUsbTaskFactory luaUsbTaskFactory = new LuaUsbTaskFactory(dialogIO);

        btnPoll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String selectedTask = (String) spnTask.getSelectedItem();
                String taskSrcFilePath = fTaskMap.get(selectedTask);

                Intent serviceIntent = new Intent(this, LuaUsbService.class);
                activeServiceConn.set(
                        new LuaUsbServiceConnection(
                                luaUsbTaskFactory.createTaskFromLuaFile(
                                        MainActivity.this, selectedTask, taskSrcFilePath),
                                () -> handler.post(() -> {
                                    btnPoll.setChecked(false);
                                    btnPoll.setEnabled(true);
                                })));
                bindService(serviceIntent, activeServiceConn.get(), BIND_AUTO_CREATE);
            } else {
                if (activeServiceConn != null) {
                    btnPoll.setEnabled(false);
                    unbindService(activeServiceConn.get());
                }
            }
        });
    }
}
