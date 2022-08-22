package org.netdex.androidusbscript;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

    private NotificationBroadcastReceiver notificationBroadcastReceiver_;

    private Handler handler_;
    private ActivityResultLauncher<Intent> selectAssetLauncher_;
    private ActivityResultLauncher<Intent> selectScriptLauncher_;

    private LuaUsbServiceConnection luaUsbSvcConn_;
    private LuaUsbService luaUsbSvc_;
    private LuaUsbTaskFactory luaUsbTaskFactory_;

    private Button btnCancel_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler_ = HandlerCompat.createAsync(Looper.getMainLooper());

        btnCancel_ = findViewById(R.id.btn_cancel);
        TextView logView = findViewById(R.id.text_log);
        ScrollView scrollView = findViewById(R.id.scrollview);

        selectAssetLauncher_ = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onSelectLuaAsset);
        selectScriptLauncher_ = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onSelectLuaScript);

        notificationBroadcastReceiver_ = new NotificationBroadcastReceiver();
        IntentFilter filter = new IntentFilter(NotificationBroadcastReceiver.ACTION_STOP);
        registerReceiver(notificationBroadcastReceiver_, filter);

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
            public boolean onConfirm(String title, String message) {
                return new ConfirmDialog(MainActivity.this, title, message).show();
            }

            @Override
            public String onPrompt(String title, String message, String hint, String def) {
                return new PromptDialog(MainActivity.this, title, message, hint, def).show();
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
                LuaUsbService.Callback callback = task -> handler_.post(() -> {
                    btnCancel_.setEnabled(false);
                });
                luaUsbSvc_.setCallback(callback);
            }
        };
        bindService(serviceIntent, luaUsbSvcConn_, BIND_AUTO_CREATE);
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationBroadcastReceiver_);
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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_asset):
                openLuaAsset();
                return true;
            case (R.id.action_script):
                openLuaScript();
                return true;
        }
        return false;
    }

    private static final int PICK_LUA_SCRIPT = 3;

    private void openLuaAsset() {
        Intent intent = new Intent(this, SelectAssetActivity.class);
        selectAssetLauncher_.launch(intent);
    }

    private void openLuaScript() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        setResult(PICK_LUA_SCRIPT, intent);
        selectScriptLauncher_.launch(intent);
    }

    protected void onSelectLuaAsset(ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK)
            return;
        Intent data = result.getData();

        String name = data.getStringExtra("name");
        String path = data.getStringExtra("path");
        submitTask(luaUsbTaskFactory_.createTaskFromLuaAsset(
                MainActivity.this, name, path));
    }

    protected void onSelectLuaScript(ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK)
            return;
        Intent data = result.getData();

        Uri uri = data.getData();
        submitTask(luaUsbTaskFactory_.createTaskFromLuaScript(
                MainActivity.this, uri.getLastPathSegment(), uri));
    }

    public LuaUsbService getLuaUsbService() {
        return luaUsbSvc_;
    }
}
