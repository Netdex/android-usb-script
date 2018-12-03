package cf.netdex.hidfuzzer.configfs.function;

import java.util.ArrayList;

import cf.netdex.hidfuzzer.util.Command;
import cf.netdex.hidfuzzer.util.SUExtensions;
import eu.chainfire.libsuperuser.Shell;

/*
 * My phone's kernel seems to have a bug that prevents the Mass Storage gadget from
 * working properly. As such, I cannot verify that this implementation is correct.
 */
public class UsbGadgetFunctionMassStorage extends UsbGadgetFunction {
    public UsbGadgetFunctionMassStorage(int id,
                                        UsbGadgetFunctionMassStorageParameters params) {
        super(id, params);
    }

    @Override
    public boolean create(Shell.Interactive su) {
        UsbGadgetFunctionMassStorageParameters params =
                (UsbGadgetFunctionMassStorageParameters) this.params;

        ArrayList<String> commands = new ArrayList<>();
        if (!SUExtensions.pathExists(su, params.file)) {
            commands.add(String.format("dd bs=1048576 count=%d if=/dev/zero of=\"%s\"",
                    params.size, params.file));
        }
        commands.add(String.format("mkdir functions/mass_storage.usb%d", this.id));
        commands.add(Command.echoToFile(params.file,
                String.format("functions/mass_storage.usb%d/lun.0/file", id)));
        commands.add(String.format("ln -s functions/mass_storage.usb%d configs/c.1", id));
//        commands.add(Command.echoToFile("0", "functions/mass_storage.usb%d/stall"));

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
                String.format("rm configs/c.1/mass_storage.usb%d", this.id),
                String.format("rmdir functions/mass_storage.usb%d", this.id),
        };
        su.addCommand(commands);
        su.waitForIdle();
        return true;
    }
}
