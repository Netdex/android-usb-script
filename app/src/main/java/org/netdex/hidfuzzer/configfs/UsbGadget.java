package org.netdex.hidfuzzer.configfs;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.netdex.hidfuzzer.configfs.function.UsbGadgetFunction;
import org.netdex.hidfuzzer.ltask.AsyncIOBridge;
import org.netdex.hidfuzzer.util.Command;

import eu.chainfire.libsuperuser.Shell;

public class UsbGadget {

    public UsbGadgetParameters params;
    public String name;
    public String configFsPath;

    private ArrayList<UsbGadgetFunction> functions_;

    public boolean isBound = false;
    private String oldGadgetUsingUDC_ = null;
    private String oldGadgetUDCDriver_ = null;

    public UsbGadget(UsbGadgetParameters parameters, String name, String configFsPath) {
        this.params = parameters;
        this.name = name;
        this.configFsPath = configFsPath;

        this.functions_ = new ArrayList<>();
    }

    public void addFunction(UsbGadgetFunction function) {
        if (isBound) {
            // TODO allow adding new functions when already bound
            throw new IllegalStateException("Cannot add new functions when already bound");
        }
        functions_.add(function);
    }

    public String getGadgetPath() {
        return String.format("%s/usb_gadget/%s", configFsPath, name);
    }

    public void create(Shell.Interactive su) throws Shell.ShellDiedException {
        String gadgetPath = getGadgetPath();

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
                "mkdir configs/c.1",
                "mkdir configs/c.1/strings/0x409",
                Command.echoToFile(params.configName, "configs/c.1/strings/0x409/configuration"),
                Command.echoToFile(String.valueOf(params.maxPowerMa), "configs/c.1/MaxPower"),
        });

        for (UsbGadgetFunction function : this.functions_) {
            function.create(su);
        }
    }

    public void bind(Shell.Interactive su) throws Shell.ShellDiedException {
        if (isBound) {
            throw new IllegalStateException("Cannot bind USB gadgets that are already bound");
        }

        ArrayList<String> drivers = Command.ls(su, "/sys/class/udc");
        if (drivers.size() != 1) {
            // TODO allow multiple USB drivers
            throw new IllegalStateException("There must be exactly one USB driver");
        }
        String udc = drivers.get(0);
        this.oldGadgetUDCDriver_ = udc;

        // Look for other gadgets using the same usb driver
        String gadgetPath = getGadgetPath();
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

        for (UsbGadgetFunction function : this.functions_) {
            function.bind(su);
        }

        this.isBound = true;
    }

    public void remove(Shell.Interactive su) throws Shell.ShellDiedException {
        if (!isBound) {
            throw new IllegalStateException("Cannot remove USB gadgets when none are installed");
        }
        String gadgetPath = getGadgetPath();
        String udcPath = Paths.get(gadgetPath, "UDC").toString();

        // Disable gadget
        su.run(Command.echoToFile("", udcPath));
        // Restore old driver if we need to
        if (oldGadgetUsingUDC_ != null) {
            su.run(Command.echoToFile(oldGadgetUDCDriver_, oldGadgetUsingUDC_));
        }
        su.run("cd " + gadgetPath);

        for (UsbGadgetFunction function : this.functions_) {
            function.remove(su);
        }

        su.run(new String[]{
                "rmdir configs/c.1/strings/0x409",
                "rmdir configs/c.1",
                "rmdir strings/0x409",
                "cd ..",
                "rmdir " + this.name
        });

        this.isBound = false;
    }
}