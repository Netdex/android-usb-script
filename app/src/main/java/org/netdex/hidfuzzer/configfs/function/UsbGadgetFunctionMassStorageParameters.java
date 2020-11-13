package org.netdex.hidfuzzer.configfs.function;

public class UsbGadgetFunctionMassStorageParameters extends UsbGadgetFunctionParameters {
    public String file;
    public boolean ro;
    public boolean removable;
    public boolean cdrom;
    public boolean nofua;

    public long size;   // in MB

    public UsbGadgetFunctionMassStorageParameters(String file, boolean ro, boolean removable,
                                                  boolean cdrom, boolean nofua, long size) {
        this.file = file;
        this.ro = ro;
        this.removable = removable;
        this.cdrom = cdrom;
        this.nofua = nofua;
        this.size = size;
    }

    public UsbGadgetFunctionMassStorageParameters(String file) {
        this.file = file;
    }

    public UsbGadgetFunctionMassStorageParameters(String file, long size) {
        this.file = file;
        this.size = size;
    }
}
