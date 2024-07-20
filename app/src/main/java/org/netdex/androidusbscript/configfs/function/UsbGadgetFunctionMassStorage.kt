package org.netdex.androidusbscript.configfs.function

import com.topjohnwu.superuser.Shell
import org.netdex.androidusbscript.configfs.UsbGadget
import org.netdex.androidusbscript.util.FileSystem
import java.io.IOException
import java.nio.file.Paths

class MassStorageParameters(
    val file: String,
    var ro: Boolean,
    var removable: Boolean,
    var cdrom: Boolean,
    var nofua: Boolean,
    var stall: Boolean, // in MB
    var size: Long,
    var label: String?, // the device is formatted as exFAT with this label
    var force: Boolean, // recreate device even if it exists
) : FunctionParameters()

/**
 * https://www.kernel.org/doc/Documentation/usb/mass-storage.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/configfs-usb-gadget-mass-storage
 */
class UsbGadgetFunctionMassStorage(usbGadget: UsbGadget, id: Int, params: MassStorageParameters) :
    UsbGadgetFunction(usbGadget, id, params) {

    @Throws(IOException::class)
    public override fun create() {
        super.create()

        val params = params as MassStorageParameters
        if (params.force || !fs.exists(Paths.get(params.file))) {
            // TODO: This is kind of dangerous, we should probably drop privileges for this
            val result = Shell.cmd(
                "dd bs=1048576 count=${params.size} if=/dev/zero of='${params.file}'",
                "mkfs.exfat -L ${params.size} '${params.file}'",
            ).exec()
            require(result.isSuccess) { "Failed to create image '${params.file}': errno=${result.code}" }
        }
        fs.write(if (params.stall) 1 else 0, functionPath.resolve("stall"))

        val lun = "lun.0"
        val lunPath = functionPath.resolve(lun)
        fs.write(params.file, lunPath.resolve("file"))
        fs.write(if (params.ro) 1 else 0, lunPath.resolve("ro"))
        fs.write(if (params.removable) 1 else 0, lunPath.resolve("removable"))
        fs.write(if (params.cdrom) 1 else 0, lunPath.resolve("cdrom"))
        fs.write(if (params.nofua) 1 else 0, lunPath.resolve("nofua"))
    }

    override val name: String
        get() = "mass_storage.usb" + this.id
}