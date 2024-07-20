package org.netdex.androidusbscript.configfs

import org.netdex.androidusbscript.configfs.function.UsbGadgetFunction
import org.netdex.androidusbscript.util.FileSystem
import timber.log.Timber
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

class UsbGadgetParameters(
    val manufacturer: String,
    val idProduct: String,
    val idVendor: String,
    val product: String,
    val configName: String
)

class UsbGadget(
    val fs: FileSystem,
    private val gadgetName: String,
    private val configFsPath: Path
) : AutoCloseable {

    private var functions: Array<UsbGadgetFunction>? = null

    @Throws(IOException::class)
    fun create(params: UsbGadgetParameters, functions: Array<UsbGadgetFunction>) {
        this.functions = functions

        Timber.d("Creating USB gadget '%s'", this.gadgetName)

        val gadgetPath = getGadgetPath(gadgetName)
        if (!isSupported()) throw UnsupportedOperationException("Device does not support ConfigFS")
        check(!isCreated()) { "USB gadget already exists" }

        fs.mkdir(gadgetPath)
        fs.write(params.idProduct, gadgetPath.resolve("idProduct"))
        fs.write(params.idVendor, gadgetPath.resolve("idVendor"))
        fs.write("239", gadgetPath.resolve("bDeviceClass"))
        fs.write("0x02", gadgetPath.resolve("bDeviceSubClass"))
        fs.write("0x01", gadgetPath.resolve("bDeviceProtocol"))

        fs.mkdir(gadgetPath.resolve("strings/0x409"))
        fs.write(serial(functions), gadgetPath.resolve("strings/0x409/serialnumber"))
        fs.write(params.manufacturer, gadgetPath.resolve("strings/0x409/manufacturer"))
        fs.write(params.product, gadgetPath.resolve("strings/0x409/product"))

        val configPath = getConfigPath()
        fs.mkdir(configPath)
        fs.mkdir(configPath.resolve("strings/0x409"))
        fs.write(params.configName, configPath.resolve("strings/0x409/configuration"))

        for (function in functions) {
            function.add()
        }
    }

    @Throws(IOException::class)
    fun bind() {
        Timber.d("Binding USB gadget '%s'", this.gadgetName)
        check(isCreated()) { "USB gadget does not exist" }
        check(!isBound()) { "USB gadget is already bound to UDC" }

        val udc = getSystemUDC(fs)
        check(udc.isNotEmpty()) { "Could not determine system UDC" }

        fs.write("", getUDCPath(SYSTEM_GADGET))
        fs.write(udc, getUDCPath(gadgetName))
    }

    @Throws(IOException::class)
    fun unbind() {
        Timber.d("Unbinding USB gadget '%s'", this.gadgetName)
        check(isCreated()) { "USB gadget does not exist" }
        check(isBound()) { "USB gadget is not bound to UDC" }

        fs.write("", getUDCPath(gadgetName))
        fs.write(getSystemUDC(fs), getUDCPath(SYSTEM_GADGET))
    }

    @Throws(IOException::class)
    fun destroy() {
        Timber.d("Destroying USB gadget '%s'", gadgetName)
        val gadgetPath = getGadgetPath()
        check(isCreated()) { "USB gadget does not exist" }

        for (function in this.functions!!) {
            function.remove()
        }

        val configPath = getConfigPath()
        fs.delete(configPath.resolve("strings/0x409"))
        fs.delete(configPath)
        fs.delete(gadgetPath.resolve("strings/0x409"))
        fs.delete(gadgetPath)
    }

    fun isSupported(): Boolean {
        return (fs.exists(configFsPath) && fs.getSystemProp("sys.usb.configfs").toInt() >= 1)
    }

    fun getGadgetPath(gadgetName: String? = null): Path {
        return configFsPath.resolve("usb_gadget").resolve(gadgetName ?: this.gadgetName)
    }

    fun getConfigPath(gadgetName: String? = null, configName: String? = null): Path {
        return getGadgetPath(gadgetName).resolve("configs").resolve(configName ?: CONFIG_DIR)
    }

    fun getUDCPath(gadgetName: String? = null): Path {
        return getGadgetPath(gadgetName).resolve("UDC")
    }

    @Throws(IOException::class)
    fun getActiveUDC(gadgetName: String?): String {
        return fs.readLine(getUDCPath(gadgetName))
    }

    fun isCreated(): Boolean {
        return fs.exists(getGadgetPath(gadgetName))
    }

    @Throws(IOException::class)
    fun isBound(): Boolean {
        return getActiveUDC(gadgetName).isNotEmpty()
    }

    fun serial(functions: Array<UsbGadgetFunction>): String {
        val functionNames = ArrayList<String>()
        for (function in functions) {
            functionNames.add(function.name)
        }
        return String.format("%x", functionNames.hashCode())
    }

    override fun close() {
        if (isCreated()) {
            if (isBound())
                unbind()
            destroy()
        }
    }

    companion object {
        // https://android.googlesource.com/platform/system/core/+/master/rootdir/init.usb.configfs.rc
        private const val SYSTEM_GADGET = "g1"
        private const val CONFIG_DIR = "c.1"

        @JvmStatic
        fun getSystemUDC(fs: FileSystem): String {
            return fs.getSystemProp("sys.usb.controller")
        }

        @Throws(IOException::class)
        @JvmStatic
        fun getUDCState(fs: FileSystem, udc: String?): String {
            return fs.readLine(Paths.get("/sys/class/udc", udc, "state"))
        }
    }
}