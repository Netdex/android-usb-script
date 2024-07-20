package org.netdex.androidusbscript.util;

import com.topjohnwu.superuser.ShellUtils;
import com.topjohnwu.superuser.nio.ExtendedFile;
import com.topjohnwu.superuser.nio.FileSystemManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import timber.log.Timber;


public class FileSystem {
    private final FileSystemManager remoteFs_;

    public FileSystem(FileSystemManager remoteFs) {
        this.remoteFs_ = remoteFs;
    }

    public InputStream open_r(String path) throws IOException {
        ExtendedFile file = remoteFs_.getFile(path);
        return file.newInputStream();
    }

    public OutputStream open_w(String path) throws IOException {
        ExtendedFile file = remoteFs_.getFile(path);
        return file.newOutputStream();
    }

    public void write(byte[] val, String path) throws IOException {
        Timber.v("echo -ne '%s' > %s", Util.escapeHex(val), path);
        ExtendedFile file = remoteFs_.getFile(path);
        OutputStream os = file.newOutputStream(false);
        os.write(val);
        os.close();
    }

    public <T> void write(T val, String path) throws IOException {
        Timber.v("echo '%s' > %s", val, path);
        ExtendedFile file = remoteFs_.getFile(path);
        OutputStream os = file.newOutputStream(false);
        String output = String.format("%s\n", val);
        os.write(output.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    public String readline(String path) throws IOException {
        ExtendedFile file = remoteFs_.getFile(path);
        InputStream is = file.newInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            return br.readLine();
        }
    }

    public boolean exists(String path) {
        return remoteFs_.getFile(path).exists();
    }

    public void mkdir(String path) throws IOException {
        Timber.v("mkdir %s", path);
        if (!remoteFs_.getFile(path).mkdir())
            throw new IOException(String.format("Failed to create directory '%s'", path));
    }

    public void ln(String path, String target) throws IOException {
        Timber.v("ln -s %s %s", target, path);
        if (!remoteFs_.getFile(path).createNewSymlink(target))
            throw new IOException(String.format("Failed to create symlink '%s' -> '%s'", path, target));
    }

    public void delete(String path) throws IOException {
        Timber.v("rm %s", path);
        if (!remoteFs_.getFile(path).delete())
            throw new IOException(String.format("Failed to delete '%s'", path));
    }

    public String getSystemProp(String prop) {
        return ShellUtils.fastCmd(String.format("getprop %s", prop));
    }

    public FileSystemManager get() {
        return remoteFs_;
    }
}
