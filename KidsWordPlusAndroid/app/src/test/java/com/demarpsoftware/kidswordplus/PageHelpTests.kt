package com.demarpsoftware.kidswordplus

import org.junit.Assert.*
import org.junit.Test

class PageHelpTests {
    @Test fun everyGuideHasLocalizedTitlesStepsAndADemoContract() {
        assertEquals(29,PageHelpCatalog.topics.size)
        for(topic in PageHelpCatalog.topics) {
            val tr=PageHelpCatalog.guide(topic,true)
            val en=PageHelpCatalog.guide(topic,false)
            assertTrue(tr.title.isNotBlank());assertTrue(en.title.isNotBlank())
            assertTrue(tr.steps.size in 2..4);assertEquals(tr.steps.size,en.steps.size)
            assertEquals(tr.demo,en.demo)
            for(step in en.steps) {
                assertTrue(step.title.isNotBlank() && step.body.isNotBlank())
                assertFalse("Untranslated help for $topic",Regex("[ıİşŞğĞ]").containsMatchIn(step.title+step.body))
            }
        }
        assertEquals(HelpDemo.SOUND,PageHelpCatalog.guide("word",true).demo)
        assertEquals(HelpDemo.CARDS,PageHelpCatalog.guide("memory",false).demo)
        assertEquals(HelpDemo.BOARD,PageHelpCatalog.guide("computer",true).demo)
    }
    @Test fun guidesDescribeTheAvailableAndroidFeatures() {
        for(tr in listOf(true,false)) {
            val games=PageHelpCatalog.guide("games",tr).steps.joinToString {it.body}
            assertFalse(games.contains("Konuşma"));assertFalse(games.contains("speaking",true))
            val language=PageHelpCatalog.guide("languageSettings",tr).steps.joinToString {it.body}
            assertFalse(language.contains("telefonun",true));assertFalse(language.contains("device language",true))
            val computer=PageHelpCatalog.guide("computer",tr).steps.joinToString {it.body}
            assertFalse(computer.contains("Sağ üst"));assertFalse(computer.contains("top right"))
        }
    }
    @Test(expected=IllegalStateException::class) fun unknownTopicsAreRejected() {PageHelpCatalog.guide("unknown",true)}
}
