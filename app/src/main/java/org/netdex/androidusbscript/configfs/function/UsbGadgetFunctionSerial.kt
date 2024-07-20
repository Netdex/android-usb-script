package org.netdex.androidusbscript.configfs.function

import org.netdex.androidusbscript.configfs.UsbGadget

class SerialParameters(

) : FunctionParameters()

/**
 * https://www.kernel.org/doc/Documentation/usb/gadget_serial.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/configfs-usb-gadget-serial
 */
class UsbGadgetFunctionSerial(usbGadget: UsbGadget, id: Int, params: SerialParameters) :
    UsbGadgetFunction(usbGadget, id, params) {

    override val name: String get() = "gser.usb$id"

    fun getPortNum(): Int {
        return Integer.parseInt(getAttribute("port_num"))
    }
}