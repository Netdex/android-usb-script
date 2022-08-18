package org.netdex.androidusbscript.configfs.function;

import org.netdex.androidusbscript.util.Command;

import eu.chainfire.libsuperuser.Shell;

/**
 * https://www.kernel.org/doc/Documentation/usb/gadget_hid.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/configfs-usb-gadget-hid
 */
public class UsbGadgetFunctionHid extends UsbGadgetFunction {
    public static class Parameters extends UsbGadgetFunction.Parameters {
        public int protocol;
        public int subclass;
        public int reportLength;
        public byte[] descriptor;

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
    public String getFunctionDir() {
        return String.format("%s%d", "hid.usb", id_);
    }

    @Override
    public void create(Shell.Threaded su, String gadgetPath) throws Shell.ShellDiedException {
        super.create(su, gadgetPath);

        Parameters params = (Parameters) this.params_;
        String functionDir = getFunctionDir();
        su.run(new String[]{
                Command.echoToFile(String.valueOf(params.protocol),
                        String.format("functions/%s/protocol", functionDir)),
                Command.echoToFile(String.valueOf(params.subclass),
                        String.format("functions/%s/subclass", functionDir)),
                Command.echoToFile(String.valueOf(params.reportLength),
                        String.format("functions/%s/report_length", functionDir)),
                Command.echoToFile(Command.escapeBytes(params.descriptor),
                        String.format("functions/%s/report_desc", functionDir), true, false),
        });
    }
}
