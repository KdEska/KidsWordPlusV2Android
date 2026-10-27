package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.content.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class LanguageSupportTests {
    @Test fun openingLessonCopyLoadsAllThirteenInterfacesWithoutVoicePackDependency() {
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val json=File(root,"KidsWordPlusAndroid/app/src/main/assets/chess/reference-content.json").readText()
        val english=openingTexts(json,"en")
        assertEquals(27,english.size)
        for(language in InterfaceStrings.codes) {
            val copy=openingTexts(json,language)
            assertEquals(language,english.keys,copy.keys)
            assertTrue(copy.values.all{it.isNotBlank()})
            if(language!="en") for(key in english.keys.filter{it.endsWith(".body")}) {
                assertNotEquals("$language $key",english[key],copy[key])
            }
        }
    }

    @Test fun recentPortalAndChessSentencesCoverAllThirteenLanguages() {
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val manifest=JSONObject(File(root,"planning/portal-refresh/localization-keys.json").readText())
        val dictionaries=JSONObject(File(root,"KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        assertEquals(13,InterfaceStrings.codes.size)
        val keys=manifest.getJSONArray("keys")
        val placeholder=Regex("\\{[0-9]+\\}")
        for(language in InterfaceStrings.codes.filter {it !in setOf("tr","en")}) {
            val dictionary=dictionaries.getJSONObject(language)
            repeat(keys.length()) { index->
                val key=keys.getString(index)
                val value=dictionary.getString(key)
                assertTrue("$language $key",value.isNotBlank())
                assertEquals(placeholder.findAll(key).map{it.value}.sorted().toList(),placeholder.findAll(value).map{it.value}.sorted().toList())
            }
            val stage=renderInterfaceTemplate(dictionary.getString("Stage {0} / 10 · Task {1} / 10"),listOf("2","5"))
            assertTrue(stage.contains("2")&&stage.contains("5"));assertFalse(stage.contains("{0}")||stage.contains("{1}"))
        }
    }
    @Test fun notificationSettingsAndReminderContentCoverEveryInterfaceLanguage() {
        val root = File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val dictionaries = JSONObject(File(root, "KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        val keys = listOf("Notifications", "Learning reminder", "Learning reminders", "Add reminder", "Delete reminder", "Reminder {n}",
            "Open Android Settings", "Android may show reminders later than the selected time to save battery.",
            "Ready to learn some words?", "Your companion is waiting. Let's discover new words together!",
            "Allow notifications in device Settings to receive reminders.")
        for (language in InterfaceStrings.codes.filter { it !in setOf("tr", "en") }) {
            val dictionary = dictionaries.getJSONObject(language)
            keys.forEach { key ->
                assertTrue("$language $key", dictionary.has(key))
                assertTrue("$language $key", dictionary.getString(key).isNotBlank())
                // The French word “Notifications” legitimately matches English.
                if (!(language == "fr" && key == "Notifications")) assertNotEquals("$language $key", key, dictionary.getString(key))
            }
            assertTrue(dictionary.getString("Reminder {n}").contains("{n}"))
        }
    }

    @Test fun chessGuideFollowsExplicitInterfaceLanguageAndKeepsSquarePlaceholders() {
        val root = File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val chess = JSONObject(File(root, "KidsWordPlusV2/Chess/interface.json").readText())
        val dictionaries = JSONObject(File(root, "KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        val english = chess.getJSONObject("en")
        val keys = english.keys().asSequence().filter { it.startsWith("guide.") }.toList()
        assertEquals(44, keys.size)
        val previous = InterfaceStrings.language
        InterfaceStrings.loadJson(dictionaries.toString())
        try {
            // A different global/content locale must not override the requested display locale.
            InterfaceStrings.language = "tr"
            for (language in InterfaceStrings.codes) {
                for (key in keys) {
                    val expected = chess.getJSONObject(language).getString(key)
                    assertEquals("$language $key", expected,
                        InterfaceStrings.text(language, chess.getJSONObject("tr").getString(key), english.getString(key)))
                }
                val square = InterfaceStrings.text(language, "Hedef kare {square}", "Target square {square}")
                assertTrue(square.contains("{square}"))
                assertTrue(renderNamedInterfaceTemplate(square, mapOf("square" to "e3")).contains("e3"))
            }
        } finally { InterfaceStrings.language = previous }
    }

    @Test fun allThirteenRealLearningPacksHavePlayableMediaAndUniqueLessons() {
        val source=File(requireNotNull(System.getProperty("kwp.packages")))
        assertEquals(13,LearningLanguages.codes.size)
        for(code in LearningLanguages.codes) {
            val pack=LearningPack.load(File(source,"packages/$code"),File(source,"packages/base"),code)
            assertTrue("$code vocabulary",pack.words.size>=1000)
            assertEquals(code,pack.language)
            assertEquals(pack.words.size,pack.lessons.sumOf{it.words.size})
            assertEquals(pack.words.size,pack.lessons.flatMap{it.words}.map{it.id}.toSet().size)
            assertEquals(pack.lessons.size,pack.lessons.map{it.id}.toSet().size)
        }
    }
    @Test fun catalogAndInterfaceTranslationsCoverSupportedLanguages() {
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val catalog=Catalog.parse(File(root,"KidsWordPlusAndroid/app/src/debug/assets/packages/catalog.json").readText(),true,true)
        assertEquals(LearningLanguages.codes+"base",catalog.map{it.language}.toSet())
        val translations=JSONObject(File(root,"KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        for(code in InterfaceStrings.codes.filter{it !in setOf("tr","en")}) {
            assertTrue("$code interface",translations.getJSONObject(code).length()>200)
            assertNotEquals("Settings",translations.getJSONObject(code).getString("Settings"))
        }
        assertEquals("Fallback",InterfaceStrings.text("unknown","Yedek","Fallback"))
        assertEquals("Türkçe",InterfaceStrings.text("tr","Türkçe","Turkish"))
    }
    @Test fun translatedTemplatesRenderOnceAndPreserveLiteralNames() {
        assertEquals("Hallo, Ada {1} $!",renderInterfaceTemplate("Hallo, {0}!",listOf("Ada {1} $")))
        assertEquals("7 pairs in 12 attempts",renderInterfaceTemplate("{1} pairs in {0} attempts",listOf("12","7")))
        assertEquals("{9}",renderInterfaceTemplate("{9}",emptyList()))
        assertEquals("Hi Ada",renderInterfaceTemplate("Hi {0}",listOf("Ada")))
    }
    @Test fun chessMenusAndDecoratedLabelsUseExistingTranslations() {
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val dictionaries=JSONObject(File(root,"KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        for(code in InterfaceStrings.codes.filter{it !in setOf("tr","en")}) {
            val dictionary=dictionaries.getJSONObject(code)
            assertNotEquals("Chess World",resolveInterfaceText(dictionary,"Chess World"))
            assertNotEquals("Match settings",resolveInterfaceText(dictionary,"Match settings"))
            assertNotEquals("Narrate moves",resolveInterfaceText(dictionary,"Narrate moves"))
            assertNotEquals("White to move",resolveInterfaceText(dictionary,"White to move"))
        }
        val dictionary=JSONObject().put("Installed","Installiert").put("Name","Name-DE")
        assertEquals("✓ Installiert",resolveInterfaceText(dictionary,"✓ Installed"))
        assertEquals("Name-DE: ",resolveInterfaceText(dictionary,"Name: "))
        assertEquals("Unknown: ",resolveInterfaceText(dictionary,"Unknown: "))
    }
    @Test fun gameResultsAndParentMessagesCoverEveryAdditionalInterfaceLanguage() {
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val dictionaries=JSONObject(File(root,"KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        for(code in InterfaceStrings.codes.filter{it !in setOf("tr","en")}) {
            val dictionary=dictionaries.getJSONObject(code)
            for(key in listOf("Time to Review","Audio could not play. Try again.","Listen to the sound first.","Lock parent area","Data on this device","Interface and learning language","Recover chess data"))
                assertNotEquals("$code $key",key,resolveInterfaceText(dictionary,key))
            val result=renderInterfaceTemplate(resolveInterfaceText(dictionary,"{0} pairs in {1} attempts"),listOf("4","6"))
            assertTrue(result.contains("4") && result.contains("6"))
            assertFalse(result.contains("{0}") || result.contains("{1}"))
            assertNotEquals("4 pairs in 6 attempts",result)
        }
    }
    @Test fun setupAndReportPackageMessagesHaveTranslations() {
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val dictionaries=JSONObject(File(root,"KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        for(code in InterfaceStrings.codes.filter{it !in setOf("tr","en")}) {
            val dictionary=dictionaries.getJSONObject(code)
            for(key in listOf("The Base language pack with shared images installs with your first language. Then you can learn offline.","Install a language pack to view its report. Previous progress is kept.","✓ Your language pack is ready","Learning language: "))
                assertNotEquals("$code $key",key,resolveInterfaceText(dictionary,key))
        }
    }
    @Test fun namedBoardInstructionsPreserveValuesAndLocalizeValidationMessages() {
        assertEquals("Set {square} on a1",renderNamedInterfaceTemplate("Set {piece} on {square}",mapOf("piece" to "{square}","square" to "a1")))
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val dictionaries=JSONObject(File(root,"KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText())
        for(code in InterfaceStrings.codes.filter{it !in setOf("tr","en")}) {
            val dictionary=dictionaries.getJSONObject(code)
            val key="Place the {piece} on {square}."
            val instruction=renderNamedInterfaceTemplate(resolveInterfaceText(dictionary,key),mapOf("piece" to resolveInterfaceText(dictionary,"Rook"),"square" to "a1"))
            assertTrue(instruction.contains("a1"));assertFalse(instruction.contains("{piece}"));assertNotEquals("Place the Rook on a1.",instruction)
            assertNotEquals("Kings cannot be adjacent.",resolveInterfaceText(dictionary,"Kings cannot be adjacent."))
        }
    }
    @Test fun displayedPieceNamesFollowTheSelectedInterface() {
        val root=File(requireNotNull(System.getProperty("kwp.packages"))).parentFile
        val json=File(root,"KidsWordPlusAndroid/app/src/main/assets/localization/interface.json").readText()
        val dictionaries=JSONObject(json)
        val previous=InterfaceStrings.language
        InterfaceStrings.loadJson(json)
        try {
            for(code in InterfaceStrings.codes.filter{it !in setOf("tr","en")}) {
                InterfaceStrings.language=code
                for(kind in com.demarpsoftware.kidswordplus.chess.PieceKind.entries) {
                    val english=kind.name.lowercase().replaceFirstChar{it.uppercase()}
                    assertEquals(dictionaries.getJSONObject(code).getString(english),pieceTitle(kind,false))
                }
            }
        } finally {InterfaceStrings.language=previous;InterfaceStrings.loadJson("{}") }
    }
}
