package org.netdex.hidfuzzer.util;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.chainfire.libsuperuser.Shell;

public class Command {

    public static <T> String echoToFile(T val, String file, boolean escape, boolean newLine) {
        if (val == null)
            return "";
        return String.format("echo %s %s \"%s\" > \"%s\"", newLine ? "" : "-n", escape ? "-e" : "",
                val, file);
    }

    public static <T> String echoToFile(T val, String file) {
        return echoToFile(val, file, false, true);
    }

    public static String timeout(String command, double timeout) {
        return String.format("timeout %f %s", timeout, command);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Escapes a byte array into a string
     * ex. [0x00, 0x04, 0x04] => "\x00\x04\x04"
     *
     * @param arr Byte array to escape
     * @return Escaped byte array as string
     */
    public static String escapeBytes(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            int v = b & 0xFF;
            sb.append("\\x").append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]);
        }
        return sb.toString();
    }

    public static String readFile(Shell.Interactive su, String path) throws Shell.ShellDiedException {
        return readStdout(su, String.format("cat \"%s\"", path));
    }

    public static String readStdout(Shell.Interactive su, String command) throws Shell.ShellDiedException {
        StringBuilder sb = new StringBuilder();
        AtomicBoolean first = new AtomicBoolean(true);
        su.run(command, new Shell.OnSyncCommandLineListener() {
            @Override
            public void onSTDERR(@NonNull String line) {

            }

            @Override
            public void onSTDOUT(@NonNull String line) {
                if (!first.getAndSet(false)) {
                    sb.append('\n');
                }
                sb.append(line);
            }
        });
        return sb.toString();
    }

    public static boolean pathExists(Shell.Interactive su, String path) throws Shell.ShellDiedException {
        int exitCode = su.run(String.format("stat \"%s\"", path));
        return exitCode == 0;
    }

    public static ArrayList<String> ls(Shell.Interactive su, String path) throws Shell.ShellDiedException {
        ArrayList<String> files = new ArrayList<>();
        // ls, each file on separate line
        su.run(String.format("ls -1A \"%s\"", path), new Shell.OnSyncCommandLineListener() {
            @Override
            public void onSTDERR(@NonNull String line) {

            }

            @Override
            public void onSTDOUT(@NonNull String line) {
                if (!line.isEmpty()) {
                    files.add(line);
                }
            }
        });
        return files;
    }

    public static String getSystemProp(Shell.Interactive su, String prop) throws Shell.ShellDiedException {
        return readStdout(su, String.format("getprop %s", prop));
    }

    /**
     * Writes bytes to a file with "echo -n -e [binary string] > file"
     *
     * @param sh   Threaded shell to send echo command
     * @param file File to write to
     * @param arr  Bytes to write
     */
    public static void write(Shell.Threaded sh, String file, byte[] arr) throws Shell.ShellDiedException, IOException {
        // NOTE: It is possible for this write to block in some circumstances when unplugged
        int exitCode = sh.run(Command.echoToFile(
                Command.escapeBytes(arr), file, true, false));
        if (exitCode != 0)
            throw new IOException(String.format("Could not write to \"%s\"", file));
    }
}
