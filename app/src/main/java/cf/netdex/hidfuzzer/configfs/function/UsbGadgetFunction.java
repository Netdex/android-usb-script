package cf.netdex.hidfuzzer.configfs.function;

import eu.chainfire.libsuperuser.Shell;

public class UsbGadgetFunction {

    int id;
    public UsbGadgetFunctionParameters params;

    public UsbGadgetFunction(int id, UsbGadgetFunctionParameters params) {
        this.id = id;
        this.params = params;
    }

    /**
     * Code to be called for the function when the ConfigFS structure is being
     * created for the USB gadget
     * It is guaranteed that the current directory will be the gadget directory
     *
     * @param su Instance of SU shell
     * @return
     */
    public boolean create(Shell.Interactive su) {
        return false;
    }

    public boolean bind(Shell.Interactive su) {
        return false;
    }

    public boolean remove(Shell.Interactive su) {
        return false;
    }
}
