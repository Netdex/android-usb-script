package org.netdex.hidfuzzer.configfs.function;

import java.io.IOException;
import java.util.ArrayList;

import org.netdex.hidfuzzer.util.Command;

import eu.chainfire.libsuperuser.Shell;

/**
 * https://www.kernel.org/doc/Documentation/usb/mass-storage.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/configfs-usb-gadget-mass-storage
 */
public class UsbGadgetFunctionMassStorage extends UsbGadgetFunction {
    public static class Parameters extends UsbGadgetFunction.Parameters {
        public String file;
        public boolean ro = false;
        public boolean removable = true;
        public boolean cdrom = false;
        public boolean nofua = false;

        public boolean stall = true;

        public long size = 256;   // in MB, ignored if the file already exists

        public static final Parameters DEFAULT = new Parameters(
                "/data/local/tmp/mass_storage-lun0.img", 256);

        public Parameters(String file, boolean ro, boolean removable,
                          boolean cdrom, boolean nofua, boolean stall, long size) {
            this.file = file;
            this.ro = ro;
            this.removable = removable;
            this.cdrom = cdrom;
            this.nofua = nofua;
            this.stall = stall;
            this.size = size;
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
    public void create(Shell.Threaded su, String gadgetPath) throws Shell.ShellDiedException {
        super.create(su, gadgetPath);

        Parameters params = (Parameters) this.params_;
        String functionDir = getFunctionDir();

        int exitCode;
        if (!Command.pathExists(su, params.file)) {
            // TODO: This is kind of dangerous, we should probably drop privileges for this
            exitCode = su.run(String.format("dd bs=1048576 count=%d if=/dev/zero of=\"%s\"",
                    params.size, params.file));
            if (exitCode != 0) {
                throw new IllegalArgumentException(
                        String.format("Failed to create image \"%s\"", params.file));
            }
        }
        su.run(Command.echoToFile(params.stall ? 1 : 0,
                String.format("functions/%s/stall", functionDir)));

        final String lunName = "lun.0";
        su.run(Command.echoToFile(params.file,
                String.format("functions/%s/%s/file", functionDir, lunName)));
        su.run(Command.echoToFile(params.ro ? 1 : 0,
                String.format("functions/%s/%s/ro", functionDir, lunName)));
        su.run(Command.echoToFile(params.removable ? 1 : 0,
                String.format("functions/%s/%s/removable", functionDir, lunName)));
        su.run(Command.echoToFile(params.cdrom ? 1 : 0,
                String.format("functions/%s/%s/cdrom", functionDir, lunName)));
        su.run(Command.echoToFile(params.nofua ? 1 : 0,
                String.format("functions/%s/%s/nofua", functionDir, lunName)));
    }

    @Override
    public String getFunctionDir() {
        return String.format("mass_storage.usb%d", this.id_);
    }
}