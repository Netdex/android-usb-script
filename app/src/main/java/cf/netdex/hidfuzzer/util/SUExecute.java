package cf.netdex.hidfuzzer.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by netdex on 1/16/2017.
 */

public class SUExecute {
    public static Process execute(String command) {
        try {
            return Runtime.getRuntime().exec(new String[]{"su", "-c", command});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
