package org.netdex.hidfuzzer.configfs.function;

import org.netdex.hidfuzzer.util.Command;

import java.nio.file.Paths;

import eu.chainfire.libsuperuser.Shell;

/**
 * https://www.kernel.org/doc/Documentation/usb/gadget_configfs.txt
 * https://www.kernel.org/doc/Documentation/ABI/testing/
 */
public abstract class UsbGadgetFunction {
    public static class Parameters {

    }

    protected int id_;
    protected Parameters params_;

    public UsbGadgetFunction(int id_, Parameters params_) {
        this.id_ = id_;
        this.params_ = params_;
    }

    /**
     * Code to be called for the function when the ConfigFS structure is being
     * created for the USB gadget
     *
     * @param su Instance of SU shell
     */
    public void create(Shell.Threaded su, String gadgetPath) throws Shell.ShellDiedException {
        String functionDir = getFunctionDir();
        String functionPath = Paths.get(gadgetPath, "functions", functionDir).toString();
        if (Command.pathExists(su, functionPath)) {
            throw new IllegalStateException(String.format("Function path \"%s\" already exists", functionPath));
        }
        int exitCode = su.run(new String[]{
                String.format("cd \"%s\"", gadgetPath),
                String.format("mkdir \"%s\"", functionPath),
        });
        if (exitCode != 0)
            throw new RuntimeException(String.format("Failed to create function \"%s\": errno=%d", functionDir, exitCode));
    }

    public void bind(Shell.Threaded su, String gadgetPath, String configDir) throws Shell.ShellDiedException {
        String functionDir = getFunctionDir();
        String functionPath = Paths.get(gadgetPath, "functions", functionDir).toString();
        if (!Command.pathExists(su, functionPath)) {
            throw new IllegalStateException(String.format("Function path \"%s\" does not exist", functionPath));
        }

        int exitCode = su.run(new String[]{
                String.format("cd \"%s\"", gadgetPath),
                String.format("ln -s \"%s\" \"configs/%s\"", functionPath, configDir),
        });
        if (exitCode != 0)
            throw new RuntimeException(String.format("Failed to bind function \"%s\": errno=%d", functionDir, exitCode));
    }

    public void unbind(Shell.Threaded su, String gadgetPath, String configDir) throws Shell.ShellDiedException {
        String functionDir = getFunctionDir();
        String linkPath = Paths.get(gadgetPath, "configs", configDir, functionDir).toString();
        if (!Command.pathExists(su, linkPath)) {
            throw new IllegalStateException(String.format("Function symlink \"%s\" does not exist", linkPath));
        }

        int exitCode = su.run(new String[]{
                String.format("cd \"%s\"", gadgetPath),
                String.format("rm \"%s\"", linkPath),
        });
        if (exitCode != 0)
            throw new RuntimeException(String.format("Failed to unbind function \"%s\": errno=%d", functionDir, exitCode));
    }

    public void remove(Shell.Threaded su, String gadgetPath) throws Shell.ShellDiedException {
        String functionDir = getFunctionDir();
        String functionPath = Paths.get(gadgetPath, "functions", functionDir).toString();
        if (!Command.pathExists(su, functionPath)) {
            throw new IllegalStateException(String.format("Function path \"%s\" does not exist", functionPath));
        }

        int exitCode = su.run(new String[]{
                String.format("rmdir \"%s\"", functionPath),
        });
        if (exitCode != 0)
            throw new RuntimeException(String.format("Failed to remove function \"%s\": errno=%d", functionDir, exitCode));
    }

    public abstract String getFunctionDir();
}
