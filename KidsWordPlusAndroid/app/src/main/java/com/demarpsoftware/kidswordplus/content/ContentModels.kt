package com.demarpsoftware.kidswordplus.content

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }
fun JSONArray.strings(): List<String> = List(length()) { getString(it) }
private fun JSONObject.optional(name: String) = if (isNull(name)) null else optString(name).takeIf { it.isNotEmpty() }

data class Dependency(val language: String, val version: String, val hash: String)
data class PackEntry(val language: String, val name: String, val version: String, val archiveHash: String,
    val manifestHash: String, val bytes: Long, val installedBytes: Long, val dependencies: List<Dependency>,
    val url: String?, val asset: String?)
fun dependencies(json: JSONObject): List<Dependency> = json.optJSONArray("dependencies")?.objects()?.map {
    Dependency(it.getString("language"), it.getString("version"), it.getString("manifestSha256"))
}.orEmpty()

object Catalog {
    fun parse(text: String, local: Boolean, allowDraft: Boolean): List<PackEntry> {
        val root = JSONObject(text)
        require(root.getString("schemaVersion") == "2.3") { "Unsupported catalog" }
        val entries = root.getJSONArray("packages").objects().filter {
            it.getString("availability") == "published" || (allowDraft && it.getString("availability") == "draft")
        }.map {
            PackEntry(it.getString("language"), it.getString("nativeName"), it.getString("version"),
                it.getString("archiveSha256"), it.getString("manifestSha256"), it.getLong("downloadBytes"),
                it.getLong("installedBytes"), dependencies(it), if (local) null else it.optional("downloadUrl"),
                if (local) it.getString("assetArchive") else null)
        }
        require(entries.map { it.language }.distinct().size == entries.size)
        entries.forEach { entry ->
            require(entry.language.matches(Regex("base|[a-z]{2}")))
            require(entry.version.isNotBlank())
            require(PackageSafety.validHash(entry.archiveHash) && PackageSafety.validHash(entry.manifestHash))
            require(entry.bytes in 1..PackageSafety.MAX_ARCHIVE && entry.installedBytes in 1..PackageSafety.MAX_PAYLOAD)
            if (local) require(entry.asset!!.matches(Regex("[a-z]+-[a-f0-9]{64}\\.zip")))
            else PackageSafety.https(entry.url ?: error("Missing download URL"))
            if (entry.language == "base") require(entry.dependencies.isEmpty())
            else {
                require(entry.dependencies.size == 1)
                val d = entry.dependencies.single()
                val base = entries.singleOrNull { it.language == "base" } ?: error("Base package missing")
                require(d.language == "base" && d.version == base.version && d.hash == base.manifestHash) { "Base dependency mismatch" }
            }
        }
        return entries
    }
}

data class Word(val id: String, val category: String, val subcategory: String, val number: Int,
    val position: Int, val text: String, val audio: String, val image: String, val sharedImage: Boolean, val difficulty: String? = null)
data class Lesson(val id: String, val category: String, val subcategory: String, val number: Int, val words: List<Word>)
data class LearningPack(val language: String, val root: File, val base: File, val words: List<Word>, val lessons: List<Lesson>) {
    fun image(word: Word) = PackageSafety.child(if (word.sharedImage) base else root, word.image)
    fun audio(word: Word) = PackageSafety.child(root, word.audio)
    companion object {
        val categoryOrder = listOf("animals", "food", "home", "school", "farm", "space", "fruits_vegetables",
            "heavy_equipment", "countries", "buildings", "flags", "professions", "clocks", "musical_instruments",
            "human_body", "daily_life_communication", "concepts_relationships")
        fun load(root: File, base: File, language: String): LearningPack {
            val manifest = JSONObject(PackageSafety.child(root, "manifest.json").readText())
            val database = PackageSafety.child(root, manifest.getString("databasePath"))
            require(database.length() in 1..16_000_000) { "Database size limit" }
            val json = JSONObject(database.readText())
            require(json.getString("schemaVersion") == "2.3" && json.getString("language") == language)
            val all = json.getJSONArray("words").objects()
            require(all.map { it.getString("conceptId") }.distinct().size == all.size)
            val words = all.filter { it.optBoolean("learningEnabled", true) && it.getString("conceptId") != "food:792" }.map { w ->
                val image = w.getJSONObject("image")
                val packageName = image.optional("package")
                require(packageName == null || packageName == "base")
                Word(w.getString("conceptId"), w.getString("categoryId"), w.getString("subcategoryId"),
                    w.getInt("lessonNumber"), w.getInt("lessonPosition"), w.getString("displayText"),
                    w.getJSONObject("audio").getString("path"), image.optional("overridePath") ?: image.getString("path"), packageName == "base", w.optional("difficulty"))
            }
            require(words.isNotEmpty())
            val alpha = json.optJSONObject("curricula")?.optJSONObject("alphabet")?.optJSONArray("lessonConceptIds")
            val alphaLessons = if (alpha == null) emptyList() else List(alpha.length()) { i -> alpha.getJSONArray(i).strings() }
            val alphabetIds = alphaLessons.flatten().toSet()
            require(alphabetIds.size == alphaLessons.sumOf { it.size }) { "Duplicate alphabet concept" }
            if (alpha != null) require(alphabetIds == words.filter { it.category == "school" && it.subcategory == "alphabet" }.map { it.id }.toSet())
            // Match Swift makeLessons, including stable IDs for the regrouped CAT vocabulary.
            val ordinary = words.filter { it.id !in alphabetIds }.groupBy {
                val legacyCategory = if (it.id.startsWith("CAT-") && it.category in listOf("daily_life_communication", "concepts_relationships")) it.subcategory else it.category
                "$legacyCategory/${it.subcategory}/${it.number}"
            }.map { (id, group) ->
                val first = group.first()
                Lesson(id, first.category, first.subcategory, first.number, group.sortedWith(compareBy({ it.position }, { it.id })))
            }
            val byId = words.associateBy { it.id }
            val alphabet = alphaLessons.mapIndexed { index, ids ->
                val group = ids.map { byId[it] ?: error("Alphabet concept missing: $it") }
                require(group.isNotEmpty() && group.all { it.category == "school" && it.subcategory == "alphabet" })
                Lesson("school/alphabet/${index + 1}", "school", "alphabet", index + 1, group)
            }
            val ordered = (ordinary + alphabet).sortedWith(compareBy({ it.category }, { it.subcategory }, { it.number }))
            val lessons = if (language in listOf("tr", "en")) ordered.groupBy { it.id.substringBeforeLast('/') }
                .toSortedMap().flatMap { (key, source) ->
                    source.flatMap { it.words }.chunked(10).mapIndexed { index, group ->
                        Lesson("$key/${index + 1}", source.first().category, source.first().subcategory, index + 1, group)
                    }
                } else ordered
            require(lessons.map { it.id }.distinct().size == lessons.size)
            return LearningPack(language, root, base, words, lessons).also { pack ->
                words.forEach { require(it.text.isNotBlank() && pack.audio(it).isFile && pack.image(it).isFile) { "Missing word media: ${it.id}" } }
            }
        }
    }
}
