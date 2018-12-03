package cf.netdex.hidfuzzer.configfs.function;

public class UsbGadgetFunctionHidParameters extends UsbGadgetFunctionParameters {
    public int protocol;
    public int subclass;
    public int reportLength;
    public byte[] descriptor;

    public UsbGadgetFunctionHidParameters(int protocol, int subclass, int reportLength, byte[] descriptor) {
        this.protocol = protocol;
        this.subclass = subclass;
        this.reportLength = reportLength;
        this.descriptor = descriptor;
    }
}
