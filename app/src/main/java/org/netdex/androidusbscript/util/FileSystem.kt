package org.netdex.androidusbscript.util

import com.topjohnwu.superuser.ShellUtils
import com.topjohnwu.superuser.nio.FileSystemManager
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class FileSystem(private val remoteFs: FileSystemManager) {
    @Throws(IOException::class)
    fun inputStream(path: Path): InputStream {
        val file = remoteFs.getFile(path.toString())
        return file.newInputStream()
    }

    @Throws(IOException::class)
    fun outputStream(path: Path): OutputStream {
        val file = remoteFs.getFile(path.toString())
        return file.newOutputStream()
    }

    @Throws(IOException::class)
    fun write(v: ByteArray, path: Path) {
        Timber.v("echo -ne '%s' > %s", Util.escapeHex(v), path)
        val file = remoteFs.getFile(path.toString())
        val os = file.newOutputStream(false)
        os.write(v)
        os.close()
    }

    @Throws(IOException::class)
    fun <T> write(v: T, path: Path) {
        Timber.v("echo '%s' > %s", v, path)
        val file = remoteFs.getFile(path.toString())
        val stream = file.newOutputStream(false)
        val output = String.format("%s\n", v)
        stream.write(output.toByteArray(StandardCharsets.UTF_8))
        stream.close()
    }

    @Throws(IOException::class)
    fun readLine(path: Path): String {
        val file = remoteFs.getFile(path.toString())
        val stream = file.newInputStream()
        BufferedReader(InputStreamReader(stream)).use { br -> return br.readLine() }
    }

    fun exists(path: Path): Boolean {
        return remoteFs.getFile(path.toString()).exists()
    }

    @Throws(IOException::class)
    fun mkdir(path: Path) {
        Timber.v("mkdir %s", path)
        if (!remoteFs.getFile(path.toString()).mkdir())
            throw IOException(String.format("Failed to create directory '%s'", path))
    }

    @Throws(IOException::class)
    fun ln(path: Path, target: Path) {
        Timber.v("ln -s %s %s", target, path)
        if (!remoteFs.getFile(path.toString()).createNewSymlink(target.toString()))
            throw IOException(String.format("Failed to create symlink '%s' -> '%s'", path, target))
    }

    @Throws(IOException::class)
    fun delete(path: Path) {
        Timber.v("rm %s", path)
        if (!remoteFs.getFile(path.toString()).delete())
            throw IOException(String.format("Failed to delete '%s'", path))
    }

    fun getSystemProp(prop: String): String {
        return ShellUtils.fastCmd("getprop '$prop'")
    }

    fun get(): FileSystemManager {
        return remoteFs
    }
}
