package com.demarpsoftware.kidswordplus

import org.junit.Assert.*
import org.junit.Test

class FeedbackTests {
    @Test fun messageBoundsIgnoreLeadingAndTrailingWhitespace() {
        assertFalse(FeedbackValidation.validMessage("short"))
        assertTrue(FeedbackValidation.validMessage("  A useful suggestion  "))
        assertTrue(FeedbackValidation.validMessage("x".repeat(3000)))
        assertFalse(FeedbackValidation.validMessage("x".repeat(3001)))
        assertFalse(FeedbackValidation.validMessage(" ".repeat(20)))
    }
    @Test fun replyAddressIsOptionalButCannotContainExtraHeaders() {
        assertTrue(FeedbackValidation.validEmail(""))
        assertTrue(FeedbackValidation.validEmail("parent@example.com"))
        assertFalse(FeedbackValidation.validEmail("not an address"))
        assertFalse(FeedbackValidation.validEmail("parent@example.com\r\nBcc: other@example.com"))
    }
    @Test fun parentCanReportPraiseAndSuggestionsAsWellAsProblems() {
        assertEquals(setOf("problem","suggestion","appreciation"),feedbackKinds.map{it.id}.toSet())
        assertEquals(setOf("general","home","learn","games","chess","profile","settings","packages"),feedbackScreens.map{it.id}.toSet())
    }
}
