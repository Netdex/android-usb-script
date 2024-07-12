package org.netdex.androidusbscript.configfs.function;

import com.topjohnwu.superuser.Shell;

import org.netdex.androidusbscript.util.FileSystem;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * https://www.kernel.org/doc/Documentation/usb/mass-storage.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/configfs-usb-gadget-mass-storage
 */
public class UsbGadgetFunctionMassStorage extends UsbGadgetFunction {
    public static class Parameters extends UsbGadgetFunction.Parameters {
        public final String file;
        public boolean ro = false;
        public boolean removable = true;
        public boolean cdrom = false;
        public boolean nofua = false;

        public boolean stall = true;

        public long size = 256;   // in MB, ignored if the file already exists
        public String label;

        public Parameters(String file, boolean ro, boolean removable,
                          boolean cdrom, boolean nofua, boolean stall, long size, String label) {
            this.file = file;
            this.ro = ro;
            this.removable = removable;
            this.cdrom = cdrom;
            this.nofua = nofua;
            this.stall = stall;
            this.size = size;
            this.label = label;
        }

        public Parameters(String file) {
            this.file = file;
        }

        public Parameters(String file, long size) {
            this.file = file;
            this.size = size;
        }
    }

    public UsbGadgetFunctionMassStorage(int id, Parameters params) {
        super(id, params);
    }

    @Override
    public void create(FileSystem fs, String gadgetPath) throws IOException {
        super.create(fs, gadgetPath);

        Parameters params = (Parameters) this.params_;
        String functionDir = getName();

        if (!fs.exists(params.file)) {
            // TODO: This is kind of dangerous, we should probably drop privileges for this
            Shell.Result result = Shell.cmd(
                    String.format(Locale.US,
                            "dd bs=1048576 count=%d if=/dev/zero of=\"%s\"",
                            params.size, params.file),
                    String.format(Locale.US,
                            "mkfs.exfat -L \"%s\" \"%s\"",
                            params.size, params.file)
            ).exec();
            if (!result.isSuccess()) {
                throw new IllegalArgumentException(
                        String.format("Failed to create image \"%s\": errno=%d", params.file, result.getCode()));
            }
        }
        fs.write(params.stall ? 1 : 0, Paths.get(gadgetPath, "functions", functionDir, "stall").toString());

        final String lunName = "lun.0";
        fs.write(params.file, Paths.get(gadgetPath, "functions", functionDir, lunName, "file").toString());
        fs.write(params.ro ? 1 : 0, Paths.get(gadgetPath, "functions", functionDir, lunName, "ro").toString());
        fs.write(params.removable ? 1 : 0, Paths.get(gadgetPath, "functions", functionDir, lunName, "removable").toString());
        fs.write(params.cdrom ? 1 : 0, Paths.get(gadgetPath, "functions", functionDir, lunName, "cdrom").toString());
        fs.write(params.nofua ? 1 : 0, Paths.get(gadgetPath, "functions", functionDir, lunName, "nofua").toString());
    }

    @Override
    public String getName() {
        return "mass_storage.usb" + this.id_;
    }
}