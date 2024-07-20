package org.netdex.androidusbscript.task

/**
 * Created by netdex on 12/30/17.
 */
interface LuaIOBridge {
    fun onLogMessage(s: String)

    fun onConfirm(title: String, message: String): Boolean

    fun onPrompt(title: String, message: String, hint: String, def: String): String
}
