package org.netdex.hidfuzzer.task;

/**
 * Created by netdex on 12/28/17.
 */

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LuaHIDTaskFactory {
    private AsyncIOBridge dialogIO_;

    public LuaHIDTaskFactory(AsyncIOBridge dialogIO) {
        this.dialogIO_ = dialogIO;
    }

    public LuaHIDTask createTaskFromLuaFile(Context context, String name, String path) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(path)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
            br.close();
            String src = sb.toString();
            return new LuaHIDTask(name, src, dialogIO_);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
