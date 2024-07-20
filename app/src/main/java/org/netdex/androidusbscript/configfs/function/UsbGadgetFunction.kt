package org.netdex.androidusbscript.configfs.function;

import org.netdex.androidusbscript.util.FileSystem;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import timber.log.Timber;

/**
 * https://www.kernel.org/doc/Documentation/usb/gadget_configfs.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/
 */
public abstract class UsbGadgetFunction {
    public static class Parameters {

    }

    protected final int id_;
    protected final Parameters params_;

    public UsbGadgetFunction(int id_, Parameters params_) {
        this.id_ = id_;
        this.params_ = params_;
    }

    /**
     * Code to be called for the function when the ConfigFS structure is being
     * created for the USB gadget
     *
     * @param fs Remote FileSystem wrapper
     */
    public void add(FileSystem fs, String gadgetPath, String configDir) throws IOException {
        this.create(fs, gadgetPath);
        this.configure(fs, gadgetPath, configDir);
    }

    protected void create(FileSystem fs, String gadgetPath) throws IOException {
        String functionDir = getName();
        String functionPath = Paths.get(gadgetPath, "functions", functionDir).toString();
        if (fs.exists(functionPath)) {
            throw new IllegalStateException(String.format("Function path '%s' already exists", functionPath));
        }
        Timber.d("Creating USB function '%s'", functionDir);
        fs.mkdir(functionPath);
    }

    protected void configure(FileSystem fs, String gadgetPath, String configDir) throws IOException {
        String functionDir = getName();
        String functionPath = Paths.get(gadgetPath, "functions", functionDir).toString();
        if (!fs.exists(functionPath)) {
            throw new IllegalStateException(String.format("Function path '%s' does not exist", functionPath));
        }
        Timber.d("Configuring USB function '%s'", functionDir);
        fs.ln(Paths.get(gadgetPath, "configs", configDir, functionDir).toString(), functionPath);
    }

    public void remove(FileSystem fs, String gadgetPath, String configDir) throws IOException {
        this.unconfigure(fs, gadgetPath, configDir);
        this.destroy(fs, gadgetPath);
    }

    protected void destroy(FileSystem fs, String gadgetPath) throws IOException {
        String functionDir = getName();
        String functionPath = Paths.get(gadgetPath, "functions", functionDir).toString();
        if (!fs.exists(functionPath)) {
            throw new IllegalStateException(String.format("Function path '%s' does not exist", functionPath));
        }
        Timber.d("Destroying USB function '%s'", functionDir);
        fs.delete(functionPath);
    }

    protected void unconfigure(FileSystem fs, String gadgetPath, String configDir) throws IOException {
        String functionDir = getName();
        String functionPath = Paths.get(gadgetPath, "configs", configDir, functionDir).toString();
        if (!fs.exists(functionPath)) {
            throw new IllegalStateException(String.format("Function symlink '%s' does not exist", functionPath));
        }
        Timber.d("Unconfiguring USB function '%s'", functionDir);
        fs.delete(functionPath);
    }

    public abstract String getName();
}
