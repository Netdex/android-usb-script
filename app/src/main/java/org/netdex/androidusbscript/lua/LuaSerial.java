package org.netdex.androidusbscript.lua;

import org.netdex.androidusbscript.function.DeviceStream;
import org.netdex.androidusbscript.util.FileSystem;

import java.io.IOException;
import java.nio.file.Path;


public class LuaSerial extends DeviceStream {
    public LuaSerial(FileSystem fs, Path devicePath) {
        super(fs, devicePath);
    }
}
