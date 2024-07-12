package org.netdex.androidusbscript.configfs;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.netdex.androidusbscript.configfs.function.UsbGadgetFunction;
import org.netdex.androidusbscript.util.FileSystem;


import timber.log.Timber;

public class UsbGadget {
    public static class Parameters {
        public final String manufacturer;
        public final String serial;
        public final String idProduct;
        public final String idVendor;
        public final String product;

        public final String configName;

        public Parameters(String manufacturer, String serial,
                          String idProduct, String idVendor, String product,
                          String configName) {
            this.manufacturer = manufacturer;
            this.serial = serial;
            this.idProduct = idProduct;
            this.idVendor = idVendor;
            this.product = product;
            this.configName = configName;
        }
    }

    private static final String CONFIG_DIR = "c.1";
    // https://android.googlesource.com/platform/system/core/+/master/rootdir/init.usb.configfs.rc
    private static final String SYSTEM_GADGET = "g1";

    private final String gadget_name_;
    private final String configFsPath_;

    private final ArrayList<UsbGadgetFunction> functions_;

    public UsbGadget(String name, String configFsPath) {
        this.gadget_name_ = name;
        this.configFsPath_ = configFsPath;

        this.functions_ = new ArrayList<>();
    }

    public void add(FileSystem fs, Parameters params) throws IOException {
        Timber.d("Creating USB gadget '%s'", this.gadget_name_);
        String gadgetPath = getGadgetPath(gadget_name_);
        if (!isSupported(fs))
            throw new UnsupportedOperationException("Device does not support ConfigFS");
        if (isCreated(fs))
            throw new IllegalStateException("USB gadget already exists");

        fs.mkdir(gadgetPath);
        fs.write(params.idProduct, Paths.get(gadgetPath, "idProduct").toString());
        fs.write(params.idVendor, Paths.get(gadgetPath, "idVendor").toString());
        fs.write("239", Paths.get(gadgetPath, "bDeviceClass").toString());
        fs.write("0x02", Paths.get(gadgetPath, "bDeviceSubClass").toString());
        fs.write("0x01", Paths.get(gadgetPath, "bDeviceProtocol").toString());

        fs.mkdir(Paths.get(gadgetPath, "strings/0x409").toString());
        fs.write(params.serial, Paths.get(gadgetPath, "strings/0x409/serialnumber").toString());
        fs.write(params.manufacturer, Paths.get(gadgetPath, "strings/0x409/manufacturer").toString());
        fs.write(params.product, Paths.get(gadgetPath, "strings/0x409/product").toString());

        fs.mkdir(Paths.get(gadgetPath, "configs", CONFIG_DIR).toString());
        fs.mkdir(Paths.get(gadgetPath, "configs", CONFIG_DIR, "strings/0x409").toString());
        fs.write(params.configName, Paths.get(gadgetPath, "configs", CONFIG_DIR, "strings/0x409/configuration").toString());

        for (UsbGadgetFunction function : this.functions_) {
            function.add(fs, gadgetPath, CONFIG_DIR);
        }
    }

    public void bind(FileSystem fs) throws IOException {
        Timber.d("Binding USB gadget '%s'", this.gadget_name_);
        if (!isCreated(fs))
            throw new IllegalStateException("USB gadget does not exist");
        if (isBound(fs))
            throw new IllegalStateException("USB gadget is already bound to UDC");

        String udc = getSystemUDC(fs);
        if (udc.isEmpty()) throw new IllegalStateException("Could not determine system UDC");

        fs.write("", getUDCPath(SYSTEM_GADGET));
        fs.write(udc, getUDCPath(gadget_name_));
    }

    public void unbind(FileSystem fs) throws IOException {
        Timber.d("Unbinding USB gadget '%s'", this.gadget_name_);
        if (!isCreated(fs))
            throw new IllegalStateException("USB gadget does not exist");
        if (!isBound(fs))
            throw new IllegalStateException("USB gadget is not bound to UDC");

        fs.write("", getUDCPath(gadget_name_));
        fs.write(getSystemUDC(fs), getUDCPath(SYSTEM_GADGET));
    }

    public void remove(FileSystem fs) throws IOException {
        Timber.d("Removing USB gadget '%s'", this.gadget_name_);
        String gadgetPath = getGadgetPath(gadget_name_);
        if (!isCreated(fs))
            throw new IllegalStateException("USB gadget does not exist");

        for (UsbGadgetFunction function : this.functions_) {
            function.remove(fs, gadgetPath, CONFIG_DIR);
        }

        fs.delete(Paths.get(gadgetPath, "configs", CONFIG_DIR, "strings/0x409").toString());
        fs.delete(Paths.get(gadgetPath, "configs", CONFIG_DIR).toString());
        fs.delete(Paths.get(gadgetPath, "strings/0x409").toString());
        fs.delete(gadgetPath);
    }

    public boolean isSupported(FileSystem fs) {
        return fs.exists(configFsPath_)
                && Integer.parseInt(fs.getSystemProp("sys.usb.configfs")) >= 1;
    }

    public String getAttribute(FileSystem fs, String functionName, String attrib) throws IOException {
        String gadgetPath = getGadgetPath(gadget_name_);
        String functionPath = Paths.get(gadgetPath, "configs", CONFIG_DIR, functionName).toString();
        if (!fs.exists(functionPath)) {
            throw new IllegalStateException(String.format("Function symlink '%s' does not exist", functionPath));
        }
        return fs.readline(Paths.get(functionPath, attrib).toString());
    }

    public String getGadgetPath(String gadgetName) {
        return Paths.get(configFsPath_, "usb_gadget", gadgetName).toString();
    }

    public String getUDCPath(String gadgetName) {
        return Paths.get(getGadgetPath(gadgetName), "UDC").toString();
    }

    public String getActiveUDC(FileSystem fs, String gadgetName) throws IOException {
        return fs.readline(getUDCPath(gadgetName));
    }

    public boolean isCreated(FileSystem fs) {
        return fs.exists(getGadgetPath(gadget_name_));
    }

    public boolean isBound(FileSystem fs) throws IOException {
        return !getActiveUDC(fs, gadget_name_).isEmpty();
    }

    public void addFunction(UsbGadgetFunction function) {
        functions_.add(function);
    }

    public ArrayList<UsbGadgetFunction> getFunctions() {
        return functions_;
    }

    public String serial() {
        ArrayList<String> functionDir = new ArrayList<>();
        for (UsbGadgetFunction function : getFunctions()) {
            functionDir.add(function.getName());
        }
        return String.format("%x", functionDir.hashCode());
    }

    public static String getSystemUDC(FileSystem fs) {
        return fs.getSystemProp("sys.usb.controller");
    }

    public static String getUDCState(FileSystem fs, String udc) throws IOException {
        return fs.readline(Paths.get("/sys/class/udc", udc, "state").toString());
    }
}