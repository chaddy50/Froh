package com.chaddy50.froh.services

import android.net.Uri
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakeMusicRepositoryProvider
import com.chaddy50.froh.fakes.FakePerformanceDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.fakes.testTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AutoLibraryCallbackTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())
    private val albumArtistsFlow = MutableStateFlow<List<AlbumArtist>>(emptyList())
    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    private val performancesFlow = MutableStateFlow<List<Performance>>(emptyList())
    private val tracksFlow = MutableStateFlow(listOf(
        testTrack(id = 10, albumId = 3, performanceId = 4)
    ))

    private fun createCallback(
        testScope: TestScope,
        initialGenreMap: Map<Long, Set<Long>> = emptyMap(),
    ): AutoLibraryCallback {
        val provider = FakeMusicRepositoryProvider(
            genreDao = FakeGenreDao(
                topLevelGenresFlow = genresFlow,
                allGenres = genresFlow,
            ),
            trackDao = FakeTrackDao(tracksFlow),
            albumDao = FakeAlbumDao(albumsFlow),
            albumArtistDao = FakeAlbumArtistDao(albumArtistsFlow, initialGenreMap),
            performanceDao = FakePerformanceDao(performancesFlow),
        )
        return AutoLibraryCallback(provider, testScope)
    }

    // region parsePath via getChildrenFor

    @Test
    fun genrePathReturnsAlbumArtistsForGenre() = runTest {
        val callback = createCallback(
            this,
            initialGenreMap = mapOf(1L to setOf(1L), 2L to setOf(2L)),
        )
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
            AlbumArtist(id = 2, name = "Mozart", sortName = "Mozart"),
        )

        val items = callback.getChildrenFor("genre/1")

        assertEquals(1, items.size)
        assertEquals("genre/1/artist/1", items[0].mediaId)
    }

    @Test
    fun genreArtistPathReturnsAlbumsForArtist() = runTest {
        val callback = createCallback(this)
        // No sub-genres → non-classical → getAlbumsForArtistInGenre
        albumsFlow.value = listOf(
            Album(id = 5, title = "Album A", catalogueSortIndex = null, artistId = 2, year = "2020"),
        )

        val items = callback.getChildrenFor("genre/1/artist/2")

        assertEquals(1, items.size)
        assertEquals("genre/1/artist/2/album/5", items[0].mediaId)
    }

    @Test
    fun genreArtistAlbumPathReturnsTracksForNonClassical() = runTest {
        val callback = createCallback(this)
        tracksFlow.value = listOf(
            testTrack(id = 10, albumId = 3),
            testTrack(id = 11, albumId = 3),
        )

        val items = callback.getChildrenFor("genre/1/artist/2/album/3")

        assertEquals(2, items.size)
        assertTrue(items.all { it.mediaId.startsWith("track/") })
    }

    @Test
    fun genreArtistAlbumPerfPathReturnsTracksForPerformance() = runTest {
        val callback = createCallback(this)
        tracksFlow.value = listOf(
            testTrack(id = 20, performanceId = 4),
            testTrack(id = 21, performanceId = 4),
            testTrack(id = 22, performanceId = 99),
        )

        val items = callback.getChildrenFor("genre/1/artist/2/album/3/perf/4")

        assertEquals(2, items.size)
        assertTrue(items.all { it.mediaId.startsWith("track/") })
    }

    @Test
    fun malformedPathReturnsEmptyList() = runTest {
        val callback = createCallback(this)

        assertEquals(emptyList<Any>(), callback.getChildrenFor("not/a/valid/path"))
        assertEquals(emptyList<Any>(), callback.getChildrenFor("artist/1"))
        assertEquals(emptyList<Any>(), callback.getChildrenFor("genre"))
    }

    @Test
    fun emptyStringReturnsEmptyList() = runTest {
        val callback = createCallback(this)

        assertEquals(emptyList<Any>(), callback.getChildrenFor(""))
    }

    @Test
    fun nonNumericIdsReturnEmptyList() = runTest {
        val callback = createCallback(this)

        assertEquals(emptyList<Any>(), callback.getChildrenFor("genre/abc"))
        assertEquals(emptyList<Any>(), callback.getChildrenFor("genre/1/artist/xyz"))
        assertEquals(emptyList<Any>(), callback.getChildrenFor("genre/1/artist/2/album/nope"))
    }

    // endregion

    // region composer picture fallback

    @Test
    fun classicalAlbumWithNoArtworkFallsBackToComposerPortrait() = runTest {
        val callback = createCallback(this)
        // A sub-genre makes genre 1 classical.
        genresFlow.value = listOf(Genre(id = 2, name = "Orchestral", parentGenreId = 1))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 2, name = "Bach", sortName = "Bach", portraitPath = "/portraits/bach.jpg"),
        )
        albumsFlow.value = listOf(
            Album(id = 5, title = "Goldberg Variations", catalogueSortIndex = null, artistId = 2, year = "1741"),
        )

        val items = callback.getChildrenFor("genre/1/artist/2")

        assertEquals(
            Uri.parse("content://com.chaddy50.froh.artwork/portraits/bach.jpg"),
            items[0].mediaMetadata.artworkUri,
        )
    }

    @Test
    fun classicalAlbumWithOwnArtworkKeepsItOverComposerPortrait() = runTest {
        val callback = createCallback(this)
        genresFlow.value = listOf(Genre(id = 2, name = "Orchestral", parentGenreId = 1))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 2, name = "Bach", sortName = "Bach", portraitPath = "/portraits/bach.jpg"),
        )
        albumsFlow.value = listOf(
            Album(id = 5, title = "Goldberg Variations", catalogueSortIndex = null, artistId = 2, year = "1741", artworkPath = "/art/goldberg.jpg"),
        )

        val items = callback.getChildrenFor("genre/1/artist/2")

        assertEquals(
            Uri.parse("content://com.chaddy50.froh.artwork/art/goldberg.jpg"),
            items[0].mediaMetadata.artworkUri,
        )
    }

    @Test
    fun nonClassicalAlbumWithNoArtworkDoesNotFallBackToPortrait() = runTest {
        val callback = createCallback(this)
        // No sub-genres → non-classical
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 2, name = "Pink Floyd", sortName = "Pink Floyd", portraitPath = "/portraits/pf.jpg"),
        )
        albumsFlow.value = listOf(
            Album(id = 5, title = "The Wall", catalogueSortIndex = null, artistId = 2, year = "1979"),
        )

        val items = callback.getChildrenFor("genre/1/artist/2")

        assertNull(items[0].mediaMetadata.artworkUri)
    }

    @Test
    fun classicalPerformanceTrackWithNoArtworkFallsBackToComposerPortrait() = runTest {
        val callback = createCallback(this)
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 2, name = "Bach", sortName = "Bach", portraitPath = "/portraits/bach.jpg"),
        )
        tracksFlow.value = listOf(
            testTrack(id = 20, albumArtistId = 2, parentGenreId = 1, performanceId = 4, artworkPath = null),
        )

        val items = callback.getChildrenFor("genre/1/artist/2/album/3/perf/4")

        assertEquals(
            Uri.parse("content://com.chaddy50.froh.artwork/portraits/bach.jpg"),
            items[0].mediaMetadata.artworkUri,
        )
    }

    // endregion
}
