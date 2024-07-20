package org.netdex.androidusbscript.configfs.function;

import org.netdex.androidusbscript.util.FileSystem;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * https://www.kernel.org/doc/Documentation/usb/gadget_hid.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/configfs-usb-gadget-hid
 */
public class UsbGadgetFunctionHid extends UsbGadgetFunction {
    public static class Parameters extends UsbGadgetFunction.Parameters {
        public final int protocol;
        public final int subclass;
        public final int reportLength;
        public final byte[] descriptor;

        public Parameters(int protocol, int subclass, int reportLength, byte[] descriptor) {
            this.protocol = protocol;
            this.subclass = subclass;
            this.reportLength = reportLength;
            this.descriptor = descriptor;
        }
    }

    public UsbGadgetFunctionHid(int id, Parameters params) {
        super(id, params);
    }

    @Override
    public String getName() {
        return "hid.usb" + id_;
    }

    @Override
    public void create(FileSystem fs, String gadgetPath) throws IOException {
        super.create(fs, gadgetPath);

        Parameters params = (Parameters) this.params_;
        String functionDir = getName();
        fs.write(params.protocol, Paths.get(gadgetPath, "functions", functionDir, "protocol").toString());
        fs.write(params.subclass, Paths.get(gadgetPath, "functions", functionDir, "subclass").toString());
        fs.write(params.reportLength, Paths.get(gadgetPath, "functions", functionDir, "report_length").toString());
        fs.write(params.descriptor, Paths.get(gadgetPath, "functions", functionDir, "report_desc").toString());
    }
}
