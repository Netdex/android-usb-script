package cf.netdex.hidfuzzer.util;

public class Command {
    public static String echoToFile(String s, String file, boolean escapes, boolean newLine) {
        return String.format("echo %s %s \"%s\" > \"%s\"", newLine ? "" : "-n", escapes ? "-e" : "", s, file);
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
        for (int j = 0; j < arr.length; j++) {
            int v = arr[j] & 0xFF;
            sb.append("\\x").append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]);
        }
        return sb.toString();
    }
}
