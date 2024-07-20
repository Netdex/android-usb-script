package org.netdex.androidusbscript.configfs.function

import org.netdex.androidusbscript.configfs.UsbGadget
import org.netdex.androidusbscript.util.FileSystem
import java.io.IOException
import java.nio.file.Paths

class HidParameters(
    val protocol: Int,
    val subclass: Int,
    val reportLength: Int,
    val descriptor: ByteArray
) : FunctionParameters()

/**
 * https://www.kernel.org/doc/Documentation/usb/gadget_hid.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/configfs-usb-gadget-hid
 */
class UsbGadgetFunctionHid(usbGadget: UsbGadget, id: Int, params: HidParameters) :
    UsbGadgetFunction(usbGadget, id, params) {

    override val name: String get() = "hid.usb$id"

    @Throws(IOException::class)
    public override fun create() {
        super.create()
        val params = params as HidParameters
        fs.write(params.protocol, functionPath.resolve("protocol"))
        fs.write(params.subclass, functionPath.resolve("subclass"))
        fs.write(params.reportLength, functionPath.resolve("report_length"))
        fs.write(params.descriptor, functionPath.resolve("report_desc"))
    }

    fun getMinor(): Int {
        return Integer.parseInt(getAttribute("dev").split(":")[1])
    }
}
