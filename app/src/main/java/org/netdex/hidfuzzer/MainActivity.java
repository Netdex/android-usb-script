package org.netdex.hidfuzzer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import org.netdex.hidfuzzer.gui.ConfirmDialog;
import org.netdex.hidfuzzer.gui.PromptDialog;
import org.netdex.hidfuzzer.service.LuaUsbService;
import org.netdex.hidfuzzer.service.LuaUsbServiceConnection;
import org.netdex.hidfuzzer.task.AsyncIOBridge;
import org.netdex.hidfuzzer.task.LuaUsbTask;
import org.netdex.hidfuzzer.task.LuaUsbTaskFactory;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "hidfuzzer";

    private LuaUsbServiceConnection activeServiceConn_;
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

        AsyncIOBridge dialogIO = new AsyncIOBridge() {
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

        luaUsbTaskFactory_ = new LuaUsbTaskFactory(dialogIO);

        btnCancel_.setOnClickListener(v -> terminateLuaUsbService());
    }

    public void createLuaUsbService(LuaUsbTask task) {
        if (activeServiceConn_ != null) {
            Toast.makeText(this, "A task is already running", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent serviceIntent = new Intent(this, LuaUsbService.class);
        activeServiceConn_ =
                new LuaUsbServiceConnection(task, () -> {
                    activeServiceConn_ = null;
                    handler_.post(() -> btnCancel_.setEnabled(false)); // TODO need to unbind...
                });
        bindService(serviceIntent, activeServiceConn_, BIND_AUTO_CREATE);
        btnCancel_.setEnabled(true);
    }

    public void terminateLuaUsbService() {
        if (activeServiceConn_ != null) {
            btnCancel_.setEnabled(false);
            unbindService(activeServiceConn_); // TODO this can cause ANR
        }
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
                createLuaUsbService(luaUsbTaskFactory_.createTaskFromLuaAsset(
                        MainActivity.this, name, path));
                break;
            case PICK_LUA_SCRIPT:
                Uri uri = data.getData();
                createLuaUsbService(luaUsbTaskFactory_.createTaskFromLuaScript(
                        MainActivity.this, uri.getLastPathSegment(), uri));
                break;
        }
    }
}
