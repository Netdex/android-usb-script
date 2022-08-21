package org.netdex.androidusbscript.util;

import static org.netdex.androidusbscript.MainActivity.TAG;

import android.util.Log;

import com.topjohnwu.superuser.ShellUtils;
import com.topjohnwu.superuser.nio.ExtendedFile;
import com.topjohnwu.superuser.nio.FileSystemManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;


public class FileSystem {
    private final FileSystemManager remoteFs_;

    public FileSystem(FileSystemManager remoteFs) {
        this.remoteFs_ = remoteFs;
    }

    public InputStream fopen_r(String path) throws IOException {
        ExtendedFile file = remoteFs_.getFile(path);
        return file.newInputStream();
    }

    public OutputStream fopen_w(String path) throws IOException {
        ExtendedFile file = remoteFs_.getFile(path);
        return file.newOutputStream();
    }

    public void fwrite(byte[] val, String path) throws IOException {
        Log.d(TAG, String.format("echo -ne %s > %s", Util.bytesToHex(val), path));
        ExtendedFile file = remoteFs_.getFile(path);
        OutputStream os = file.newOutputStream(false);
        os.write(val);
        os.close();
    }

    public <T> void fwrite(T val, String path) throws IOException {
        Log.d(TAG, String.format("echo \"%s\" > %s", val, path));
        ExtendedFile file = remoteFs_.getFile(path);
        OutputStream os = file.newOutputStream(false);
        String output = String.format("%s\n", val);
        os.write(output.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    public String fscan(String path) throws IOException {
        ExtendedFile file = remoteFs_.getFile(path);
        InputStream is = file.newInputStream();
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                out.append(buffer, 0, numRead);
            }
        }
        return out.toString();
    }

    public String freadline(String path) throws IOException {
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
        Log.d(TAG, "mkdir " + path);
        if (!remoteFs_.getFile(path).mkdir())
            throw new IOException(String.format("Failed to create directory '%s'", path));
    }

    public void ln(String path, String target) throws IOException {
        Log.d(TAG, "ln -s " + target + " " + path);
        if (!remoteFs_.getFile(path).createNewSymlink(target))
            throw new IOException(String.format("Failed to create symlink '%s' -> '%s'", path, target));
    }

    public void delete(String path) throws IOException {
        Log.d(TAG, "rm " + path);
        if (!remoteFs_.getFile(path).delete())
            throw new IOException(String.format("Failed to delete '%s'", path));
    }

    public String getSystemProp(String prop) {
        return ShellUtils.fastCmd(String.format("getprop %s", prop));
    }

    public FileSystemManager getRemoteFs() {
        return remoteFs_;
    }
}
