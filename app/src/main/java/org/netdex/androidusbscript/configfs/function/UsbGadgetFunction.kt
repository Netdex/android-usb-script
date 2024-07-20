package org.netdex.androidusbscript.configfs.function

import org.netdex.androidusbscript.configfs.UsbGadget
import org.netdex.androidusbscript.util.FileSystem
import timber.log.Timber
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

open class FunctionParameters

/**
 * https://www.kernel.org/doc/Documentation/usb/gadget_configfs.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/
 */
abstract class UsbGadgetFunction(
    private val usbGadget: UsbGadget,
    protected val id: Int,
    protected val params: FunctionParameters
) {
    abstract val name: String
    protected val fs: FileSystem get() = usbGadget.fs
    protected val functionPath: Path
        get() = usbGadget.getGadgetPath().resolve("functions").resolve(name)

    @Throws(IOException::class)
    fun add() {
        this.create()
        this.configure()
    }

    @Throws(IOException::class)
    fun remove() {
        this.unconfigure()
        this.destroy()
    }

    @Throws(IOException::class)
    protected open fun create() {
        check(!fs.exists(functionPath)) {
            "Function path '$functionPath' already exists"
        }
        Timber.d("Creating USB function '%s'", name)
        fs.mkdir(functionPath)
    }

    @Throws(IOException::class)
    protected fun configure() {
        check(fs.exists(functionPath)) { "Function path '$functionPath' does not exist" }
        Timber.d("Configuring USB function '%s'", name)
        val functionLinkPath = usbGadget.getConfigPath().resolve(name)
        fs.ln(functionLinkPath, functionPath)
    }

    @Throws(IOException::class)
    protected fun unconfigure() {
        val functionLinkPath = usbGadget.getConfigPath().resolve(name)
        check(fs.exists(functionLinkPath)) { "Function symlink '$functionLinkPath' does not exist" }
        Timber.d("Unconfiguring USB function '%s'", name)
        fs.delete(functionLinkPath)
    }

    @Throws(IOException::class)
    protected fun destroy() {
        check(fs.exists(functionPath)) { "Function path '$functionPath' does not exist" }
        Timber.d("Destroying USB function '%s'", name)
        fs.delete(functionPath)
    }

    @Throws(IOException::class)
    protected fun getAttribute(attrib: String?): String {
        check(fs.exists(functionPath)) { "Function path '$functionPath' does not exist" }
        return fs.readLine(functionPath.resolve(attrib))
    }
}
