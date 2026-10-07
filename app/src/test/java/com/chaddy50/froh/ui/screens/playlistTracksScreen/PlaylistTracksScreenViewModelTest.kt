package com.chaddy50.froh.ui.screens.playlistTracksScreen

import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.fakes.testTrack
import com.chaddy50.froh.navigation.PlaylistTracksRoute
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
class PlaylistTracksScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playlistFlow = MutableStateFlow<Playlist?>(null)
    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())

    private fun createViewModel(
        playlistId: Long = 1L,
        title: String = "Test",
    ): PlaylistTracksScreenViewModel {
        val dao = FakePlaylistDao(
            playlistByIdFlow = playlistFlow,
            tracksForPlaylistFlow = tracksFlow,
        )
        return PlaylistTracksScreenViewModel(PlaylistTracksRoute(playlistId = playlistId, title = title), PlaylistRepository(dao))
    }

    @Test
    fun initialStateIsLoading() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun emitsPlaylistAndTracks() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        playlistFlow.value = Playlist(id = 1, name = "Favorites")
        tracksFlow.value = listOf(testTrack(id = 1), testTrack(id = 2))
        advanceUntilIdle()
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Favorites", state.playlist?.name)
        assertEquals(2, state.tracks.size)
    }

    @Test
    fun nullPlaylistReturnsNonLoadingEmptyState() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        playlistFlow.value = null
        advanceUntilIdle()
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.playlist)
        assertEquals(emptyList<Track>(), state.tracks)
    }

    @Test
    fun entityHeaderShowsTrackCountAndDuration() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.entityHeaderState.collect() }
        playlistFlow.value = Playlist(id = 1, name = "Favorites")
        tracksFlow.value = listOf(
            testTrack(id = 1, durationMs = 180000),
            testTrack(id = 2, durationMs = 120000),
        )
        advanceUntilIdle()
        val header = vm.entityHeaderState.value
        assertEquals("Favorites", header.title)
        assertEquals("2 tracks - 5:00", header.subtitle)
    }

    @Test
    fun entityHeaderFallsBackToPlaylistWhenNull() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.entityHeaderState.collect() }
        playlistFlow.value = null
        tracksFlow.value = emptyList()
        advanceUntilIdle()
        assertEquals("Playlist", vm.entityHeaderState.value.title)
    }
}
