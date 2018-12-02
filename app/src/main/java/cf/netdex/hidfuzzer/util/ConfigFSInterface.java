package cf.netdex.hidfuzzer.util;

import java.util.ArrayList;

import cf.netdex.hidfuzzer.ltask.DialogIO;
import eu.chainfire.libsuperuser.Shell;

public class ConfigFSInterface {

    public static class GadgetConfig {
        public String manufacturer;
        public String serial;
        public String idProduct;
        public String idVendor;
        public String product;
        public String configName;
        public int maxPowerMa;

        public GadgetConfig(String manufacturer, String serial, String idProduct, String idVendor, String product, String configName, int maxPowerMa) {
            this.manufacturer = manufacturer;
            this.serial = serial;
            this.idProduct = idProduct;
            this.idVendor = idVendor;
            this.product = product;
            this.configName = configName;
            this.maxPowerMa = maxPowerMa;
        }
    }

    public static class HidGadgetConfig extends GadgetConfig {
        public int protocol;
        public int subclass;
        public int reportLength;
        public byte[] descriptor;

        public HidGadgetConfig(String manufacturer, String serial, String idProduct, String idVendor, String product, String configName, int maxPowerMa, int protocol, int subclass, int reportLength, byte[] descriptor) {
            super(manufacturer, serial, idProduct, idVendor, product, configName, maxPowerMa);
            this.protocol = protocol;
            this.subclass = subclass;
            this.reportLength = reportLength;
            this.descriptor = descriptor;
        }
    }

    // model for a USB gadget with a maximum of 1 configuration (only HID gadget)
    // TODO support multiple configs for mouse + keyboard
    public static abstract class UsbGadget {


        public GadgetConfig config;
        public String name;
        public String configFsPath;

        public boolean isBound = false;
        private String oldGadgetUsingUDC = null;
        private String oldGadgetUDCDriver = null;

        protected DialogIO userIO = null;

        protected UsbGadget(GadgetConfig config, String name, String configFsPath, DialogIO userIO) {
            this.config = config;
            this.name = name;
            this.configFsPath = configFsPath;
            this.userIO = userIO;
        }

        public String getGadgetPath() {
            return String.format("%s/usb_gadget/%s", configFsPath, name);
        }

        public void createGadget(Shell.Interactive su) {
            String gadgetPath = getGadgetPath();
            String[] commands = {
                    "mkdir " + gadgetPath,
                    "cd " + gadgetPath,
                    Command.echoToFile(config.idProduct, "idProduct"),
                    Command.echoToFile(config.idVendor, "idVendor"),
                    "mkdir strings/0x409",
                    Command.echoToFile(config.serial, "strings/0x409/serialnumber"),
                    Command.echoToFile(config.manufacturer, "strings/0x409/manufacturer"),
                    Command.echoToFile(config.product, "strings/0x409/product"),
                    "mkdir configs/c.1",
                    "mkdir configs/c.1/strings/0x409",
                    Command.echoToFile(config.configName, "configs/c.1/strings/0x409/configuration"),
                    Command.echoToFile(String.valueOf(config.maxPowerMa), "configs/c.1/MaxPower"),
            };
            userIO.log(String.join("<br>", commands));

            su.addCommand(commands);
            su.waitForIdle();
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

            String gadgetPath = getGadgetPath();
            String[] otherUsbGadgets = SUExtensions.ls(su, gadgetPath + "/..");
            if (otherUsbGadgets == null) {
                return false;
            }

            // Look for other gadgets using the same usb driver
            ArrayList<String> commands = new ArrayList<String>();
            for (String otherUsbGadgetPath : otherUsbGadgets) {
                String udcPath = gadgetPath + "/../" + otherUsbGadgetPath + "/UDC";
                String driver = SUExtensions.readFile(su, udcPath);
                if (driver.equals(udc)) {
                    // Backup the driver so we can restore it later
                    this.oldGadgetUsingUDC = udcPath;
                    commands.add(Command.echoToFile("", udcPath));
                }
            }

            commands.add(Command.echoToFile(udc, gadgetPath + "/UDC"));
            userIO.log(String.join("<br>", commands));

            su.addCommand(commands);
            su.waitForIdle();
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
            userIO.log(String.join("<br>", commands));

            su.addCommand(commands);
            su.waitForIdle();
            this.isBound = false;
            return true;
        }
    }

    public static class UsbHidGadget extends UsbGadget {

        public UsbHidGadget(HidGadgetConfig config, String configFsPath, DialogIO userIO) {
            super(config, "keyboardgadget", configFsPath, userIO);
        }

        public void createGadget(Shell.Interactive su) {
            super.createGadget(su);
            HidGadgetConfig config = (HidGadgetConfig) this.config;
            String[] commands = {
                    // TODO don't hardcode names so we can support multiple functions
                    "mkdir functions/hid.usb0",
                    Command.echoToFile(String.valueOf(config.protocol), "functions/hid.usb0/protocol"),
                    Command.echoToFile(String.valueOf(config.subclass), "functions/hid.usb0/subclass"),
                    Command.echoToFile(String.valueOf(config.reportLength), "functions/hid.usb0/report_length"),
                    Command.echoToFile(Command.escapeBytes(config.descriptor), "functions/hid.usb0/report_desc", true, false),
                    "ln -s functions/hid.usb0 configs/c.1"
            };
            userIO.log(String.join("<br>", commands));

            su.addCommand(commands);
            su.waitForIdle();
        }

        public boolean remove(Shell.Interactive su) {
            boolean r1 = super.remove(su);
            if (!r1) return false;
            String[] commands = {
                    "rm configs/c.1/hid.usb0",
                    "rmdir configs/c.1/strings/0x409",
                    "rmdir configs/c.1",
                    "rmdir functions/hid.usb0",
                    "rmdir strings/0x409",
                    "cd ..",
                    "rmdir " + this.name
            };
            userIO.log(String.join("<br>", commands));

            su.addCommand(commands);
            su.waitForIdle();
            return true;
        }
    }
}
