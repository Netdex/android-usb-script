package cf.netdex.hidfuzzer.configfs;

public class UsbGadgetParameters {
    public String manufacturer;
    public String serial;
    public String idProduct;
    public String idVendor;
    public String product;

    public String configName;
    public int maxPowerMa;

    public UsbGadgetParameters(String manufacturer, String serial,
                               String idProduct, String idVendor, String product,
                               String configName, int maxPowerMa) {
        this.manufacturer = manufacturer;
        this.serial = serial;
        this.idProduct = idProduct;
        this.idVendor = idVendor;
        this.product = product;
        this.configName = configName;
        this.maxPowerMa = maxPowerMa;
    }
}