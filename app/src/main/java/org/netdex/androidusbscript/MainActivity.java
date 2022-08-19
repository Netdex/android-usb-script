package org.netdex.androidusbscript;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import org.netdex.androidusbscript.gui.ConfirmDialog;
import org.netdex.androidusbscript.gui.PromptDialog;
import org.netdex.androidusbscript.service.LuaUsbService;
import org.netdex.androidusbscript.service.LuaUsbServiceConnection;
import org.netdex.androidusbscript.task.LuaIOBridge;
import org.netdex.androidusbscript.task.LuaUsbTask;
import org.netdex.androidusbscript.task.LuaUsbTaskFactory;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "android-usb-script";

    private LuaUsbServiceConnection luaUsbSvcConn_;
    private LuaUsbService luaUsbSvc_;
    private LuaUsbTaskFactory luaUsbTaskFactory_;
    private Handler handler_;

    private Button btnCancel_;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler_ = HandlerCompat.createAsync(Looper.getMainLooper());

        btnCancel_ = findViewById(R.id.btn_cancel);
        TextView logView = findViewById(R.id.text_log);
        ScrollView scrollView = findViewById(R.id.scrollview);

        LuaIOBridge dialogIO = new LuaIOBridge() {
            @Override
            public void onLogMessage(String s) {
                handler_.post(() -> {
                    logView.append(Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT));
                    logView.append("\n");
                    scrollView.fullScroll(View.FOCUS_DOWN);
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

        btnCancel_.setOnClickListener(v -> stopActiveTask());

        luaUsbTaskFactory_ = new LuaUsbTaskFactory(dialogIO);

        Intent serviceIntent = new Intent(this, LuaUsbService.class);
        luaUsbSvcConn_ = new LuaUsbServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                super.onServiceConnected(name, binder);
                luaUsbSvc_ = luaUsbSvcConn_.getService();
                LuaUsbService.Callback callback = new LuaUsbService.Callback() {
                    @Override
                    public void onTaskCompleted(LuaUsbTask task) {
                        handler_.post(() -> {
                            btnCancel_.setEnabled(false);
                        });
                    }
                };
                luaUsbSvc_.setCallback(callback);
            }
        };
        bindService(serviceIntent, luaUsbSvcConn_, BIND_AUTO_CREATE);
    }

    public void submitTask(LuaUsbTask task) {
        if (luaUsbSvc_.submitTask(task)) {
            btnCancel_.setEnabled(true);
        } else {
            Toast.makeText(this, "A task is already running", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopActiveTask() {
        luaUsbSvc_.stopActiveTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_asset:
                openLuaAsset();
                return true;
            case R.id.action_script:
                openLuaScript();
                return true;
        }
        return false;
    }

    private static final int PICK_LUA_ASSET = 2;
    private static final int PICK_LUA_SCRIPT = 3;

    private void openLuaAsset() {
        Intent intent = new Intent(this, SelectAssetActivity.class);
        startActivityForResult(intent, PICK_LUA_ASSET);
    }

    private void openLuaScript() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_LUA_SCRIPT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        switch (requestCode) {
            case PICK_LUA_ASSET:
                String name = data.getStringExtra("name");
                String path = data.getStringExtra("path");
                submitTask(luaUsbTaskFactory_.createTaskFromLuaAsset(
                        MainActivity.this, name, path));
                break;
            case PICK_LUA_SCRIPT:
                Uri uri = data.getData();
                submitTask(luaUsbTaskFactory_.createTaskFromLuaScript(
                        MainActivity.this, uri.getLastPathSegment(), uri));
                break;
        }
    }
}
