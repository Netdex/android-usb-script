package org.netdex.hidfuzzer.hid;

import java.io.IOException;
import java.util.Arrays;

import org.netdex.hidfuzzer.util.Command;

import eu.chainfire.libsuperuser.Shell;

/**
 * Native communication with HID devices
 * <p>
 * Created by netdex on 1/15/2017.
 */

public class HID {
    private static final byte[] mouse_buf = new byte[4];
    private static final byte[] keyboard_buf = new byte[8];

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
    public static void sendHIDMouse(Shell.Threaded sh, String dev, byte... offset) {
        throw new UnsupportedOperationException("mouse descriptor not implemented"); // TODO
        /*
        if (offset.length > 4)
            throw new IllegalArgumentException("Your mouse can only move in two dimensions");
        Arrays.fill(mouse_buf, (byte) 0);
        System.arraycopy(offset, 0, mouse_buf, 0, offset.length);
        return write_bytes(sh, dev, mouse_buf);*/
    }

    /**
     * A        B        C        D        E        F        G        H
     * XXXXXXXX 00000000 XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX
     * <p>
     * A: K modifier mask
     * B: Reserved
     * C: K 1; D: K 2; E: K 3; F: K 4; G: K 5; H: K 6;
     *
     * @param sh   SUExtensions shell
     * @param dev  KB device (/dev/hidg0)
     * @param keys HID keyboard bytes
     */
    public static void sendHIDKeyboard(Shell.Threaded sh, String dev, byte... keys) throws Shell.ShellDiedException, IOException {
        if (keys.length > 7)
            throw new IllegalArgumentException("Cannot send more than 6 keys");
        Arrays.fill(keyboard_buf, (byte) 0);
        if (keys.length > 0) keyboard_buf[0] = keys[0];
        if (keys.length > 1) System.arraycopy(keys, 1, keyboard_buf, 2, keys.length - 1);
        write_bytes(sh, dev, keyboard_buf);
    }

    /**
     * Writes bytes to a file with "echo -n -e [binary string] > file"
     *
     * @param sh  Threaded shell to send echo command
     * @param dev File to write to
     * @param arr Bytes to write
     */
    private static void write_bytes(Shell.Threaded sh, String dev, byte[] arr) throws Shell.ShellDiedException, IOException {
        int exitCode = sh.run(Command.echoToFile(Command.escapeBytes(arr), dev, true, false));
        if (exitCode != 0)
            throw new IOException(String.format("Could not write to device \"%s\"", dev));
    }
}
