package org.netdex.androidusbscript

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import org.netdex.androidusbscript.gui.ConfirmDialog
import org.netdex.androidusbscript.gui.PromptDialog
import org.netdex.androidusbscript.service.LuaUsbService
import org.netdex.androidusbscript.service.LuaUsbServiceConnection
import org.netdex.androidusbscript.task.LuaIOBridge
import org.netdex.androidusbscript.task.LuaUsbTask
import org.netdex.androidusbscript.task.LuaUsbTaskFactory
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private var notificationBroadcastReceiver: NotificationBroadcastReceiver? = null

    private var selectAssetLauncher: ActivityResultLauncher<Intent>? = null
    private var selectScriptLauncher: ActivityResultLauncher<Intent>? = null

    private var luaUsbTaskFactory: LuaUsbTaskFactory? = null
    var luaUsbService: LuaUsbService? = null; private set

    private var btnCancel: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        setContentView(R.layout.activity_main)

        val handler = HandlerCompat.createAsync(Looper.getMainLooper())

        btnCancel = findViewById(R.id.btn_cancel)
        val logView = findViewById<TextView>(R.id.text_log)
        val scrollView = findViewById<ScrollView>(R.id.scrollview)

        selectAssetLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult -> this.onSelectLuaAsset(result) }
        selectScriptLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult -> this.onSelectLuaScript(result) }

        notificationBroadcastReceiver = NotificationBroadcastReceiver()
        val filter = IntentFilter(NotificationBroadcastReceiver.ACTION_STOP)
        ContextCompat.registerReceiver(
            this,
            notificationBroadcastReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        val dialogIO = object : LuaIOBridge {
            override fun onLogMessage(s: String) {
                handler.post {
                    logView.append(Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT))
                    logView.append("\n")
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }

            override fun onConfirm(title: String, message: String): Boolean {
                return ConfirmDialog(this@MainActivity, title, message).show()
            }

            override fun onPrompt(
                title: String, message: String, hint: String, def: String
            ): String {
                return PromptDialog(this@MainActivity, title, message, hint, def).show()
            }
        }

        btnCancel!!.setOnClickListener { stopActiveTask() }

        luaUsbTaskFactory = LuaUsbTaskFactory(dialogIO)

        val luaUsbSvcConn = object : LuaUsbServiceConnection() {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                super.onServiceConnected(name, binder)
                luaUsbService = service
                val callback = LuaUsbService.Callback { task: LuaUsbTask? ->
                    handler.post { btnCancel!!.setEnabled(false) }
                }
                luaUsbService!!.setCallback(callback)
            }
        }
        val serviceIntent = Intent(this, LuaUsbService::class.java)
        bindService(serviceIntent, luaUsbSvcConn, BIND_AUTO_CREATE)

        // https://developer.android.com/develop/ui/views/notifications/notification-permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
            }
            when {
                ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }

                else -> {
                    requestPermissionLauncher.launch(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationBroadcastReceiver)
    }

    private fun submitTask(task: LuaUsbTask?) {
        if (luaUsbService!!.submitTask(task!!)) {
            btnCancel!!.isEnabled = true
        } else {
            Toast.makeText(this, "A task is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopActiveTask() {
        luaUsbService!!.stopActiveTask()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_asset -> {
                openLuaAsset()
                return true
            }

            R.id.action_script -> {
                openLuaScript()
                return true
            }
        }
        return false
    }

    private fun openLuaAsset() {
        val intent = Intent(this, SelectAssetActivity::class.java)
        selectAssetLauncher!!.launch(intent)
    }

    private fun openLuaScript() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*")
        setResult(PICK_LUA_SCRIPT, intent)
        selectScriptLauncher!!.launch(intent)
    }

    private fun onSelectLuaAsset(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) return
        val data = result.data

        val name = data!!.getStringExtra("name")
        val path = data.getStringExtra("path")
        submitTask(
            luaUsbTaskFactory!!.createTaskFromLuaAsset(
                this@MainActivity, name, path
            )
        )
    }

    private fun onSelectLuaScript(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) return
        val data = result.data

        val uri = data!!.data
        submitTask(
            luaUsbTaskFactory!!.createTaskFromLuaScript(
                this@MainActivity, uri!!.lastPathSegment, uri
            )
        )
    }

    companion object {
        private const val PICK_LUA_SCRIPT = 3
    }
}
