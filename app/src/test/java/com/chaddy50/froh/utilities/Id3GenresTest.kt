package com.chaddy50.froh.utilities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val GENRE_EMO = "Emo"

class ResolveId3GenreNameTest {

    @Test
    fun parenthesisedCodeResolvesToGenreName() {
        assertEquals(GENRE_EMO, resolveId3GenreName("(161)"))
    }

    @Test
    fun parenthesisedCodeWithRefinementResolvesFromTable() {
        assertEquals(GENRE_EMO, resolveId3GenreName("(161)Emo"))
    }

    @Test
    fun outOfRangeCodeWithRefinementReturnsRefinement() {
        assertEquals("Chiptune", resolveId3GenreName("(255)Chiptune"))
    }

    @Test
    fun outOfRangeCodeWithoutRefinementReturnsNull() {
        assertNull(resolveId3GenreName("(255)"))
    }

    @Test
    fun bareNumericCodeResolves() {
        assertEquals(GENRE_EMO, resolveId3GenreName("161"))
    }

    @Test
    fun standardLowCodeResolves() {
        assertEquals("Rock", resolveId3GenreName("(17)"))
    }

    @Test
    fun remixSpecialCodeReturnsRemix() {
        assertEquals("Remix", resolveId3GenreName("(RX)"))
    }

    @Test
    fun coverSpecialCodeReturnsCover() {
        assertEquals("Cover", resolveId3GenreName("(CR)"))
    }

    @Test
    fun plainGenreNameIsReturnedUnchanged() {
        assertEquals("Video Game", resolveId3GenreName("Video Game"))
    }

    @Test
    fun plainGenreNameIsTrimmed() {
        assertEquals("Electronic", resolveId3GenreName("  Electronic  "))
    }

    @Test
    fun nullReturnsNull() {
        assertNull(resolveId3GenreName(null))
    }

    @Test
    fun blankReturnsNull() {
        assertNull(resolveId3GenreName("   "))
    }

    @Test
    fun genreNameStartingWithDigitIsNotTreatedAsCode() {
        assertEquals("8-Bit", resolveId3GenreName("8-Bit"))
    }

    @Test
    fun nonNumericParenthesesReturnedUnchanged() {
        assertEquals("(Live)", resolveId3GenreName("(Live)"))
    }
}
