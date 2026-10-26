package com.demarpsoftware.kidswordplus.content

import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

data class InstalledPackage(val version:String,val manifestHash:String,val bytes:Long)

/** File operations run on the caller's IO dispatcher. A single UI operation is allowed at a time. */
class PackageStore(private val directory: File, private val allowDraft: Boolean,
    private val openAsset: (String) -> InputStream, private val downloader: PackageDownloader = PackageDownloader()) {
    companion object { private val lock = Any() }
    fun clearStaging() = synchronized(lock) { File(directory, "staging").deleteRecursively(); Unit }
    private fun languageRoot(language: String): File {
        require(language.matches(Regex("base|[a-z]{2}")))
        return File(directory, language)
    }
    private fun revision(language: String, hash: String): File {
        require(PackageSafety.validHash(hash))
        return File(languageRoot(language), "revisions/$hash")
    }
    fun installedLanguages(): List<String> = synchronized(lock) { directory.listFiles().orEmpty().filter {
        it.isDirectory && it.name.matches(Regex("[a-z]{2}")) && File(it, "current").isFile
    }.map { it.name }.sorted() }

    fun installedDetails():Map<String,InstalledPackage> = synchronized(lock) {
        (installedLanguages()+"base").mapNotNull {language -> runCatching {
            val hash=File(languageRoot(language),"current").readText().trim()
            val root=revision(language,hash)
            val manifest=checkedManifest(root,hash,language)
            language to InstalledPackage(manifest.getString("packageVersion"),hash,manifest.getLong("installedPayloadBytes")+File(root,"manifest.json").length())
        }.getOrNull() }.toMap()
    }
    fun load(language: String): LearningPack? = synchronized(lock) {
        val pointer = File(languageRoot(language), "current")
        if (!pointer.isFile) return@synchronized null
        val hash = pointer.readText().trim()
        val root = revision(language, hash)
        val manifest = checkedManifest(root, hash, language)
        val dependency = dependencies(manifest).single()
        require(dependency.language == "base")
        val base = revision("base", dependency.hash)
        val baseManifest = checkedManifest(base, dependency.hash, "base")
        require(baseManifest.getString("packageVersion") == dependency.version && dependencies(baseManifest).isEmpty())
        LearningPack.load(root, base, language)
    }
    private fun checkedManifest(root: File, hash: String, language: String): JSONObject {
        val file = File(root, "manifest.json")
        require(file.length() in 1..5_000_000 && PackageSafety.sha(file) == hash) { "Manifest hash mismatch" }
        return JSONObject(file.readText()).also {
            require(it.getString("schemaVersion") == "2.3" && it.getString("language") == language)
            require(allowDraft || it.getBoolean("releaseReady")) { "Draft content is unavailable in Release" }
            require(it.optString("minimumReaderSchemaVersion", "2.3") in listOf("2.2", "2.3"))
        }
    }
    fun install(language: String, catalog: List<PackEntry>, check: () -> Unit, status: (String, Float) -> Unit) = synchronized(lock) {
        require(language != "base")
        val entry = catalog.single { it.language == language }
        val dependency = entry.dependencies.single()
        val base = catalog.single { it.language == "base" }
        require(dependency.language == "base" && dependency.version == base.version && dependency.hash == base.manifestHash)
        installOne(base, check, status)
        check()
        installOne(entry, check, status)
    }
    private fun validate(root: File, entry: PackEntry, check: () -> Unit) {
        val manifest = checkedManifest(root, entry.manifestHash, entry.language)
        require(manifest.getString("packageVersion") == entry.version && dependencies(manifest) == entry.dependencies)
        val files = manifest.getJSONArray("files").objects()
        require(files.size in 1..PackageSafety.MAX_ENTRIES)
        val expected = files.map { it.getString("path") }
        require(expected.distinct().size == expected.size && "manifest.json" !in expected)
        var total = 0L
        files.forEach { record ->
            check()
            val file = PackageSafety.child(root, record.getString("path"))
            val size = record.getLong("bytes")
            val hash = record.getString("sha256")
            require(size in 0..PackageSafety.MAX_FILE && PackageSafety.validHash(hash))
            require(file.isFile && file.length() == size && PackageSafety.sha(file) == hash) { "File verification failed: ${record.getString("path")}" }
            total += size; require(total <= PackageSafety.MAX_PAYLOAD)
        }
        require(total == manifest.getLong("installedPayloadBytes"))
        require(total + File(root, "manifest.json").length() == entry.installedBytes)
        val actual = root.walkTopDown().filter { it.isFile }.map { it.relativeTo(root).invariantSeparatorsPath }.toSet()
        require(actual == expected.toSet() + "manifest.json") { "Unexpected package files" }
        if (entry.language != "base") {
            val d = entry.dependencies.single()
            val base = revision("base", d.hash)
            val baseManifest = checkedManifest(base, d.hash, "base")
            require(baseManifest.getString("packageVersion") == d.version)
            LearningPack.load(root, base, entry.language)
        }
    }
    private fun installOne(entry: PackEntry, check: () -> Unit, status: (String, Float) -> Unit) {
        val destination = revision(entry.language, entry.manifestHash)
        if (destination.isDirectory) {
            status(entry.language, 0.9f)
            validate(destination, entry, check)
            check()
            PackageSafety.atomicWrite(File(languageRoot(entry.language), "current"), entry.manifestHash)
            return
        }
        directory.mkdirs()
        require(directory.usableSpace >= entry.bytes + entry.installedBytes) { "Not enough storage" }
        val partial = File(directory, "downloads/${entry.archiveHash}.part")
        status(entry.language, 0f)
        if (entry.asset != null) {
            partial.parentFile!!.mkdirs()
            try {
                openAsset("packages/${entry.asset}").use { input ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(65_536); var count = 0L
                        while (true) {
                            check(); val n = input.read(buffer); if (n < 0) break
                            count += n; require(count <= entry.bytes)
                            output.write(buffer, 0, n); status(entry.language, count.toFloat() / entry.bytes * 0.65f)
                        }
                    }
                }
                require(partial.length() == entry.bytes && PackageSafety.sha(partial) == entry.archiveHash)
            } catch (e: Exception) { partial.delete(); throw e }
        } else downloader.download(entry, partial, check) { status(entry.language, it.toFloat() / entry.bytes * 0.65f) }
        check()
        val stage = File(directory, "staging/${UUID.randomUUID()}")
        require(stage.mkdirs())
        try {
            status(entry.language, 0.7f)
            PackageSafety.extract(partial.inputStream(), stage, check)
            status(entry.language, 0.85f)
            validate(stage, entry, check)
            check()
            destination.parentFile!!.mkdirs()
            Files.move(stage.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE)
            PackageSafety.atomicWrite(File(languageRoot(entry.language), "current"), entry.manifestHash)
            partial.delete()
            status(entry.language, 1f)
        } finally { stage.deleteRecursively() }
    }
    fun remove(language: String) = synchronized(lock) {
        require(language != "base")
        // Remove pointer first. Base revisions and progress live outside this directory.
        val root = languageRoot(language)
        val pointer = File(root, "current")
        require(!pointer.exists() || pointer.delete())
        require(!root.exists() || root.deleteRecursively())
    }
}
