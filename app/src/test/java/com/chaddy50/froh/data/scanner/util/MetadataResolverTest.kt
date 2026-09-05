package com.chaddy50.froh.data.scanner.util

import com.chaddy50.froh.fakes.FakeMedia3MetadataReader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private fun cursorData(
    trackId: Long = 1L,
    albumId: Long? = 100L,
    trackDuration: Long? = 0L,
) = CursorData(
    trackId = trackId,
    albumId = albumId,
    trackDuration = trackDuration,
    lastModifiedAt = 0L,
)

private fun fullMetadata() = TrackMetadata(
    title = "Surviving Exile",
    artist = "Darren Korb",
    albumArtist = "Pyre",
    album = "Pyre - Original Soundtrack",
    genre = "Video Game",
    year = "2017",
    trackNumber = "06",
    discNumber = "1",
    durationMilliseconds = 205_128L,
)

@RunWith(RobolectricTestRunner::class)
class MetadataResolverResolveTest {

    @Test
    fun degenerateMediaStoreRowTakesAllTagsFromMedia3() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata())
        val resolver = MetadataResolver(reader)

        // duration=0 is the signature of a row the platform extractor failed to populate
        val resolved = resolver.resolve(cursorData(trackDuration = 0L))

        assertEquals("Surviving Exile", resolved.title)
        assertEquals("Darren Korb", resolved.artist)
        assertEquals("Pyre", resolved.albumArtist)
        assertEquals("Pyre - Original Soundtrack", resolved.album)
        assertEquals("Video Game", resolved.genre)
        assertEquals("2017", resolved.year)
        assertEquals(6, resolved.trackNumber)
        assertEquals(1, resolved.discNumber)
        assertEquals(205_128L, resolved.durationMilliseconds)
    }

    @Test
    fun numericGenreCodeFromMedia3IsResolved() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(genre = "(161)"))
        val resolver = MetadataResolver(reader)

        val resolved = resolver.resolve(cursorData())

        assertEquals("Emo", resolved.genre)
    }

    @Test
    fun missingTitleFallsBackToUnknownTitle() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(title = null))
        val resolver = MetadataResolver(reader)

        assertEquals(UNKNOWN_TITLE, resolver.resolve(cursorData()).title)
    }

    @Test
    fun missingArtistFallsBackToUnknownArtist() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(artist = null, albumArtist = null))
        val resolver = MetadataResolver(reader)

        val resolved = resolver.resolve(cursorData())

        assertEquals(UNKNOWN_ARTIST, resolved.artist)
        assertEquals(UNKNOWN_ARTIST, resolved.albumArtist)
    }

    @Test
    fun missingAlbumFallsBackToUnknownAlbum() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(album = null))
        val resolver = MetadataResolver(reader)

        assertEquals(UNKNOWN_ALBUM, resolver.resolve(cursorData()).album)
    }

    @Test
    fun missingGenreFallsBackToUnknownGenre() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(genre = null))
        val resolver = MetadataResolver(reader)

        assertEquals(UNKNOWN_GENRE, resolver.resolve(cursorData()).genre)
    }

    @Test
    fun missingDateFallsBackToUnknownYear() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(year = null))
        val resolver = MetadataResolver(reader)

        assertEquals("Unknown Year", resolver.resolve(cursorData()).year)
    }

    @Test
    fun fullDateIsNormalisedToYear() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(year = "2003-05-06"))
        val resolver = MetadataResolver(reader)

        assertEquals("2003", resolver.resolve(cursorData()).year)
    }

    @Test
    fun trackNumberWithTotalIsParsed() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(trackNumber = "02/14"))
        val resolver = MetadataResolver(reader)

        assertEquals(2, resolver.resolve(cursorData()).trackNumber)
    }

    @Test
    fun nullMedia3ReadFallsBackToMediaStoreDuration() = runTest {
        val reader = FakeMedia3MetadataReader(metadata = null)
        val resolver = MetadataResolver(reader)

        val resolved = resolver.resolve(cursorData(trackDuration = 230_183L))

        assertEquals(230_183L, resolved.durationMilliseconds)
    }

    @Test
    fun nullMedia3ReadYieldsUnknownFieldsWithoutCrashing() = runTest {
        val reader = FakeMedia3MetadataReader(metadata = null)
        val resolver = MetadataResolver(reader)

        val resolved = resolver.resolve(cursorData(trackDuration = null))

        assertEquals(UNKNOWN_TITLE, resolved.title)
        assertEquals(UNKNOWN_ARTIST, resolved.artist)
        assertEquals(UNKNOWN_ARTIST, resolved.albumArtist)
        assertEquals(UNKNOWN_ALBUM, resolved.album)
        assertEquals(UNKNOWN_GENRE, resolved.genre)
        assertEquals("Unknown Year", resolved.year)
        assertEquals(0L, resolved.durationMilliseconds)
    }

    @Test
    fun media3DurationIsPreferredOverMediaStoreDuration() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata().copy(durationMilliseconds = 205_128L))
        val resolver = MetadataResolver(reader)

        val resolved = resolver.resolve(cursorData(trackDuration = 999_999L))

        assertEquals(205_128L, resolved.durationMilliseconds)
    }

    @Test
    fun readIsInvokedOncePerTrack() = runTest {
        val reader = FakeMedia3MetadataReader(fullMetadata())
        val resolver = MetadataResolver(reader)

        resolver.resolve(cursorData())

        assertEquals(1, reader.readCount)
    }
}
