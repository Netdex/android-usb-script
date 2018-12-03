package cf.netdex.hidfuzzer.configfs.function;

import cf.netdex.hidfuzzer.util.Command;
import eu.chainfire.libsuperuser.Shell;

public class UsbGadgetFunctionHid extends UsbGadgetFunction {

    public UsbGadgetFunctionHid(int id, UsbGadgetFunctionHidParameters params) {
        super(id, params);
    }

    @Override
    public boolean create(Shell.Interactive su) {
        UsbGadgetFunctionHidParameters params = (UsbGadgetFunctionHidParameters) this.params;
        String[] commands = {
                String.format("mkdir functions/hid.usb%d", this.id),
                Command.echoToFile(String.valueOf(params.protocol),
                        String.format("functions/hid.usb%d/protocol", this.id)),
                Command.echoToFile(String.valueOf(params.subclass),
                        String.format("functions/hid.usb%d/subclass", this.id)),
                Command.echoToFile(String.valueOf(params.reportLength),
                        String.format("functions/hid.usb%d/report_length", this.id)),
                Command.echoToFile(Command.escapeBytes(params.descriptor),
                        String.format("functions/hid.usb%d/report_desc", id), true, false),
                String.format("ln -s functions/hid.usb%d configs/c.1", id)
        };

        su.addCommand(commands);
        su.waitForIdle();
        return true;
    }

    @Override
    public boolean bind(Shell.Interactive su) {
        return true;
    }

    @Override
    public boolean remove(Shell.Interactive su) {
        String[] commands = {
                String.format("rm configs/c.1/hid.usb%d", this.id),
                String.format("rmdir functions/hid.usb%d", this.id),
        };
        su.addCommand(commands);
        su.waitForIdle();
        return true;
    }
}
