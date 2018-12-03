package cf.netdex.hidfuzzer.ltask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

import java.lang.ref.WeakReference;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import cf.netdex.hidfuzzer.configfs.UsbGadget;
import cf.netdex.hidfuzzer.configfs.UsbGadgetParameters;
import cf.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionHid;
import cf.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionHidParameters;
import cf.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionMassStorage;
import cf.netdex.hidfuzzer.configfs.function.UsbGadgetFunctionMassStorageParameters;
import cf.netdex.hidfuzzer.hid.HIDR;
import cf.netdex.hidfuzzer.lua.LuaHIDBinding;
import cf.netdex.hidfuzzer.util.SUExtensions;
import eu.chainfire.libsuperuser.Shell;

import static cf.netdex.hidfuzzer.MainActivity.TAG;

/**
 * Created by netdex on 1/16/2017.
 */

public class HIDTask extends AsyncTask<Void, HIDTask.RunState, Void> {

    static final String DEV_KEYBOARD = "/dev/hidg0";
    static final String DEV_MOUSE = "/dev/hidg1";

    private WeakReference<Context> mContext;

    // TODO: remove hacky behavior for updating log view
    private DialogIO mUserIO;

    private Shell.Interactive mSU;
    private HIDR mH;
    private LuaHIDBinding mLuaHIDBinding;

    private String mName;
    private String mSrc;
    private LuaValue mLuaChunk;


    public HIDTask(Context context, String name, String src) {
        this.mContext = new WeakReference<>(context);
        this.mName = name;
        this.mSrc = src;

        mUserIO = new DialogIO(context);
    }

    @Override
    protected void onPreExecute() {
        try {
            mLuaHIDBinding = new LuaHIDBinding(this);
            Globals mLuaGlobals = LuaTaskLoader.createGlobals(this);
            mLuaChunk = LuaTaskLoader.loadChunk(mLuaGlobals, mSrc);
        } catch (LuaError e) {
            mUserIO.log("<b>LuaError:</b> " + e.getMessage());
            this.cancel(true);
        }
    }

    private UsbGadgetParameters usbGadgetParams = new UsbGadgetParameters(
            "Samsung",
            "samsung123",
            "0xa4a5",
            "0x0525",
            "Mass Storage Gadget",
            "Configuration 1",
            120
    );

    private UsbGadgetFunctionHidParameters usbGadgetFcnHidParams = new UsbGadgetFunctionHidParameters(
            1,
            1,
            8,
            new byte[]{
                    (byte) 0x05, (byte) 0x01,    /* USAGE_PAGE (Generic Desktop)	          */
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

    private UsbGadgetFunctionMassStorageParameters usbGadgetFcnStorParams = null;

    private UsbGadget usbGadget = null;

    @Override
    protected Void doInBackground(Void... params) {
        mUserIO.clear();

        // I don't know why but apparently you can't initialize SU shell on UI thread
        mSU = createSU();
        if (mSU == null) {
            mUserIO.log("<b>! Failed to obtain SU !</b>");
            return null;
        }
//        if (SUExtensions.pathExists(mSU, DEV_KEYBOARD)) {
//            // assume kernel patch exists
//            // TODO we can't actually do this because configfs makes it
//        } else {
            if (SUExtensions.pathExists(mSU, "/config")) {
                Log.i(TAG, "No kernel patch detected, using configfs");
                usbGadget = new UsbGadget(usbGadgetParams, "keyboardgadget", "/config");
                UsbGadgetFunctionHid fcnHid = new UsbGadgetFunctionHid(0, usbGadgetFcnHidParams);
                usbGadget.addFunction(fcnHid);
//                usbGadgetFcnStorParams =
//                        new UsbGadgetFunctionMassStorageParameters(
//                                /*getContext().getFilesDir().getAbsolutePath() + */"/data/local/tmp/mass_storage-lun0.img",
//                                256);
//                Log.i(TAG, usbGadgetFcnStorParams.file);
//                UsbGadgetFunctionMassStorage fcnStor =
//                        new UsbGadgetFunctionMassStorage(1, usbGadgetFcnStorParams);
//                usbGadget.addFunction(fcnStor);
                usbGadget.create(mSU);
                if (!usbGadget.bind(mSU)) {
                    mUserIO.log("<b>Could not bind usb gadget</b>");
                    return null;
                }
            } else {
                Log.e(TAG, "No method exists for accessing hid gadget");
                return null;
            }
//        }

        mSU.addCommand("chmod 666 " + DEV_KEYBOARD);
        //mSU.addCommand("chmod 666 " + DEV_MOUSE);

        mH = new HIDR(mSU, DEV_KEYBOARD, "" /*DEV_MOUSE*/);
        mUserIO.log("<b>-- Started <i>" + mName + "</i></b>");
        run();
        mUserIO.log("<b>-- Ended <i>" + mName + "</i></b>");
        if (usbGadget != null) {
            usbGadget.remove(mSU);
            usbGadget = null;
        }
        return null;
    }

    public void progress(RunState s) {
        mUserIO.log(mName + ": <b>" + s.name() + "</b>");
    }

    private void run() {
        try {
            mLuaChunk.call();
        } catch (LuaError e) {
            e.printStackTrace();
            mUserIO.log("<b>LuaError:</b> " + e.getMessage());
        }
    }

    @Override
    protected void onCancelled() {
        cleanup();
    }

    @Override
    protected void onPostExecute(Void result) {
        cleanup();
    }

    private void cleanup() {
        if (mH != null)
            mH.getKeyboardLightListener().kill();
        if (mSU != null) {
            mSU.kill();
            mSU.close();
        }
        // For some reason, you must setChecked before setEnabled or the setEnabled will have no effect
        mUserIO.getModeButton().get().setChecked(false);
        mUserIO.getModeButton().get().setEnabled(true);
    }

    private static Shell.Interactive createSU() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final boolean[] root = new boolean[1];
            Shell.Interactive sh = new Shell.Builder()
                    .useSU()
                    .setWantSTDERR(true)
                    .setWatchdogTimeout(5)
                    .setMinimalLogging(true)
                    .open((commandCode, exitCode, output) -> {
                        if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING) {
                            Log.e(TAG, "Failed to open SUExtensions");
                            root[0] = false;
                        } else {
                            root[0] = true;
                        }
                        latch.countDown();
                    });
            latch.await();
            if (!root[0]) return null;
            return sh;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getContext() {
        return mContext.get();
    }

    Shell.Interactive getSU() {
        return mSU;
    }

    public HIDR getHIDR() {
        return mH;
    }

    public DialogIO getIO() {
        return mUserIO;
    }

    public LuaHIDBinding getLuaHIDBinding() {
        return mLuaHIDBinding;
    }

    public enum RunState {
        RUNNING,
        IDLE,
        STOPPED,
        DONE
    }
}