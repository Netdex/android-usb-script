package org.netdex.androidusbscript.util;

public class Util {
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static String escapeHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 4];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 4] = '\\';
            hexChars[j * 4 + 1] = 'x';
            hexChars[j * 4 + 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 4 + 3] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
