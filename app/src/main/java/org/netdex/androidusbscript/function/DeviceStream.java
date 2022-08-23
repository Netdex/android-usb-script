package org.netdex.androidusbscript.function;

import org.netdex.androidusbscript.util.FileSystem;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

public abstract class DeviceStream implements Closeable {
    private final FileSystem fs_;
    private final String devicePath_;

    private OutputStream os_;
    private InputStream is_;

    public DeviceStream(FileSystem fs, String devicePath) {
        this.fs_ = fs;
        this.devicePath_ = devicePath;
    }

    protected void write(byte[] b) throws IOException {
//        Log.d(TAG, String.format("write %s > %s", Util.bytesToHex(buffer), devicePath_));
        this.getOutputStream().write(b);
    }

    protected int read(byte[] b) throws IOException {
//        Log.d(TAG, String.format("read %s > %s", Util.bytesToHex(buffer), devicePath_));
        return this.getInputStream().read(b);
    }
    public int available() throws IOException {
        return this.getInputStream().available();
    }

    protected OutputStream getOutputStream() throws IOException {
        if (os_ == null) {
            if (!fs_.exists(devicePath_))
                throw new RuntimeException(String.format("Device \"%s\" does not exist", devicePath_));
            os_ = fs_.fopen_w(devicePath_);
        }
        return os_;
    }

    protected InputStream getInputStream() throws IOException {
        if (is_ == null) {
            if (!fs_.exists(devicePath_))
                throw new RuntimeException(String.format("Device \"%s\" does not exist", devicePath_));
            is_ = fs_.fopen_r(devicePath_);
        }
        return is_;
    }

    @Override
    public void close() throws IOException {
        if (os_ != null)
            os_.close();
        if (is_ != null)
            is_.close();
    }
}
