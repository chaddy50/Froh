package com.chaddy50.froh.ui.screens.performancesScreen

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
import com.chaddy50.froh.fakes.FakeDeezerRepository
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakePerformanceDao
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.fakes.testTrack
import com.chaddy50.froh.navigation.PerformancesRoute
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
class PerformancesScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    private val albumArtistsFlow = MutableStateFlow<List<AlbumArtist>>(emptyList())
    private val performancesFlow = MutableStateFlow<List<Performance>>(emptyList())
    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())

    private fun createViewModel(
        genreId: Long = 10L,
        albumId: Long = 1L,
        classicalGenreId: Long? = 10L,
    ): PerformancesScreenViewModel {
        val genreDao = FakeGenreDao()
        val albumArtistDao = FakeAlbumArtistDao(albumArtistsFlow)
        val albumDao = FakeAlbumDao(albumsFlow)
        val trackDao = FakeTrackDao(tracksFlow)
        val performanceDao = FakePerformanceDao(performancesFlow)
        val deezerRepository = FakeDeezerRepository()

        val config = ClassicalGenreConfig().apply { this.classicalGenreId = classicalGenreId }
        return PerformancesScreenViewModel(
            PerformancesRoute(genreId = genreId, albumId = albumId, title = "Test"),
            config,
            PerformanceRepository(performanceDao),
            AlbumRepository(albumDao),
            AlbumArtistRepository(albumArtistDao, deezerRepository),
            TrackRepository(trackDao),
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
    fun emitsPerformancesForAlbum() = runTest {
        val vm = createViewModel(albumId = 1L)
        backgroundScope.launch { vm.uiState.collect() }

        albumsFlow.value = listOf(
            Album(id = 1, title = "Goldberg Variations", catalogueSortIndex = 988, artistId = 1, year = "1741"),
        )
        performancesFlow.value = listOf(
            Performance(id = 1, albumId = 1, albumName = "Goldberg Variations", artistId = 2, artistName = "Glenn Gould", year = "1981", genreId = 10),
            Performance(id = 2, albumId = 1, albumName = "Goldberg Variations", artistId = 3, artistName = "Andras Schiff", year = "2001", genreId = 10),
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.performances.size)
        assertEquals("Goldberg Variations", state.screenTitle)
    }

    @Test
    fun screenTitleFallsBackToPerformanceWhenAlbumNotEmitted() = runTest {
        val vm = createViewModel()
        // uiState uses filterNotNull + map, so it won't emit until album is available
        assertTrue(vm.uiState.value.isLoading)
        assertEquals("Performance", vm.uiState.value.screenTitle)
    }

    // --- entityHeaderState ---

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
    fun entityHeaderShowsNonClassicalAlbumDetails() = runTest {
        val vm = createViewModel(genreId = 1L, albumId = 1L, classicalGenreId = 10L)
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
    fun entityHeaderFallsBackWhenAlbumNotFound() = runTest {
        val vm = createViewModel(albumId = 99L)
        backgroundScope.launch { vm.entityHeaderState.collect() }
        advanceUntilIdle()

        assertFalse(vm.entityHeaderState.value.isLoading)
    }
}
