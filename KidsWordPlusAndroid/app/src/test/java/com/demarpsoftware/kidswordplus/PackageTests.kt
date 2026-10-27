package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.content.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PackageTests {
    @get:Rule val temporary = TemporaryFolder()
    private fun hash(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    private fun zip(files: Map<String, ByteArray>): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        ZipOutputStream(output).use { zip -> files.forEach { (name, data) -> zip.putNextEntry(ZipEntry(name)); zip.write(data); zip.closeEntry() } }
        return output.toByteArray()
    }
    private fun rejects(block: () -> Unit) {
        try { block(); fail("Expected rejection") } catch (_: IllegalArgumentException) { } catch (_: IllegalStateException) { }
    }
    private fun fixture(language: String, version: String, dependency: Dependency? = null, brokenMedia: Boolean = false, legacyBeer: Boolean = false): Pair<PackEntry, ByteArray> {
        val files = mutableMapOf("images/a.jpg" to byteArrayOf(1, 2, 3))
        if (language != "base") {
            files.clear(); files["audio/a.mp3"] = byteArrayOf(4, 5)
            val word = JSONObject().put("conceptId", "animals:1").put("categoryId", "animals").put("subcategoryId", "pets")
                .put("lessonNumber", 1).put("lessonPosition", 1).put("displayText", "Cat")
                .put("audio", JSONObject().put("path", if (brokenMedia) "audio/missing.mp3" else "audio/a.mp3"))
                .put("image", JSONObject().put("path", "images/a.jpg").put("package", "base"))
            val words=JSONArray().put(word)
            if(legacyBeer)words.put(JSONObject(word.toString()).put("conceptId","food:792").put("displayText","Beer"))
            files["database.json"] = JSONObject().put("schemaVersion", "2.3").put("language", language)
                .put("words", words).toString().toByteArray()
        }
        val dependencies = JSONArray()
        if (dependency != null) dependencies.put(JSONObject().put("language", dependency.language).put("version", dependency.version).put("manifestSha256", dependency.hash))
        val manifest = JSONObject().put("schemaVersion", "2.3").put("language", language).put("packageVersion", version)
            .put("minimumReaderSchemaVersion", "2.3").put("releaseReady", false).put("databasePath", "database.json")
            .put("dependencies", dependencies).put("installedPayloadBytes", files.values.sumOf { it.size })
            .put("files", JSONArray(files.map { (name, bytes) -> JSONObject().put("path", name).put("bytes", bytes.size).put("sha256", hash(bytes)) }))
            .toString().toByteArray()
        files["manifest.json"] = manifest
        val archive = zip(files)
        return PackEntry(language, language, version, hash(archive), hash(manifest), archive.size.toLong(),
            files.values.sumOf { it.size.toLong() }, listOfNotNull(dependency), null, "$language-${hash(archive)}.zip") to archive
    }
    @Test fun legacyBeerIsHiddenFromLessonsAndGames() {
        val base=fixture("base","1")
        val en=fixture("en","2",Dependency("base",base.first.version,base.first.manifestHash),legacyBeer=true)
        val archives=mapOf("packages/"+base.first.asset to base.second,"packages/"+en.first.asset to en.second)
        val store=PackageStore(temporary.newFolder("legacyBeer"),true,{ByteArrayInputStream(archives.getValue(it))})
        store.install("en",listOf(base.first,en.first),{}, {_,_->})
        val pack=store.load("en")!!
        assertEquals(listOf("animals:1"),pack.words.map {it.id})
        assertTrue(pack.lessons.flatMap {it.words}.none {it.id=="food:792"})
    }
    @Test fun installedMetadataUsesVerifiedRevisionAndIgnoresBrokenOtherPointers() {
        val base=fixture("base","1")
        val tr=fixture("tr","2",Dependency("base",base.first.version,base.first.manifestHash))
        val catalog=listOf(base.first,tr.first)
        val archives=mapOf("packages/"+base.first.asset to base.second,"packages/"+tr.first.asset to tr.second)
        val directory=temporary.newFolder("metadata")
        val store=PackageStore(directory,true,{ByteArrayInputStream(archives.getValue(it))})
        assertTrue(store.installedDetails().isEmpty())
        store.install("tr",catalog,{}, {_,_->})
        val details=store.installedDetails()
        assertEquals(InstalledPackage("2",tr.first.manifestHash,tr.first.installedBytes),details["tr"])
        assertEquals(base.first.installedBytes,details.getValue("base").bytes)
        File(directory,"en").mkdirs();File(directory,"en/current").writeText("invalid")
        assertEquals(details,store.installedDetails())
        File(directory,"tr/revisions/${tr.first.manifestHash}/manifest.json").appendText(" ")
        assertFalse("tr" in store.installedDetails())
        assertEquals(details["base"],store.installedDetails()["base"])
    }
    @Test fun legacyWineIsExcludedBeforeCheckingItsRemovedMedia() {
        val sources = File(requireNotNull(System.getProperty("kwp.packages")))
        val source = File(sources,"packages/en")
        val db = JSONObject(File(source,"database.json").readText())
        val first = db.getJSONArray("words").getJSONObject(0)
        val wine = JSONObject(first.toString()).put("conceptId","food:830").put("displayText","Wine")
        wine.getJSONObject("image").put("path","images/removed-wine.jpg").remove("overridePath")
        db.put("words",org.json.JSONArray().put(first).put(wine));db.remove("curricula")
        val temporary = java.nio.file.Files.createTempDirectory("kwp-wine-filter").toFile()
        try {
            File(temporary,"manifest.json").writeText(JSONObject().put("databasePath","database.json").toString())
            File(temporary,"database.json").writeText(db.toString())
            val audio = first.getJSONObject("audio").getString("path")
            File(temporary,audio).apply {parentFile!!.mkdirs();File(source,audio).copyTo(this)}
            val loaded = LearningPack.load(temporary,File(sources,"packages/base"),"en")
            assertEquals(listOf(first.getString("conceptId")),loaded.words.map {it.id})
        } finally {temporary.deleteRecursively()}
    }
    @Test fun realDatabasesRespectIosLessonLayoutWithoutCopyingContent() {
        val sources = File(requireNotNull(System.getProperty("kwp.packages")))
        val base = File(sources, "packages/base")
        val tr = LearningPack.load(File(sources, "packages/tr"), base, "tr")
        val en = LearningPack.load(File(sources, "packages/en"), base, "en")
        assertEquals(1701, tr.words.size)
        assertEquals(1698, en.words.size)
        for (pack in listOf(tr, en)) {
            assertEquals(pack.words.size, pack.lessons.sumOf { it.words.size })
            assertEquals(pack.words.map { it.id }.toSet(), pack.lessons.flatMap { it.words }.map { it.id }.toSet())
            assertTrue(pack.lessons.all { it.words.size in 1..10 })
            assertTrue(pack.lessons.all { lesson -> lesson.words.all { it.subcategory == lesson.subcategory && it.category == lesson.category } })
        }
        assertEquals(listOf(10, 10, 9), tr.lessons.filter { it.subcategory == "alphabet" }.map { it.words.size })
    }
    @Test fun realPackagesInstallOfflineAndKeepBasePinned() {
        val sources = File(requireNotNull(System.getProperty("kwp.packages")))
        val sourceCatalog = JSONObject(File(sources, "catalog.json").readText())
        val original = sourceCatalog.getJSONArray("packages").objects().filter { it.getString("language") in listOf("base", "tr", "en") }
        val archives = original.associate { record ->
            "packages/${record.getString("language")}-${record.getString("archiveSha256")}.zip" to File(sources, record.getString("localArchive"))
        }
        original.forEach { it.put("assetArchive", "${it.getString("language")}-${it.getString("archiveSha256")}.zip") }
        sourceCatalog.put("packages", JSONArray(original))
        val catalog = Catalog.parse(sourceCatalog.toString(), true, true)
        val directory = temporary.newFolder("content")
        val store = PackageStore(directory, true, { archives.getValue(it).inputStream() })
        store.install("tr", catalog, {}, { _, _ -> })
        store.install("en", catalog, {}, { _, _ -> })
        val tr = store.load("tr")!!
        val en = store.load("en")!!
        assertEquals(1701, tr.words.size)
        assertEquals(1698, en.words.size)
        assertEquals(17, tr.words.map { it.category }.distinct().size)
        assertEquals(tr.words.size, tr.lessons.sumOf { it.words.size })
        assertTrue(tr.lessons.all { it.words.size in 1..10 })
        assertTrue(en.lessons.all { it.words.size in 1..10 })
        assertTrue(tr.lessons.all { lesson -> lesson.words.all { it.subcategory == lesson.subcategory && it.category == lesson.category } })
        val alphabet = tr.lessons.filter { it.subcategory == "alphabet" }
        assertEquals(listOf(10, 10, 9), alphabet.map { it.words.size })
        assertNotEquals(alphabet.flatMap { it.words }.find { it.text == "I" }!!.id, alphabet.flatMap { it.words }.find { it.text == "İ" }!!.id)
        val progress = File(temporary.root, "progress.json").apply { writeText("keep") }
        store.remove("tr")
        assertNull(store.load("tr"))
        assertEquals(en.words.size, store.load("en")!!.words.size)
        assertEquals("keep", progress.readText())
        assertTrue(en.base.isDirectory)
    }
    @Test fun invalidAndCancelledUpdatesPreservePreviousInstallation() {
        val base = fixture("base", "1")
        val d = Dependency("base", "1", base.first.manifestHash)
        val good = fixture("en", "1", d)
        val broken = fixture("en", "2", d, brokenMedia = true)
        val update = fixture("en", "3", d)
        val bytes = listOf(base, good, broken, update).associate { "packages/${it.first.asset}" to it.second }
        val directory = temporary.newFolder("content")
        val store = PackageStore(directory, true, { ByteArrayInputStream(bytes.getValue(it)) })
        store.install("en", listOf(base.first, good.first), {}, { _, _ -> })
        val old = store.load("en")!!.root
        rejects { store.install("en", listOf(base.first, broken.first), {}, { _, _ -> }) }
        assertEquals(old, store.load("en")!!.root)
        var cancel = false
        try {
            store.install("en", listOf(base.first, update.first), { if (cancel) throw java.util.concurrent.CancellationException() },
                { code, progress -> if (code == "en" && progress >= 0.85f) cancel = true })
            fail("Expected cancellation")
        } catch (_: java.util.concurrent.CancellationException) { }
        assertEquals(old, store.load("en")!!.root)
        assertTrue(File(directory, "staging").listFiles().orEmpty().isEmpty())
        val release = PackageStore(directory, false, { error("unused") })
        rejects { release.load("en") }
    }
    @Test fun catalogRejectsDependencyMismatchAndRemoteAssetInjection() {
        val source = File(requireNotNull(System.getProperty("kwp.packages")), "catalog.json")
        val json = JSONObject(source.readText())
        val records = json.getJSONArray("packages").objects().filter { it.getString("language") in listOf("base", "en") }
        records.forEach { it.put("availability", "published").put("downloadUrl", "https://example.com/a.zip").put("assetArchive", "../injected") }
        json.put("packages", JSONArray(records))
        assertTrue(Catalog.parse(json.toString(), false, false).all { it.asset == null })
        records.first { it.getString("language") == "en" }.getJSONArray("dependencies").getJSONObject(0).put("manifestSha256", "0".repeat(64))
        rejects { Catalog.parse(json.toString(), false, false) }
    }
    @Test fun traversalAndUnexpectedFilesAreRejected() {
        val stage = temporary.newFolder("stage")
        rejects { PackageSafety.extract(ByteArrayInputStream(zip(mapOf("../escape" to byteArrayOf(1)))), stage, {}) }
        assertFalse(File(temporary.root, "escape").exists())
        rejects { PackageSafety.child(stage, "x\\y") }
        rejects { PackageSafety.child(stage, "/outside") }
        rejects { PackageSafety.https("http://example.com") }
    }
    @Test fun resumedDownloadAndServerRestartAreVerified() {
        val bytes = "complete archive".toByteArray()
        val entry = PackEntry("en", "English", "1", hash(bytes), "0".repeat(64), bytes.size.toLong(), 100, emptyList(), "https://example.com/a.zip", null)
        val part = File(temporary.root, "a.part").apply { writeBytes(bytes.take(5).toByteArray()) }
        PackageDownloader(PackageTransport { _, offset ->
            assertEquals(5L, offset)
            DownloadResponse(206, (bytes.size - 5).toLong(), "bytes 5-${bytes.lastIndex}/${bytes.size}", null, ByteArrayInputStream(bytes.copyOfRange(5, bytes.size)))
        }).download(entry, part, {}, {})
        assertArrayEquals(bytes, part.readBytes())
        part.writeBytes(bytes.take(3).toByteArray())
        PackageDownloader(PackageTransport { _, _ -> DownloadResponse(200, bytes.size.toLong(), null, null, ByteArrayInputStream(bytes)) }).download(entry, part, {}, {})
        assertArrayEquals(bytes, part.readBytes())
        part.writeBytes(bytes.take(3).toByteArray())
        rejects { PackageDownloader(PackageTransport { _, _ -> DownloadResponse(206, 1, "bytes 1-2/3", null, ByteArrayInputStream(bytes)) }).download(entry, part, {}, {}) }
        assertEquals(3L, part.length())
        val bad = ByteArray(bytes.size) { 0 }
        rejects { PackageDownloader(PackageTransport { _, _ -> DownloadResponse(200, bad.size.toLong(), null, null, ByteArrayInputStream(bad)) }).download(entry, part, {}, {}) }
        assertFalse(part.exists())
    }
    @Test fun interruptedDownloadKeepsBytesForRetry() {
        val bytes = "a longer complete archive".toByteArray()
        val entry = PackEntry("en", "English", "1", hash(bytes), "0".repeat(64), bytes.size.toLong(), 100, emptyList(), "https://example.com/a.zip", null)
        val part = File(temporary.root, "partial")
        val interrupted = object : java.io.InputStream() {
            var position = 0
            override fun read(): Int { if (position == 7) throw IOException("disconnect"); return bytes[position++].toInt() }
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                if (position == 7) throw IOException("disconnect")
                val count = minOf(length, 7 - position)
                bytes.copyInto(buffer, offset, position, position + count); position += count; return count
            }
        }
        try { PackageDownloader(PackageTransport { _, _ -> DownloadResponse(200, bytes.size.toLong(), null, null, interrupted) }).download(entry, part, {}, {}); fail() }
        catch (_: IOException) { }
        assertEquals(7L, part.length())
        PackageDownloader(PackageTransport { _, offset -> DownloadResponse(206, bytes.size - offset, "bytes $offset-${bytes.lastIndex}/${bytes.size}", null, ByteArrayInputStream(bytes.copyOfRange(offset.toInt(), bytes.size))) }).download(entry, part, {}, {})
        assertArrayEquals(bytes, part.readBytes())
    }
}
