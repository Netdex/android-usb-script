package cf.netdex.hidfuzzer.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.chainfire.libsuperuser.Shell;

public class SUExtensions {
    public static String readFile(Shell.Interactive su, String path) {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] result = new String[1];
        su.addCommand("cat " + path, 0, (commandCode, exitCode, output) -> {
            if (exitCode != 0) {
                result[0] = null;
            } else {
                result[0] = String.join("\n", output);
            }
            latch.countDown();
        });
        try {
            latch.await();
            return result[0];
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean pathExists(Shell.Interactive su, String path) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);
        su.addCommand("ls " + path, 0, (commandCode, exitCode, output) -> {
            result.set(exitCode == 0);
            latch.countDown();
        });
        try {
            latch.await();
            return result.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String[] ls(Shell.Interactive su, String path) {
        CountDownLatch latch = new CountDownLatch(1);

        final String[][] result = {null};
        su.addCommand("ls " + path, 0, (commandCode, exitCode, output) -> {
            if (exitCode != 0) {
                result[0] = null;
            } else {
                result[0] = String.join("\n", output).split("\\s+");
            }
            latch.countDown();
        });
        try {
            latch.await();
            return result[0];
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
