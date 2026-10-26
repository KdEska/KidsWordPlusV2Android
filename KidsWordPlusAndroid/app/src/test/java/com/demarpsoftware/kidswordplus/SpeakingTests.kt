package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.speech.SpeakingRules
import com.demarpsoftware.kidswordplus.games.GameProgress
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class SpeakingTests {
    @Test fun recognizesWholeWordsWithoutRemovingImportantLetters() {
        val examples=listOf(
            Triple("tr"," İNEK! ","inek"), Triple("en"," CAT! ","cat"), Triple("de"," KATZE! ","Katze"),
            Triple("fr"," ÉLÉPHANT! ","éléphant"),Triple("es","ÁGUILA!","Águila"),Triple("pt","ÁGUIA!","Águia"),
            Triple("zh"," 毛 毛 虫！","毛毛虫"),Triple("ja"," ね こ！","ネコ"),
            Triple("ko"," 고양이! ","고양이"),Triple("it"," GATTO! ","gatto"),
            Triple("hi","क़िला","क़िला"),Triple("ar","قِطَّةٌ","قطة"),Triple("ru"," КОШКА! ","Кошка"))
        assertEquals(examples.size,SpeakingRules.locales.size)
        examples.forEach {(language,heard,expected)->assertTrue(language,SpeakingRules.matches(heard,expected,language))}
        listOf(Triple("tr","sık","şık"),Triple("tr","I","i"),Triple("ja","かき","かぎ"),
            Triple("hi","कल","काल"),Triple("ar","سال","سأل"),Triple("ko","달","딸"),
            Triple("en","a cat","cat"),Triple("zh","这是猫","猫"),Triple("ru","Это кошка","Кошка")).forEach {
                assertFalse(it.toString(),SpeakingRules.matches(it.second,it.third,it.first))
            }
        assertFalse(SpeakingRules.matches("","", "en"))
        assertNull(SpeakingRules.locales["xx"])
    }
    @Test fun canonicalHangulAndAccentsRemainComparable() {
        assertTrue(SpeakingRules.matches(java.text.Normalizer.normalize("고양이",java.text.Normalizer.Form.NFD),"고양이","ko"))
        assertFalse(SpeakingRules.matches("pesca","pèsca","it"))
        assertFalse(SpeakingRules.matches("aguila","águila","es"))
    }
    @Test fun speakingRoundCreditsOnlyOnceAndKeepsLanguagesSeparate() {
        val day=LocalDate.of(2026,10,22)
        val initial=GameProgress()
        val once=initial.speakingRound("tr","round-a",setOf("animals:9"),day)
        assertEquals(setOf("animals:9"),once.activity("tr",day).reviews)
        assertEquals(1,once.activity("tr",day).sessions.size)
        assertEquals(once,once.speakingRound("tr","round-a",setOf("animals:20"),day))
        val twice=once.speakingRound("tr","round-b",emptySet(),day)
        assertEquals(2,twice.activity("tr",day).sessions.size)
        assertTrue(twice.activity("en",day).sessions.isEmpty())
        assertEquals(twice,GameProgress.decode(twice.encode()))
    }
    @Test fun microphoneGrantContinuesTheSameExplicitWordRequest() {
        val request=com.demarpsoftware.kidswordplus.speech.SpeakingStartRequest("animals:9",true)
        assertFalse(request.canStart("animals:9",true,true,false))
        val granted=request.permissionResult(true)
        assertTrue(granted.canStart("animals:9",true,true,false))
        assertFalse(granted.canStart("animals:20",true,true,false))
        assertFalse(granted.canStart("animals:9",false,true,false))
        assertFalse(granted.canStart("animals:9",true,false,false))
        assertFalse(granted.canStart("animals:9",true,true,true))
        assertFalse(request.permissionResult(false).canStart("animals:9",true,true,false))
        assertFalse(com.demarpsoftware.kidswordplus.speech.SpeakingStartRequest().permissionResult(true).canStart("animals:9",true,true,false))
    }
    @Test fun unsupportedSupportQueriesDoNotBlockTheLocalEngine() {
        val policy=com.demarpsoftware.kidswordplus.speech.SpeakingSupportPolicy
        assertTrue(policy.queryUnavailable(14))
        assertFalse(policy.queryUnavailable(13))
        assertTrue(policy.tryLocal(listOf("tr_TR"),emptyList(),"tr-TR"))
        assertTrue(policy.tryLocal(emptyList(),emptyList(),"tr-TR"))
        assertFalse(policy.tryLocal(listOf("en-US"),listOf("tr-TR"),"tr-TR"))
        assertFalse(policy.tryLocal(emptyList(),listOf("tr-TR"),"tr-TR"))
    }
    @Test fun installedEnglishRegionIsUsedWithoutRequiringAnotherDownload() {
        val policy=com.demarpsoftware.kidswordplus.speech.SpeakingSupportPolicy
        assertEquals("en-GB",policy.installedLocale(listOf("fr-FR","en-GB"),"en-US"))
        assertEquals("en-US",policy.installedLocale(listOf("en-GB","en-US"),"en-US"))
        assertEquals("en-GB",policy.installedLocale(listOf("en-AU","en-GB"),"en-US"))
        assertEquals("en_GB".replace('_','-'),policy.installedLocale(listOf("en_GB"),"en-US"))
        assertNull(policy.installedLocale(listOf("en-GB"),"tr-TR"))
        assertNull(policy.installedLocale(listOf("zh-TW"),"zh-CN"))
    }
    @Test fun modelAvailabilitySeparatesDownloadSchedulingFromActualReadiness() {
        val policy=com.demarpsoftware.kidswordplus.speech.SpeakingSupportPolicy
        fun plan(installed:List<String>,downloadable:List<String>,pending:List<String>)=policy.plan(installed,downloadable,pending,"en-US")
        assertEquals(com.demarpsoftware.kidswordplus.speech.SpeakingModelAvailability.READY,plan(listOf("en-GB"),listOf("en-US"),emptyList()).availability)
        assertEquals("en-GB",plan(listOf("en-GB"),listOf("en-US"),emptyList()).locale)
        assertEquals(com.demarpsoftware.kidswordplus.speech.SpeakingModelAvailability.DOWNLOADABLE,plan(emptyList(),listOf("en-GB"),emptyList()).availability)
        assertEquals("en-GB",plan(emptyList(),listOf("en-GB"),emptyList()).locale)
        assertEquals(com.demarpsoftware.kidswordplus.speech.SpeakingModelAvailability.PENDING,plan(emptyList(),listOf("en-US"),listOf("en-US")).availability)
        assertEquals(com.demarpsoftware.kidswordplus.speech.SpeakingModelAvailability.UNSUPPORTED,plan(listOf("fr-FR"),listOf("tr-TR"),emptyList()).availability)
        assertEquals(com.demarpsoftware.kidswordplus.speech.SpeakingModelAvailability.UNKNOWN,plan(emptyList(),emptyList(),emptyList()).availability)
    }
}
