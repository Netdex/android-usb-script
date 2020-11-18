package org.netdex.hidfuzzer.function;

import org.netdex.hidfuzzer.util.Command;

import java.io.IOException;
import java.util.Arrays;

import eu.chainfire.libsuperuser.Shell;

public class HidMouseInterface {
    private final Shell.Threaded su_;
    private final String devicePath_;

    public HidMouseInterface(Shell.Threaded su, String devicePath) {
        this.su_ = su;
        this.devicePath_ = devicePath;
    }

    public void click(byte mask, long duration) throws Shell.ShellDiedException, IOException, InterruptedException {
        sendMouse(mask);
        if (duration > 0) {
            Thread.sleep(duration);
        }
        sendMouse();
    }

    public void move(byte dx, byte dy) throws Shell.ShellDiedException, IOException {
        sendMouse((byte) 0, dx, dy);
    }

    public void scroll(byte offset) throws Shell.ShellDiedException, IOException {
        sendMouse((byte) 0, (byte) 0, (byte) 0, offset);
    }

    /**
     * Sends mouse command
     *
     * @param offset command byte[] to send, defined in HID.java
     */
    public void sendMouse(byte... offset) throws Shell.ShellDiedException, IOException {
        sendMouseHID(su_, devicePath_, offset);
    }

    /**
     * A        B        C        D
     * XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX
     * <p>
     * A: Mouse button mask
     * B: Mouse X-offset
     * C: Mouse Y-offset
     * D: Mouse wheel offset
     *
     * @param sh     SUExtensions shell
     * @param dev    Mouse device (/dev/hidg1)
     * @param offset HID mouse bytes
     */
    public static void sendMouseHID(Shell.Threaded sh, String dev, byte... offset) throws Shell.ShellDiedException, IOException {
        byte[] buffer = new byte[4];
        if (offset.length > 4)
            throw new IllegalArgumentException("Your mouse can only move in two dimensions");
        Arrays.fill(buffer, (byte) 0);
        System.arraycopy(offset, 0, buffer, 0, offset.length);
        Command.write(sh, dev, buffer);
    }
}
