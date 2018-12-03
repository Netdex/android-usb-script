package cf.netdex.hidfuzzer.configfs;

import java.util.ArrayList;

import cf.netdex.hidfuzzer.configfs.function.UsbGadgetFunction;
import cf.netdex.hidfuzzer.util.Command;
import cf.netdex.hidfuzzer.util.SUExtensions;
import eu.chainfire.libsuperuser.Shell;

public class UsbGadget {

    public UsbGadgetParameters params;
    public String name;
    public String configFsPath;

    private ArrayList<UsbGadgetFunction> functions;

    public boolean isBound = false;
    private String oldGadgetUsingUDC = null;
    private String oldGadgetUDCDriver = null;

    public UsbGadget(UsbGadgetParameters parameters, String name, String configFsPath) {
        this.params = parameters;
        this.name = name;
        this.configFsPath = configFsPath;

        this.functions = new ArrayList<>();
    }

    public boolean addFunction(UsbGadgetFunction function) {
        if (isBound) return false;
        functions.add(function);
        return true;
    }

    public String getGadgetPath() {
        return String.format("%s/usb_gadget/%s", configFsPath, name);
    }

    public boolean create(Shell.Interactive su) {
        String gadgetPath = getGadgetPath();
        String[] commands = {
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
        };

        su.addCommand(commands);
        su.waitForIdle();

        for (UsbGadgetFunction function : this.functions)
            if (!function.create(su))
                return false;
        return true;
    }

    public boolean bind(Shell.Interactive su) {
        if (isBound)
            return false;

        String[] drivers = SUExtensions.ls(su, "/sys/class/udc");
        if (drivers == null || drivers.length != 1) {
            return false;
        }
        String udc = drivers[0];
        this.oldGadgetUDCDriver = udc;

        // Look for other gadgets using the same usb driver
        ArrayList<String> commands = new ArrayList<>();

        String gadgetPath = getGadgetPath();
        String[] otherUsbGadgets = SUExtensions.ls(su, gadgetPath + "/..");
        if (otherUsbGadgets != null) {
            for (String otherUsbGadgetPath : otherUsbGadgets) {
                String udcPath = gadgetPath + "/../" + otherUsbGadgetPath + "/UDC";
                String driver = SUExtensions.readFile(su, udcPath);
                if (driver.equals(udc)) {
                    // Backup the driver so we can restore it later
                    this.oldGadgetUsingUDC = udcPath;
                    commands.add(Command.echoToFile("", udcPath));
                }
            }
        }

        commands.add(Command.echoToFile(udc, gadgetPath + "/UDC"));

        su.addCommand(commands);
        su.waitForIdle();

        for (UsbGadgetFunction function : this.functions)
            if (!function.bind(su))
                return false;

        this.isBound = true;
        return true;
    }

    public boolean remove(Shell.Interactive su) {
        if (!isBound)
            return false;

        ArrayList<String> commands = new ArrayList<>();
        String gadgetPath = getGadgetPath();
        String udcPath = gadgetPath + "/UDC";

        // Disable gadget
        commands.add(Command.echoToFile("", udcPath));

        // Restore old driver if we need to
        if (oldGadgetUsingUDC != null) {
            commands.add(Command.echoToFile(oldGadgetUDCDriver, oldGadgetUsingUDC));
        }
        commands.add("cd " + gadgetPath);

        su.addCommand(commands);
        su.waitForIdle();

        for (UsbGadgetFunction function : this.functions)
            if (!function.remove(su))
                return false;

        String[] moreCommands = {
                "rmdir configs/c.1/strings/0x409",
                "rmdir configs/c.1",
                "rmdir strings/0x409",
                "cd ..",
                "rmdir " + this.name
        };

        su.addCommand(moreCommands);
        su.waitForIdle();

        this.isBound = false;
        return true;
    }
}