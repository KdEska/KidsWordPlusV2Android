package com.demarpsoftware.kidswordplus.content

import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.zip.ZipInputStream

object PackageSafety {
    const val MAX_ARCHIVE = 1_000_000_000L
    const val MAX_PAYLOAD = 1_000_000_000L
    const val MAX_FILE = 100_000_000L
    const val MAX_ENTRIES = 20_000
    fun validHash(value: String) = value.matches(Regex("[a-f0-9]{64}"))
    fun https(value: String): URI = URI(value).also {
        require(it.scheme == "https" && !it.host.isNullOrEmpty() && it.userInfo == null && it.fragment == null) { "HTTPS required" }
    }
    fun child(root: File, path: String): File {
        require(path.isNotBlank() && !path.startsWith('/') && !path.contains('\\') && !path.contains('\u0000')) { "Unsafe path" }
        require(path.split('/').none { it == ".." || it == "." || it.contains(':') || it.isEmpty() }) { "Unsafe path" }
        val file = File(root, path).canonicalFile
        require(file.path.startsWith(root.canonicalPath + File.separator)) { "Path escapes package" }
        return file
    }
    fun sha(file: File): String = file.inputStream().use { input ->
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(65_536)
        while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
        digest.digest().joinToString("") { "%02x".format(it) }
    }
    fun atomicWrite(file: File, text: String) {
        file.parentFile!!.mkdirs()
        val temporary = File(file.parentFile, file.name + ".tmp")
        temporary.outputStream().use { it.write(text.toByteArray(Charsets.UTF_8)); it.fd.sync() }
        Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }
    fun extract(input: InputStream, destination: File, check: () -> Unit) {
        val paths = HashSet<String>()
        var total = 0L
        var entries = 0
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                check()
                val entry = zip.nextEntry ?: break
                require(++entries <= MAX_ENTRIES)
                val name = entry.name.removeSuffix("/")
                require(paths.add(name)) { "Duplicate ZIP entry" }
                val target = child(destination, name)
                if (entry.isDirectory) { require(target.mkdirs() || target.isDirectory) }
                else {
                    require(entry.size <= MAX_FILE)
                    target.parentFile!!.mkdirs()
                    var size = 0L
                    target.outputStream().use { output ->
                        val buffer = ByteArray(65_536)
                        while (true) {
                            check()
                            val count = zip.read(buffer)
                            if (count < 0) break
                            size += count; total += count
                            require(size <= MAX_FILE && total <= MAX_PAYLOAD) { "ZIP size limit exceeded" }
                            output.write(buffer, 0, count)
                        }
                    }
                }
                zip.closeEntry() // ZipInputStream verifies CRC; entries are always written as regular files.
            }
        }
        require(entries > 0)
    }
}
