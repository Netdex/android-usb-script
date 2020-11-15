package org.netdex.hidfuzzer.util;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.chainfire.libsuperuser.Shell;

public class Command {
    public static String echoToFile(String s, String file, boolean escape, boolean newLine) {
        if (s == null)
            return "";
        return String.format("echo %s %s \"%s\" > \"%s\"", newLine ? "" : "-n", escape ? "-e" : "", s, file);
    }

    public static String echoToFile(String s, String file) {
        return echoToFile(s, file, false, true);
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

    public static String readFile(Shell.Interactive su, String path) {
        StringBuilder sb = new StringBuilder();
        AtomicBoolean first = new AtomicBoolean(true);
        try {
            su.run(String.format("cat \"%s\"", path), new Shell.OnSyncCommandLineListener() {
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
        } catch (Shell.ShellDiedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static boolean pathExists(Shell.Interactive su, String path) throws Shell.ShellDiedException {
        int exitCode = su.run(String.format("ls \"%s\"", path));
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
}
