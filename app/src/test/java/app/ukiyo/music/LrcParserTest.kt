package app.ukiyo.music

import app.ukiyo.music.ui.currentLrcIndex
import app.ukiyo.music.ui.parseLrc
import org.junit.Assert.assertEquals
import org.junit.Test

class LrcParserTest {
    @Test
    fun parsesTimestampsAndSorts() {
        val lines = parseLrc("[00:10.50]second\n[00:01.00]first")
        assertEquals(2, lines.size)
        assertEquals("first", lines[0].text)
        assertEquals(1000L, lines[0].timeMs)
        assertEquals("second", lines[1].text)
        assertEquals(10500L, lines[1].timeMs)
    }

    @Test
    fun findsCurrentLine() {
        val lines = parseLrc("[00:01.00]one\n[00:05.00]two")
        assertEquals(0, currentLrcIndex(lines, 1500L))
        assertEquals(1, currentLrcIndex(lines, 5000L))
        assertEquals(-1, currentLrcIndex(lines, 500L))
    }
}
