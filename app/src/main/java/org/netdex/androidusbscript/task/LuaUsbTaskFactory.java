package org.netdex.androidusbscript.task;

/*
  Created by netdex on 12/28/17.
 */

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import timber.log.Timber;

public class LuaUsbTaskFactory {
    private final LuaIOBridge dialogIO_;

    public LuaUsbTaskFactory(LuaIOBridge dialogIO) {
        this.dialogIO_ = dialogIO;
    }

    public LuaUsbTask createTaskFromLuaAsset(Context context, String name, String pathToAsset) {
        try {
            return createTaskFromInputStream(name, context.getAssets().open(pathToAsset));
        } catch (IOException e) {
            Timber.e(e);
            return null;
        }
    }

    public LuaUsbTask createTaskFromLuaScript(Context context, String name, Uri uri) {
        try {
            return createTaskFromInputStream(name, context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            Timber.e(e);
            return null;
        }
    }

    private LuaUsbTask createTaskFromInputStream(String name, InputStream stream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
            br.close();
            String src = sb.toString();
            return new LuaUsbTask(name, src, dialogIO_);
        } catch (IOException e) {
            Timber.e(e);
            return null;
        }

    }
}
