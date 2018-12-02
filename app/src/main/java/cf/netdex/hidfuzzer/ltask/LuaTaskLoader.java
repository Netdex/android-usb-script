package cf.netdex.hidfuzzer.ltask;

/**
 * Created by netdex on 12/28/17.
 */

import android.content.Context;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LuaTaskLoader {
    public static Globals createGlobals(HIDTask task) {
        Globals globals = JsePlatform.standardGlobals();
        task.getLuaHIDBinding().bind(globals);
        return globals;
    }

    public static LuaValue loadChunk(Globals globals, String code) {
        return globals.load(code);
    }

    public static HIDTask createTaskFromLuaFile(Context context, String name, String path) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(path)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
            br.close();
            String src = sb.toString();
            return new HIDTask(context, name, src);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
