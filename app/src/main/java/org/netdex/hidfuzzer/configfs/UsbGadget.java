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

        public static final Parameters DEFAULT = new Parameters(
                "Samsung",
                "samsung123",
                "0xa4a5",
                "0x0525",
                "Mass Storage Gadget",
                "Configuration 1"
        );

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

    public static final String CONFIG_DIR = "c.1";

    private Parameters params_;
    private String name_;
    private String configFsPath_;

    private ArrayList<UsbGadgetFunction> functions_;

    private String oldGadgetUsingUDC_ = null;
    private String oldGadgetUDCDriver_ = null;

    public UsbGadget(Parameters parameters, String name, String configFsPath) {
        this.params_ = parameters;
        this.name_ = name;
        this.configFsPath_ = configFsPath;

        this.functions_ = new ArrayList<>();
    }

    public void addFunction(UsbGadgetFunction function) {
        functions_.add(function);
    }

    public void create(Shell.Threaded su) throws Shell.ShellDiedException {
        String gadgetPath = getGadgetPath();
        if (Command.pathExists(su, gadgetPath)) {
            throw new IllegalStateException("USB gadget already exists");
        }

        su.run(new String[]{
                "mkdir " + gadgetPath,
                "cd " + gadgetPath,
                Command.echoToFile(params_.idProduct, "idProduct"),
                Command.echoToFile(params_.idVendor, "idVendor"),
                Command.echoToFile("239", "bDeviceClass"),
                Command.echoToFile("0x02", "bDeviceSubClass"),
                Command.echoToFile("0x01", "bDeviceProtocol"),

                "mkdir strings/0x409",
                Command.echoToFile(params_.serial, "strings/0x409/serialnumber"),
                Command.echoToFile(params_.manufacturer, "strings/0x409/manufacturer"),
                Command.echoToFile(params_.product, "strings/0x409/product"),

                String.format("mkdir \"configs/%s\"", CONFIG_DIR),
                String.format("mkdir \"configs/%s/strings/0x409\"", CONFIG_DIR),
                Command.echoToFile(params_.configName, String.format("configs/%s/strings/0x409/configuration", CONFIG_DIR)),
        });

        for (UsbGadgetFunction function : this.functions_) {
            function.create(su, gadgetPath);
        }
    }

    public void bind(Shell.Threaded su) throws Shell.ShellDiedException {
        String gadgetPath = getGadgetPath();
        if (isBound(su)) {
            throw new IllegalStateException("USB gadget is already bound to UDC");
        }

        for (UsbGadgetFunction function : this.functions_) {
            function.bind(su, gadgetPath, CONFIG_DIR);
        }

        ArrayList<String> drivers = Command.ls(su, "/sys/class/udc");
        if (drivers.size() != 1) {
            // TODO allow multiple USB drivers
            throw new IllegalStateException("There must be exactly one USB driver");
        }
        String udc = drivers.get(0);
        this.oldGadgetUDCDriver_ = udc;

        // Look for other gadgets using the same usb driver
        ArrayList<String> otherUsbGadgets = Command.ls(su, Paths.get(gadgetPath, "..").toString());
        for (String otherUsbGadgetPath : otherUsbGadgets) {
            String udcPath = Paths.get(gadgetPath, "..", otherUsbGadgetPath, "UDC").toString();
            String driver = Command.readFile(su, udcPath);
            if (driver.equals(udc)) {
                // Backup the driver so we can restore it later
                this.oldGadgetUsingUDC_ = udcPath;
                su.run(Command.echoToFile("", udcPath));
                break;
            }
        }

        su.run(Command.echoToFile(udc, Paths.get(gadgetPath, "UDC").toString()));
    }

    public void unbind(Shell.Threaded su) throws Shell.ShellDiedException {
        if (!isBound(su)) {
            throw new IllegalStateException("USB gadget is not bound to UDC");
        }

        // Disable gadget
        su.run(Command.echoToFile("", getUDCPath()));
        // Restore old driver if we need to
        if (oldGadgetUsingUDC_ != null) {
            su.run(Command.echoToFile(oldGadgetUDCDriver_, oldGadgetUsingUDC_));
        }

        String gadgetPath = getGadgetPath();
        for (UsbGadgetFunction function : this.functions_) {
            function.unbind(su, gadgetPath, CONFIG_DIR);
        }
    }

    public void remove(Shell.Threaded su) throws Shell.ShellDiedException {
        String gadgetPath = getGadgetPath();
        if (!Command.pathExists(su, gadgetPath)) {
            throw new IllegalStateException("USB gadget does not exist");
        }

        for (UsbGadgetFunction function : this.functions_) {
            function.remove(su, gadgetPath);
        }

        su.run(new String[]{
                "rmdir configs/c.1/strings/0x409",
                "rmdir configs/c.1",
                "rmdir strings/0x409",
                "cd ..",
                "rmdir " + this.name_
        });
    }

    public String getGadgetPath() {
        return String.format("%s/usb_gadget/%s", configFsPath_, name_);
    }

    public String getUDCPath() {
        return Paths.get(getGadgetPath(), "UDC").toString();
    }

    public String getUDC(Shell.Threaded su) {
        return Command.readFile(su, getUDCPath());
    }

    public boolean isBound(Shell.Threaded su) {
        return !getUDC(su).isEmpty();
    }

}