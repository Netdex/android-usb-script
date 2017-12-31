package cf.netdex.hidfuzzer.ltask;

/**
 * Created by netdex on 12/28/17.
 */

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaTaskLoader {
    public static Globals createGlobals(HIDTask task) {
        Globals globals = JsePlatform.standardGlobals();
        task.getLuaHIDBinding().bind(globals);
        return globals;
    }

    public static LuaValue loadChunk(Globals globals, String code) {
        LuaValue chunk = globals.load(code);
        return chunk;
    }
}
