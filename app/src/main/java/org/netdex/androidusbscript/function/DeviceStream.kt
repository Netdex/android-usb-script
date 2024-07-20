package org.netdex.androidusbscript.function

import com.topjohnwu.superuser.io.SuFile
import org.netdex.androidusbscript.util.FileSystem
import timber.log.Timber
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

abstract class DeviceStream(private val fs: FileSystem, private val devicePath: Path) :
    Closeable {

    private val lazyOutput = lazy {
        if (!waitForDevice())
            throw RuntimeException("Device '$devicePath' does not exist")
        Timber.d("Opening output stream for device '%s'", devicePath)
        fs.outputStream(devicePath)
    }

    private val lazyInput = lazy {
        if (!waitForDevice())
            throw RuntimeException("Device '$devicePath' does not exist")
        // MITIGATION: Using RootService via IPC causes my phone to kernel panic when reading
        // /dev/hidgX. Though it's not recommended, using SuFile here seems to work well enough.
        Timber.d("Opening input stream for device '%s'", devicePath)
        SuFile.open(devicePath.toString()).newInputStream()

    }

    @get:Throws(IOException::class, InterruptedException::class)
    protected val outputStream: OutputStream by lazyOutput

    @get:Throws(IOException::class, InterruptedException::class)
    protected val inputStream: InputStream by lazyInput

    @Throws(IOException::class, InterruptedException::class)
    protected fun write(b: ByteArray?) {
        outputStream.write(b)
    }

    @Throws(IOException::class, InterruptedException::class)
    protected fun read(b: ByteArray?): Int {
        return inputStream.read(b)
    }

    @Throws(IOException::class, InterruptedException::class)
    protected fun read(): Int {
        // NOTE: Under no circumstance do we allow the script to read without data being available,
        // since there is no way to cancel a blocking read.
        return inputStream.read()
    }

    @Throws(IOException::class, InterruptedException::class)
    protected fun available(): Int {
        return inputStream.available()
    }

    @Throws(InterruptedException::class)
    private fun waitForDevice(): Boolean {
        for (i in 0..4) {
            if (fs.exists(devicePath)) {
                return true
            }
            Thread.sleep(500)
        }
        return false
    }

    @Throws(IOException::class)
    override fun close() {
        Timber.d("Closing device stream '%s'", devicePath)
        if (lazyOutput.isInitialized())
            outputStream.close()
        if (lazyInput.isInitialized())
            inputStream.close()
    }
}
