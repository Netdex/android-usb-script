package cf.netdex.hidfuzzer.ltask;

/**
 * Created by netdex on 12/28/17.
 */

import org.luaj.vm2.*;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.IoLib;
import org.luaj.vm2.lib.MathLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.*;

import cf.netdex.hidfuzzer.ltask.HIDTask;

public class LuaTaskLoader {
    public static Globals createGlobals(HIDTask task){
        Globals globals = JsePlatform.standardGlobals();
        task.getLuaHIDBinding().bind(globals);
        return globals;
    }

    public static LuaValue loadChunk(Globals globals, String code){
        LuaValue chunk = globals.load(code);
        return chunk;
    }
}
