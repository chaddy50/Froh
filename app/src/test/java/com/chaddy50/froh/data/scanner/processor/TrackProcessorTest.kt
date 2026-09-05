package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.scanner.util.UNKNOWN_TITLE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@RunWith(RobolectricTestRunner::class)
class TrackProcessorTest {

    private fun process(
        trackId: Long = 10L,
        trackNumber: Int = 3,
        genreId: Long = 1L,
        genreName: String = "Classical",
        parentGenreId: Long? = null,
        parentGenreName: String? = null,
        artistId: Long = 20L,
        artistName: String = "Beethoven",
        albumId: Long = 30L,
        albumName: String = "Album",
        albumArtworkPath: String? = "/art/30.jpg",
        albumArtistId: Long = 40L,
        albumArtistName: String = "Beethoven",
        performanceId: Long? = 50L,
        year: String = "1808",
        trackTitle: String = "Symphony No. 5",
        discNumber: Int = 1,
        trackDuration: Long = 300000L,
    ) = TrackProcessor().process(
        trackId, trackNumber, genreId, genreName,
        parentGenreId, parentGenreName, artistId, artistName,
        albumId, albumName, albumArtworkPath, albumArtistId,
        albumArtistName, performanceId, year, trackTitle,
        discNumber, trackDuration,
    )

    @Test
    fun returnsTrackWithCorrectFields() {
        val track = process()

        assertEquals(10L, track.id)
        assertEquals("Symphony No. 5", track.title)
        assertEquals(3, track.number)
        assertEquals(30L, track.albumId)
        assertEquals("Album", track.albumName)
        assertEquals(20L, track.artistId)
        assertEquals("Beethoven", track.artistName)
        assertEquals(40L, track.albumArtistId)
        assertEquals("Beethoven", track.albumArtistName)
        assertEquals(1L, track.genreId)
        assertEquals("Classical", track.genreName)
        assertEquals(50L, track.performanceId)
        assertEquals("/art/30.jpg", track.artworkPath)
        assertEquals("1808", track.year)
        assertEquals(1, track.discNumber)
        assertEquals(300000L.toDuration(DurationUnit.MILLISECONDS), track.duration)
    }

    @Test
    fun constructsUriFromTrackId() {
        val track = process(trackId = 42L)

        assertEquals("content://media/external/audio/media/42", track.uri)
    }

    @Test
    fun resolvedTitleIsUsedInsteadOfCursorTitle() {
        // Titles now come from Media3, not the MediaStore row, which for some files is the filename
        val track = process(trackTitle = "Surviving Exile")

        assertEquals("Surviving Exile", track.title)
    }

    @Test
    fun unknownTitleFallbackIsPassedThrough() {
        val track = process(trackTitle = UNKNOWN_TITLE)

        assertEquals("Unknown Title", track.title)
    }

    @Test
    fun missingDiscNumberDefaultsToZero() {
        val track = process(discNumber = 0)

        assertEquals(0, track.discNumber)
    }

    @Test
    fun missingTrackDurationDefaultsToZero() {
        val track = process(trackDuration = 0L)

        assertEquals(0L.toDuration(DurationUnit.MILLISECONDS), track.duration)
    }

    @Test
    fun nullArtworkPathPassedThrough() {
        val track = process(albumArtworkPath = null)

        assertNull(track.artworkPath)
    }

    @Test
    fun setsParentGenreFields() {
        val track = process(parentGenreId = 99L, parentGenreName = "Parent Genre")

        assertEquals(99L, track.parentGenreId)
        assertEquals("Parent Genre", track.parentGenreName)
    }

    @Test
    fun nullPerformanceIdPassedThrough() {
        val track = process(performanceId = null)

        assertNull(track.performanceId)
    }
}
