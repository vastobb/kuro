package dev.kuro.app

import dev.kuro.app.util.greetingForHour
import org.junit.Assert.assertEquals
import org.junit.Test

class KuroFoundationTest {
    @Test
    fun greetingBuckets_matchSpec() {
        assertEquals("Good morning", greetingForHour(8))
        assertEquals("Good afternoon", greetingForHour(14))
        assertEquals("Good evening", greetingForHour(21))
    }
}
