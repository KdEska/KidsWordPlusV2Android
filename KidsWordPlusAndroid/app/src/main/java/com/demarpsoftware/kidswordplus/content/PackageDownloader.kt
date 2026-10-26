package com.demarpsoftware.kidswordplus.content

import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection

class DownloadResponse(val status: Int, val length: Long, val range: String?, val encoding: String?,
    val stream: InputStream, private val closeConnection: () -> Unit = {}) : AutoCloseable {
    override fun close() { try { stream.close() } finally { closeConnection() } }
}
fun interface PackageTransport { fun open(url: String, offset: Long): DownloadResponse }
class HttpsTransport : PackageTransport {
    override fun open(url: String, offset: Long): DownloadResponse {
        val connection = PackageSafety.https(url).toURL().openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 15_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("Accept-Encoding", "identity")
        if (offset > 0) connection.setRequestProperty("Range", "bytes=$offset-")
        try {
            val status = connection.responseCode
            require(status == 200 || status == 206) { "HTTP $status" }
            return DownloadResponse(status, connection.contentLengthLong, connection.getHeaderField("Content-Range"),
                connection.getHeaderField("Content-Encoding"), connection.inputStream) { connection.disconnect() }
        } catch (e: Exception) { connection.disconnect(); throw e }
    }
}
class PackageDownloader(private val transport: PackageTransport = HttpsTransport()) {
    fun download(entry: PackEntry, partial: File, check: () -> Unit, progress: (Long) -> Unit): File {
        val url = entry.url ?: error("No URL")
        PackageSafety.https(url)
        if (partial.exists() && partial.length() > entry.bytes) partial.delete()
        if (partial.length() == entry.bytes && partial.isFile) {
            if (PackageSafety.sha(partial) == entry.archiveHash) return partial
            partial.delete()
        }
        var offset = if (partial.isFile) partial.length() else 0L
        check()
        transport.open(url, offset).use { response ->
            require(response.encoding.isNullOrBlank() || response.encoding == "identity") { "Compressed transport rejected" }
            when (response.status) {
                200 -> offset = 0L // Server without Range support: truncate and restart.
                206 -> require(response.range == "bytes $offset-${entry.bytes - 1}/${entry.bytes}") { "Invalid Content-Range" }
                else -> error("HTTP ${response.status}")
            }
            require(response.length == -1L || response.length == entry.bytes - offset) { "Invalid Content-Length" }
            partial.parentFile!!.mkdirs()
            var written = offset
            partial.outputStreamAppend(offset > 0).use { output ->
                val buffer = ByteArray(65_536)
                while (true) {
                    check()
                    val count = response.stream.read(buffer)
                    if (count < 0) break
                    require(written + count <= entry.bytes) { "Download exceeds expected size" }
                    output.write(buffer, 0, count); written += count; progress(written)
                }
            }
            require(written == entry.bytes) { "Incomplete download; retry to resume" }
        }
        check()
        if (PackageSafety.sha(partial) != entry.archiveHash) {
            partial.delete(); error("Archive hash mismatch")
        }
        return partial
    }
    fun catalog(url: String, check: () -> Unit): String {
        PackageSafety.https(url)
        return transport.open(url, 0).use { response ->
            require(response.status == 200)
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(16_384)
            while (true) {
                check(); val count = response.stream.read(buffer); if (count < 0) break
                require(output.size() + count <= 2_000_000) { "Catalog too large" }
                output.write(buffer, 0, count)
            }
            output.toString("UTF-8")
        }
    }
}
private fun File.outputStreamAppend(append: Boolean) = java.io.FileOutputStream(this, append)
