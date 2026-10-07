package com.chaddy50.froh.ui.screens.tracksScreen

import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.PerformanceRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeAudioDbRepository
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakePerformanceDao
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.fakes.testTrack
import com.chaddy50.froh.navigation.TracksRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TracksScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())
    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    private val albumArtistsFlow = MutableStateFlow<List<AlbumArtist>>(emptyList())
    private val performancesFlow = MutableStateFlow<List<Performance>>(emptyList())

    private fun createViewModel(
        genreId: Long = 1L,
        albumId: Long = 1L,
        performanceId: Long = -1L,
        classicalGenreId: Long? = null,
    ): TracksScreenViewModel {
        val genreDao = FakeGenreDao()
        val albumArtistDao = FakeAlbumArtistDao(albumArtistsFlow)
        val albumDao = FakeAlbumDao(albumsFlow)
        val trackDao = FakeTrackDao(tracksFlow)
        val performanceDao = FakePerformanceDao(performancesFlow)
        val audioDbRepository = FakeAudioDbRepository()

        val config = ClassicalGenreConfig().apply { this.classicalGenreId = classicalGenreId }
        return TracksScreenViewModel(
            TracksRoute(genreId = genreId, albumId = albumId, performanceId = performanceId, title = "Test"),
            config,
            TrackRepository(trackDao),
            AlbumRepository(albumDao),
            AlbumArtistRepository(albumArtistDao, audioDbRepository),
            PerformanceRepository(performanceDao),
            PlaylistRepository(FakePlaylistDao()),
        )
    }

    // --- uiState ---

    @Test
    fun initialStateIsLoading() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun emitsTracksForAlbum() = runTest {
        val vm = createViewModel(albumId = 1L)
        backgroundScope.launch { vm.uiState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "The Wall", catalogueSortIndex = null, artistId = 1, year = "1979"),
        )
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Pink Floyd", sortName = "Pink Floyd"),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, title = "Comfortably Numb"),
            testTrack(id = 2, albumId = 1, title = "Another Brick in the Wall"),
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.tracks.size)
        assertEquals("The Wall", state.screenTitle)
        assertEquals("Pink Floyd", state.albumArtist?.name)
    }

    @Test
    fun emitsTracksForPerformance() = runTest {
        val vm = createViewModel(albumId = 1L, performanceId = 5L)
        backgroundScope.launch { vm.uiState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "Goldberg Variations", catalogueSortIndex = 988, artistId = 1, year = "1741"),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, performanceId = 5L, title = "Variation 1"),
            testTrack(id = 2, albumId = 1, performanceId = 5L, title = "Variation 2"),
            testTrack(id = 3, albumId = 1, performanceId = null, title = "Other track"),
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.tracks.size)
    }

    @Test
    fun performanceIdNegativeOneSentinelTreatedAsNull() = runTest {
        // -1 is the default sentinel meaning "no performance" — should fetch album tracks
        val vm = createViewModel(albumId = 1L, performanceId = -1L)
        backgroundScope.launch { vm.uiState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "Album", catalogueSortIndex = null, artistId = 1, year = "2020"),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, performanceId = null, title = "Track A"),
            testTrack(id = 2, albumId = 1, performanceId = 5L, title = "Track B"),
        )
        advanceUntilIdle()

        // Both tracks returned because we're fetching by album, not by performance
        assertEquals(2, vm.uiState.value.tracks.size)
    }

    @Test
    fun screenTitleFallsBackToTracksWhenAlbumNull() = runTest {
        val vm = createViewModel(albumId = 99L)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()

        assertEquals("Tracks", vm.uiState.value.screenTitle)
        assertNull(vm.uiState.value.albumArtist)
    }

    // --- entityHeaderState (album view, no performance) ---

    @Test
    fun entityHeaderShowsAlbumDetailsNonClassical() = runTest {
        val vm = createViewModel(genreId = 1L, albumId = 1L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "The Wall", catalogueSortIndex = null, artistId = 1, year = "1979", artworkPath = "/art/wall.jpg"),
        )
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Pink Floyd", sortName = "Pink Floyd"),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, durationMs = 180000),
            testTrack(id = 2, albumId = 1, durationMs = 120000),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("The Wall", header.title)
        assertEquals("Pink Floyd", header.subtitle)
        assertEquals("1979 - 2 tracks - 5:00", header.details)
        assertEquals("/art/wall.jpg", header.artworkPath)
    }

    @Test
    fun entityHeaderShowsClassicalAlbumDetails() = runTest {
        val vm = createViewModel(genreId = 10L, albumId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "Goldberg Variations", catalogueSortIndex = 988, artistId = 1, year = "1741", artworkPath = "/art/goldberg.jpg"),
        )
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        performancesFlow.value = listOf(
            Performance(id = 1, albumId = 1, albumName = "Goldberg Variations", artistId = 2, artistName = "Glenn Gould", year = "1981", genreId = 10),
            Performance(id = 2, albumId = 1, albumName = "Goldberg Variations", artistId = 3, artistName = "Andras Schiff", year = "2001", genreId = 10),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, durationMs = 180000),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("Goldberg Variations", header.title)
        assertEquals("Bach", header.subtitle)
        assertEquals("2 performances", header.details)
        assertNull(header.artworkPath) // classical hides artwork
    }

    @Test
    fun entityHeaderFallsBackWhenAlbumNotFound() = runTest {
        val vm = createViewModel(albumId = 99L)
        backgroundScope.launch { vm.entityHeaderState.collect() }
        advanceUntilIdle()

        assertFalse(vm.entityHeaderState.value.isLoading)
    }

    // --- entityHeaderState (performance view) ---

    @Test
    fun entityHeaderShowsPerformanceDetails() = runTest {
        val vm = createViewModel(genreId = 10L, albumId = 1L, performanceId = 5L, classicalGenreId = 10L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "Goldberg Variations", catalogueSortIndex = 988, artistId = 1, year = "1741"),
        )
        performancesFlow.value = listOf(
            Performance(id = 5, albumId = 1, albumName = "Goldberg Variations", artistId = 2, artistName = "Glenn Gould", year = "1981", genreId = 10),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, performanceId = 5L, durationMs = 180000),
            testTrack(id = 2, albumId = 1, performanceId = 5L, durationMs = 120000),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("Goldberg Variations", header.title)
        assertEquals("1981 - Glenn Gould", header.subtitle)
        assertEquals("2 movements - 5:00", header.details)
    }

    @Test
    fun entityHeaderUsesTracksLabelForNonClassicalPerformance() = runTest {
        val vm = createViewModel(genreId = 1L, albumId = 1L, performanceId = 5L, classicalGenreId = 10L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "Album", catalogueSortIndex = null, artistId = 1, year = "2020"),
        )
        performancesFlow.value = listOf(
            Performance(id = 5, albumId = 1, albumName = "Album", artistId = 2, artistName = "Artist", year = "2020", genreId = 1),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, performanceId = 5L, durationMs = 200000),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("1 tracks - 3:20", header.details)
    }

    // --- isClassical ---

    @Test
    fun uiStateIsClassicalWhenGenreMatchesClassicalGenreId() = runTest {
        val vm = createViewModel(genreId = 10L, albumId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.uiState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "Goldberg Variations", catalogueSortIndex = 988, artistId = 1, year = "1741"),
        )
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, title = "I. Aria"),
        )
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isClassical)
    }

    @Test
    fun uiStateIsNotClassicalForNonClassicalGenre() = runTest {
        val vm = createViewModel(genreId = 1L, albumId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.uiState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "The Wall", catalogueSortIndex = null, artistId = 1, year = "1979"),
        )
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Pink Floyd", sortName = "Pink Floyd"),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, title = "Comfortably Numb"),
        )
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isClassical)
    }

    @Test
    fun uiStateIsNotClassicalWhenClassicalGenreIdIsNull() = runTest {
        val vm = createViewModel(genreId = 1L, albumId = 1L, classicalGenreId = null)
        backgroundScope.launch { vm.uiState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "Album", catalogueSortIndex = null, artistId = 1, year = "2020"),
        )
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Artist", sortName = "Artist"),
        )
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 1, title = "Track A"),
        )
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isClassical)
    }
}
