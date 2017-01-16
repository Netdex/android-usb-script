package cf.netdex.hidfuzzer;

import java.util.concurrent.CountDownLatch;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by netdex on 1/15/2017.
 */

class HID {
    static int hid_mouse(Shell.Interactive sh, String dev, byte... offset) {
        if (offset.length > 4)
            throw new IllegalArgumentException("Your mouse can only move in two dimensions");
        // for some odd reason, 4-byte padding is required
        byte[] buf = new byte[8];
        System.arraycopy(offset, 0, buf, 0, offset.length);
        return write_bytes(sh, dev, buf);
    }


    static int hid_keyboard(Shell.Interactive sh, String dev, byte... keys) {
        if (keys.length > 8)
            throw new IllegalArgumentException("Cannot send more than 7 keys");
        // for some odd reason, every odd byte has to be blank
        byte[] buf = new byte[16];
        for (int i = 0; i < keys.length; i++) {
            buf[2 * i] = keys[i];
        }
        return write_bytes(sh, dev, buf);
    }

    // TODO read state of NUM_LOCK, CAPS_LOCK, and SCROLL_LOCK by reading /dev/hidg0
    // lol you can create a serial line by flashing the num and caps lights, probably 10 baud though

    private static int write_bytes(Shell.Interactive sh, String dev, byte[] arr) {
        String bt = escapeBytes(arr);
        final Integer[] err = {-1};
        try {
            // run echo command to write to device as root
            final CountDownLatch latch = new CountDownLatch(1);
            String c = String.format("echo -n -e \"%s\" > %s", bt, dev);
            sh.addCommand(c, 0, new Shell.OnCommandLineListener() {
                @Override
                public void onLine(String line) {
                }

                @Override
                public void onCommandResult(int commandCode, int exitCode) {
                    err[0] = exitCode;
                    latch.countDown();
                }
            });
            latch.await();
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return err[0];
    }

    /**
     * Escapes a byte array into a string
     * ex. [0x0, 0x4, 0x4] => "\x00\x04\x04"
     *
     * @param arr Byte array to escape
     * @return Escaped byte array as string
     */
    private static String escapeBytes(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(String.format("\\x%02x", b));
        }
        return sb.toString();
    }
}
