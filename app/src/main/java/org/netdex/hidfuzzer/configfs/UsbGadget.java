package org.netdex.hidfuzzer.configfs;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.netdex.hidfuzzer.configfs.function.UsbGadgetFunction;
import org.netdex.hidfuzzer.util.Command;

import eu.chainfire.libsuperuser.Shell;

public class UsbGadget {
    public static class Parameters {
        public String manufacturer;
        public String serial;
        public String idProduct;
        public String idVendor;
        public String product;

        public String configName;

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

    public void create(Shell.Threaded su, Parameters params) throws Shell.ShellDiedException {
        String gadgetPath = getGadgetPath(gadget_name_);
        if (!Command.getSystemProp(su, "sys.usb.configfs").equals("1"))
            throw new IllegalStateException("Device does not support ConfigFS");
        if (isCreated(su))
            throw new IllegalStateException("USB gadget already exists");

        su.run(new String[]{
                "mkdir " + gadgetPath,
                "cd " + gadgetPath,
                Command.echoToFile(params.idProduct, "idProduct"),
                Command.echoToFile(params.idVendor, "idVendor"),
                Command.echoToFile("239", "bDeviceClass"),
                Command.echoToFile("0x02", "bDeviceSubClass"),
                Command.echoToFile("0x01", "bDeviceProtocol"),

                "mkdir strings/0x409",
                Command.echoToFile(params.serial, "strings/0x409/serialnumber"),
                Command.echoToFile(params.manufacturer, "strings/0x409/manufacturer"),
                Command.echoToFile(params.product, "strings/0x409/product"),

                String.format("mkdir \"configs/%s\"", CONFIG_DIR),
                String.format("mkdir \"configs/%s/strings/0x409\"", CONFIG_DIR),
                Command.echoToFile(params.configName, String.format("configs/%s/strings/0x409/configuration", CONFIG_DIR)),
        });

        for (UsbGadgetFunction function : this.functions_) {
            function.create(su, gadgetPath);
        }
    }

    public void bind(Shell.Threaded su) throws Shell.ShellDiedException {
        String gadgetPath = getGadgetPath(gadget_name_);
        if (!isCreated(su))
            throw new IllegalStateException("USB gadget does not exist");
        if (isBound(su))
            throw new IllegalStateException("USB gadget is already bound to UDC");

        for (UsbGadgetFunction function : this.functions_) {
            function.bind(su, gadgetPath, CONFIG_DIR);
        }

        String udc = getSystemUDC(su);
        if (udc.isEmpty()) throw new IllegalStateException("Could not determine system UDC");

        su.run(Command.echoToFile("", getUDCPath(SYSTEM_GADGET)));
        su.run(Command.echoToFile(udc, getUDCPath(gadget_name_)));
    }

    public void unbind(Shell.Threaded su) throws Shell.ShellDiedException {
        if (!isCreated(su))
            throw new IllegalStateException("USB gadget does not exist");
        if (!isBound(su))
            throw new IllegalStateException("USB gadget is not bound to UDC");

        su.run(Command.echoToFile("", getUDCPath(gadget_name_)));
        su.run(Command.echoToFile(getSystemUDC(su), SYSTEM_GADGET));

        String gadgetPath = getGadgetPath(gadget_name_);
        for (UsbGadgetFunction function : this.functions_) {
            function.unbind(su, gadgetPath, CONFIG_DIR);
        }
    }

    public void remove(Shell.Threaded su) throws Shell.ShellDiedException {
        String gadgetPath = getGadgetPath(gadget_name_);
        if (!isCreated(su))
            throw new IllegalStateException("USB gadget does not exist");

        for (UsbGadgetFunction function : this.functions_) {
            function.remove(su, gadgetPath);
        }

        su.run(new String[]{
                String.format("rmdir \"configs/%s/strings/0x409\"", CONFIG_DIR),
                String.format("rmdir \"configs/%s\"", CONFIG_DIR),
                "rmdir strings/0x409",
                "cd ..",
                String.format("rmdir \"%s\"", this.gadget_name_)
        });
    }

    public String getGadgetPath(String gadgetName) {
        return String.format("%s/usb_gadget/%s", configFsPath_, gadgetName);
    }

    public String getUDCPath(String gadgetName) {
        return Paths.get(getGadgetPath(gadgetName), "UDC").toString();
    }

    public String getActiveUDC(Shell.Threaded su, String gadgetName) throws Shell.ShellDiedException {
        return Command.readFile(su, getUDCPath(gadgetName));
    }

    public boolean isCreated(Shell.Threaded su) throws Shell.ShellDiedException {
        return Command.pathExists(su, getGadgetPath(gadget_name_));
    }

    public boolean isBound(Shell.Threaded su) throws Shell.ShellDiedException {
        return !getActiveUDC(su, gadget_name_).isEmpty();
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
            functionDir.add(function.getFunctionDir());
        }
        return String.format("%x", functionDir.hashCode());
    }

    public static String getSystemUDC(Shell.Threaded su) throws Shell.ShellDiedException {
        return Command.getSystemProp(su, "sys.usb.controller");
    }

    public static String getUDCState(Shell.Threaded su, String udc) throws Shell.ShellDiedException {
        return Command.readFile(su, String.format("/sys/class/udc/%s/state", udc));
    }

}