package org.netdex.hidfuzzer.configfs.function;

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
     */
    public void create(Shell.Interactive su) throws Shell.ShellDiedException {
    }

    public void bind(Shell.Interactive su) {
    }

    public void remove(Shell.Interactive su) throws Shell.ShellDiedException {
    }
}
