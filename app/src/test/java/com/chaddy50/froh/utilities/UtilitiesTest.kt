package com.chaddy50.froh.utilities

import org.junit.Assert.assertEquals
import org.junit.Test

class StripArticlesTest {

    @Test
    fun stripsThe() {
        assertEquals("Beatles", stripArticles("The Beatles"))
    }

    @Test
    fun stripsA() {
        assertEquals("Day to Remember", stripArticles("A Day to Remember"))
    }

    @Test
    fun stripsAn() {
        assertEquals("Album", stripArticles("An Album"))
    }

    @Test
    fun caseInsensitive() {
        assertEquals("beatles", stripArticles("the beatles"))
        assertEquals("day", stripArticles("a day"))
        assertEquals("album", stripArticles("an album"))
    }

    @Test
    fun doesNotStripMiddleArticles() {
        assertEquals("Rage Against The Machine", stripArticles("Rage Against The Machine"))
    }

    @Test
    fun returnsNameWithNoArticle() {
        assertEquals("Beethoven", stripArticles("Beethoven"))
    }

    @Test
    fun trimsWhitespace() {
        assertEquals("Name", stripArticles("  Name  "))
    }

    @Test
    fun emptyString() {
        assertEquals("", stripArticles(""))
    }

    @Test
    fun articleAloneReturnsEmpty() {
        assertEquals("", stripArticles("The "))
    }

    @Test
    fun prefixSubstringDoesNotMatch() {
        assertEquals("Therapy", stripArticles("Therapy"))
        assertEquals("Anthem", stripArticles("Anthem"))
    }
}

class ChooseAlbumLabelTest {

    @Test
    fun returnsAlbumsWhenNotClassical() {
        assertEquals("albums", chooseAlbumLabel(false))
    }

    @Test
    fun returnsWorksWhenClassical() {
        assertEquals("works", chooseAlbumLabel(true))
    }
}

class ChooseArtistLabelTest {

    @Test
    fun returnsArtistsWhenNotClassical() {
        assertEquals("artists", chooseArtistLabel(false))
    }

    @Test
    fun returnsComposersWhenClassical() {
        assertEquals("composers", chooseArtistLabel(true))
    }
}

class ChooseAlbumArtworkPathTest {

    @Test
    fun classicalPrefersOwnArtworkOverPortrait() {
        assertEquals("art.jpg", chooseAlbumArtworkPath(true, "art.jpg", "portrait.jpg"))
    }

    @Test
    fun classicalFallsBackToPortraitWhenNoOwnArtwork() {
        assertEquals("portrait.jpg", chooseAlbumArtworkPath(true, null, "portrait.jpg"))
    }

    @Test
    fun classicalReturnsNullWhenNeitherArtworkNorPortraitExists() {
        assertEquals(null, chooseAlbumArtworkPath(true, null, null))
    }

    @Test
    fun nonClassicalNeverFallsBackToPortrait() {
        assertEquals(null, chooseAlbumArtworkPath(false, null, "portrait.jpg"))
    }
}

class FormatMillisecondsTest {

    @Test
    fun zero() {
        assertEquals("0:00", formatMillisecondsIntoMinutesAndSeconds(0))
    }

    @Test
    fun oneSecond() {
        assertEquals("0:01", formatMillisecondsIntoMinutesAndSeconds(1000))
    }

    @Test
    fun oneMinute() {
        assertEquals("1:00", formatMillisecondsIntoMinutesAndSeconds(60000))
    }

    @Test
    fun padsSingleDigitSeconds() {
        assertEquals("1:05", formatMillisecondsIntoMinutesAndSeconds(65000))
    }

    @Test
    fun largeValue() {
        assertEquals("75:30", formatMillisecondsIntoMinutesAndSeconds(4530000))
    }

    @Test
    fun truncatesSubSecondMilliseconds() {
        assertEquals("0:01", formatMillisecondsIntoMinutesAndSeconds(1999))
    }
}
