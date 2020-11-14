//                usbGadgetFcnStorParams =
//                        new UsbGadgetFunctionMassStorageParameters(
//                                /*getContext().getFilesDir().getAbsolutePath() + */"/data/local/tmp/mass_storage-lun0.img",
//                                256);
//                Log.i(TAG, usbGadgetFcnStorParams.file);
//                UsbGadgetFunctionMassStorage fcnStor =
//                        new UsbGadgetFunctionMassStorage(1, usbGadgetFcnStorParams);
//                usbGadget.addFunction(fcnStor);
//
//package org.netdex.hidfuzzer.configfs.function;
//
//import java.util.ArrayList;
//
//import org.netdex.hidfuzzer.util.Command;
//
//import eu.chainfire.libsuperuser.Shell;
//
///*
// * My phone's kernel seems to have a bug that prevents the Mass Storage gadget from
// * working properly. As such, I cannot verify that this implementation is correct.
// */
//public class UsbGadgetFunctionMassStorage extends UsbGadgetFunction {
//    public static class Parameters extends UsbGadgetFunction.Parameters {
//        public String file;
//        public boolean ro;
//        public boolean removable;
//        public boolean cdrom;
//        public boolean nofua;
//
//        public long size;   // in MB
//
//        public Parameters(String file, boolean ro, boolean removable,
//                          boolean cdrom, boolean nofua, long size) {
//            this.file = file;
//            this.ro = ro;
//            this.removable = removable;
//            this.cdrom = cdrom;
//            this.nofua = nofua;
//            this.size = size;
//        }
//
//        public Parameters(String file) {
//            this.file = file;
//        }
//
//        public Parameters(String file, long size) {
//            this.file = file;
//            this.size = size;
//        }
//    }
//
//    public UsbGadgetFunctionMassStorage(int id, Parameters params) {
//        super(id, params);
//    }
//
//    @Override
//    public void create(Shell.Interactive su) throws Shell.ShellDiedException {
//        Parameters params = (Parameters) this.params_;
//
//        ArrayList<String> commands = new ArrayList<>();
//        if (!Command.pathExists(su, params.file)) {
//            commands.add(String.format("dd bs=1048576 count=%d if=/dev/zero of=\"%s\"",
//                    params.size, params.file));
//        }
//        commands.add(String.format("mkdir functions/mass_storage.usb%d", this.id_));
//        commands.add(Command.echoToFile(params.file,
//                String.format("functions/mass_storage.usb%d/lun.0/file", id_)));
//        commands.add(String.format("ln -s functions/mass_storage.usb%d configs/c.1", id_));
////        commands.add(Command.echoToFile("0", "functions/mass_storage.usb%d/stall"));
//
//        su.addCommand(commands);
//        su.waitForIdle();
//    }
//
//    @Override
//    public void remove(Shell.Interactive su) throws Shell.ShellDiedException {
//        su.run(new String[]{
//                String.format("rm configs/c.1/mass_storage.usb%d", this.id_),
//                String.format("rmdir functions/mass_storage.usb%d", this.id_),
//        });
//    }
//}