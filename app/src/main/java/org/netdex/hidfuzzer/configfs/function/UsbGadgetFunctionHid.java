package org.netdex.hidfuzzer.configfs.function;

import org.netdex.hidfuzzer.util.Command;

import eu.chainfire.libsuperuser.Shell;

/**
 * https://www.kernel.org/doc/Documentation/usb/gadget_hid.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/configfs-usb-gadget-hid
 */
public class UsbGadgetFunctionHid extends UsbGadgetFunction {
    public static class Parameters extends UsbGadgetFunction.Parameters {
        public int protocol;
        public int subclass;
        public int reportLength;
        public byte[] descriptor;

        public static final Parameters DEFAULT = new Parameters(
                1,
                1,
                8,
                new byte[]{
                        (byte) 0x05, (byte) 0x01,    /* USAGE_PAGE (Generic Desktop)	       */
                        (byte) 0x09, (byte) 0x06,    /* USAGE (Keyboard)                       */
                        (byte) 0xa1, (byte) 0x01,    /* COLLECTION (Application)               */
                        (byte) 0x05, (byte) 0x07,    /*   USAGE_PAGE (Keyboard)                */
                        (byte) 0x19, (byte) 0xe0,    /*   USAGE_MINIMUM (Keyboard LeftControl) */
                        (byte) 0x29, (byte) 0xe7,    /*   USAGE_MAXIMUM (Keyboard Right GUI)   */
                        (byte) 0x15, (byte) 0x00,    /*   LOGICAL_MINIMUM (0)                  */
                        (byte) 0x25, (byte) 0x01,    /*   LOGICAL_MAXIMUM (1)                  */
                        (byte) 0x75, (byte) 0x01,    /*   REPORT_SIZE (1)                      */
                        (byte) 0x95, (byte) 0x08,    /*   REPORT_COUNT (8)                     */
                        (byte) 0x81, (byte) 0x02,    /*   INPUT (Data,Var,Abs)                 */
                        (byte) 0x95, (byte) 0x01,    /*   REPORT_COUNT (1)                     */
                        (byte) 0x75, (byte) 0x08,    /*   REPORT_SIZE (8)                      */
                        (byte) 0x81, (byte) 0x03,    /*   INPUT (Cnst,Var,Abs)                 */
                        (byte) 0x95, (byte) 0x05,    /*   REPORT_COUNT (5)                     */
                        (byte) 0x75, (byte) 0x01,    /*   REPORT_SIZE (1)                      */
                        (byte) 0x05, (byte) 0x08,    /*   USAGE_PAGE (LEDs)                    */
                        (byte) 0x19, (byte) 0x01,    /*   USAGE_MINIMUM (Num Lock)             */
                        (byte) 0x29, (byte) 0x05,    /*   USAGE_MAXIMUM (Kana)                 */
                        (byte) 0x91, (byte) 0x02,    /*   OUTPUT (Data,Var,Abs)                */
                        (byte) 0x95, (byte) 0x01,    /*   REPORT_COUNT (1)                     */
                        (byte) 0x75, (byte) 0x03,    /*   REPORT_SIZE (3)                      */
                        (byte) 0x91, (byte) 0x03,    /*   OUTPUT (Cnst,Var,Abs)                */
                        (byte) 0x95, (byte) 0x06,    /*   REPORT_COUNT (6)                     */
                        (byte) 0x75, (byte) 0x08,    /*   REPORT_SIZE (8)                      */
                        (byte) 0x15, (byte) 0x00,    /*   LOGICAL_MINIMUM (0)                  */
                        (byte) 0x25, (byte) 0x65,    /*   LOGICAL_MAXIMUM (101)                */
                        (byte) 0x05, (byte) 0x07,    /*   USAGE_PAGE (Keyboard)                */
                        (byte) 0x19, (byte) 0x00,    /*   USAGE_MINIMUM (Reserved)             */
                        (byte) 0x29, (byte) 0x65,    /*   USAGE_MAXIMUM (Keyboard Application) */
                        (byte) 0x81, (byte) 0x00,    /*   INPUT (Data,Ary,Abs)                 */
                        (byte) 0xc0                  /* END_COLLECTION                         */
                }
        );

        public Parameters(int protocol, int subclass, int reportLength, byte[] descriptor) {
            this.protocol = protocol;
            this.subclass = subclass;
            this.reportLength = reportLength;
            this.descriptor = descriptor;
        }
    }

    public UsbGadgetFunctionHid(int id, Parameters params) {
        super(id, params);
    }

    @Override
    public String getFunctionDir() {
        return String.format("%s%d", "hid.usb", id_);
    }

    @Override
    public void create(Shell.Threaded su, String gadgetPath) throws Shell.ShellDiedException {
        super.create(su, gadgetPath);

        Parameters params = (Parameters) this.params_;
        String functionDir = getFunctionDir();
        su.run(new String[]{
                Command.echoToFile(String.valueOf(params.protocol),
                        String.format("functions/%s/protocol", functionDir)),
                Command.echoToFile(String.valueOf(params.subclass),
                        String.format("functions/%s/subclass", functionDir)),
                Command.echoToFile(String.valueOf(params.reportLength),
                        String.format("functions/%s/report_length", functionDir)),
                Command.echoToFile(Command.escapeBytes(params.descriptor),
                        String.format("functions/%s/report_desc", functionDir), true, false),
        });
    }
}
