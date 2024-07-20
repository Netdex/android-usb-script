package org.netdex.androidusbscript.function;

import com.topjohnwu.superuser.io.SuFile;

import org.netdex.androidusbscript.util.FileSystem;
import org.netdex.androidusbscript.util.Util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public abstract class DeviceStream implements Closeable {
    private final FileSystem fs_;
    private final String devicePath_;

    private OutputStream os_;
    private InputStream is_;

    public DeviceStream(FileSystem fs, String devicePath) {
        this.fs_ = fs;
        this.devicePath_ = devicePath;
    }

    protected void write(byte[] b) throws IOException, InterruptedException {
//        Timber.v("write(%s) < %s", devicePath_, Util.bytesToHex(b));
        this.getOutputStream().write(b);
    }

    protected int read(byte[] b) throws IOException, InterruptedException {
        int ret = this.getInputStream().read(b);
//        Timber.v("read(%s) > %s", devicePath_, Util.bytesToHex(b));
        return ret;
    }

    protected int read() throws IOException, InterruptedException {
        // NOTE: Under no circumstance do we allow the script to read without data being available,
        // since there is no way to cancel a blocking read.
        return this.getInputStream().read();
    }

    protected int available() throws IOException, InterruptedException {
        return this.getInputStream().available();
    }

    protected OutputStream getOutputStream() throws IOException, InterruptedException {
        if (os_ == null) {
            if (!waitForDevice())
                throw new RuntimeException(String.format("Device '%s' does not exist", devicePath_));
            Timber.d("Opening output stream for device '%s'", devicePath_);
            os_ = fs_.open_w(devicePath_);
        }
        return os_;
    }

    protected InputStream getInputStream() throws IOException, InterruptedException {
        if (is_ == null) {
            if (!waitForDevice())
                throw new RuntimeException(String.format("Device \"%s\" does not exist", devicePath_));
            // MITIGATION: Using RootService via IPC causes my phone to kernel panic when reading
            // /dev/hidgX. Though it's not recommended, using SuFile here seems to work well enough.
            Timber.d("Opening input stream for device '%s'", devicePath_);
            is_ = SuFile.open(devicePath_).newInputStream();
        }
        return is_;
    }

    private boolean waitForDevice() throws InterruptedException {
        for (int i = 0; i < 5; ++i) {
            if (fs_.exists(devicePath_)) {
                return true;
            }
            Thread.sleep(500);
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        Timber.d("Closing device stream '%s'", devicePath_);
        if (os_ != null) {
            os_.close();
            os_ = null;
        }
        if (is_ != null) {
            is_.close();
            is_ = null;
        }
    }
}
