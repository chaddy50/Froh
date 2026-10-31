package com.chaddy50.froh.ui.screens.playlistsScreen

import com.chaddy50.froh.data.dao.PlaylistDao
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.entity.PlaylistTrack
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

/**
 * Delegates every [PlaylistDao] method to a plain [FakePlaylistDao], except
 * [getTracksForPlaylist], which this test needs correctly scoped per playlist id (the
 * shared [FakePlaylistDao] intentionally keeps that method a single flat, id-agnostic flow
 * for its other, single-playlist-at-a-time callers).
 */
private class MultiPlaylistFakePlaylistDao(
    allPlaylistsFlow: MutableStateFlow<List<Playlist>>,
    private val playlistTracksFlow: MutableStateFlow<List<PlaylistTrack>>,
    private val allTracksFlow: MutableStateFlow<List<Track>>,
) : PlaylistDao by FakePlaylistDao(allPlaylistsFlow = allPlaylistsFlow) {
    override fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> =
        combine(playlistTracksFlow, allTracksFlow) { playlistTracks, tracks ->
            playlistTracks
                .filter { it.playlistId == playlistId }
                .sortedBy { it.position }
                .mapNotNull { playlistTrack -> tracks.find { it.id == playlistTrack.trackId } }
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistsScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playlistsFlow = MutableStateFlow<List<Playlist>>(emptyList())
    private val playlistTracksFlow = MutableStateFlow<List<PlaylistTrack>>(emptyList())
    private val allTracksFlow = MutableStateFlow<List<Track>>(emptyList())

    private fun createViewModel(): PlaylistsScreenViewModel {
        val dao = MultiPlaylistFakePlaylistDao(playlistsFlow, playlistTracksFlow, allTracksFlow)
        return PlaylistsScreenViewModel(PlaylistRepository(dao), TrackRepository(FakeTrackDao()))
    }

    private fun makeTrack(id: Long, albumId: Long, artworkPath: String? = null) = Track(
        id = id,
        uri = "uri/$id",
        title = "Track $id",
        number = 1,
        albumId = albumId,
        albumName = "Album $albumId",
        artistId = 1,
        artistName = "Artist",
        albumArtistId = 1,
        albumArtistName = "Artist",
        genreId = 1,
        genreName = "Genre",
        parentGenreId = null,
        parentGenreName = null,
        duration = 3.minutes,
        discNumber = 1,
        artworkPath = artworkPath,
        year = "2000",
    )

    /** Each (albumId, artworkPath) pair becomes one track in the given playlist, in order. */
    private fun addTracksToPlaylist(playlistId: Long, vararg albumIdToArtworkPath: Pair<Long, String>) {
        val startingTrackId = (allTracksFlow.value.size + 1).toLong()
        val newTracks = albumIdToArtworkPath.mapIndexed { index, (albumId, artworkPath) ->
            makeTrack(id = startingTrackId + index, albumId = albumId, artworkPath = artworkPath)
        }
        val newPlaylistTracks = newTracks.mapIndexed { index, track ->
            PlaylistTrack(playlistId = playlistId, trackId = track.id, position = index)
        }
        allTracksFlow.value = allTracksFlow.value + newTracks
        playlistTracksFlow.value = playlistTracksFlow.value + newPlaylistTracks
    }

    @Test
    fun initialStateIsLoading() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.isLoading)
        assertEquals(emptyList<PlaylistWithCollageArtwork>(), vm.uiState.value.playlists)
    }

    @Test
    fun emitsPlaylistsFromRepository() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        val playlists = listOf(
            Playlist(id = 1, name = "Favorites"),
            Playlist(id = 2, name = "Workout"),
        )
        playlistsFlow.value = playlists
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(playlists, vm.uiState.value.playlists.map { it.playlist })
    }

    @Test
    fun updatesWhenRepositoryEmitsNewList() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        playlistsFlow.value = listOf(Playlist(id = 1, name = "Favorites"))
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.playlists.size)

        playlistsFlow.value = listOf(
            Playlist(id = 1, name = "Favorites"),
            Playlist(id = 2, name = "Workout"),
        )
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.playlists.size)
    }

    @Test
    fun playlistWithZeroOrOneDistinctAlbumHasNoCollageArtwork() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        addTracksToPlaylist(playlistId = 2, 1L to "/a.jpg")
        playlistsFlow.value = listOf(
            Playlist(id = 1, name = "Empty"),
            Playlist(id = 2, name = "OneAlbum"),
        )
        advanceUntilIdle()

        val playlistsById = vm.uiState.value.playlists.associateBy { it.playlist.id }
        assertNull(playlistsById.getValue(1).collageArtworkPaths)
        assertNull(playlistsById.getValue(2).collageArtworkPaths)
    }

    @Test
    fun manyTracksFromOneAlbumStillCountAsOneDistinctAlbum() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        addTracksToPlaylist(
            playlistId = 1,
            1L to "/a.jpg",
            1L to "/a.jpg",
            1L to "/a.jpg",
        )
        playlistsFlow.value = listOf(Playlist(id = 1, name = "Favorites"))
        advanceUntilIdle()

        assertNull(vm.uiState.value.playlists.single().collageArtworkPaths)
    }

    @Test
    fun playlistWithTwoDistinctAlbumsCyclesArtworkToFillFourCells() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        addTracksToPlaylist(playlistId = 1, 1L to "/a.jpg", 2L to "/b.jpg")
        playlistsFlow.value = listOf(Playlist(id = 1, name = "Favorites"))
        advanceUntilIdle()

        assertEquals(
            listOf("/a.jpg", "/b.jpg", "/a.jpg", "/b.jpg"),
            vm.uiState.value.playlists.single().collageArtworkPaths,
        )
    }

    @Test
    fun playlistWithThreeDistinctAlbumsCyclesArtworkToFillFourCells() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        addTracksToPlaylist(playlistId = 1, 1L to "/a.jpg", 2L to "/b.jpg", 3L to "/c.jpg")
        playlistsFlow.value = listOf(Playlist(id = 1, name = "Favorites"))
        advanceUntilIdle()

        assertEquals(
            listOf("/a.jpg", "/b.jpg", "/c.jpg", "/a.jpg"),
            vm.uiState.value.playlists.single().collageArtworkPaths,
        )
    }

    @Test
    fun playlistWithFourOrMoreDistinctAlbumsUsesFirstFourWithoutCycling() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        addTracksToPlaylist(
            playlistId = 1,
            1L to "/a.jpg",
            2L to "/b.jpg",
            3L to "/c.jpg",
            4L to "/d.jpg",
            5L to "/e.jpg",
        )
        playlistsFlow.value = listOf(Playlist(id = 1, name = "Favorites"))
        advanceUntilIdle()

        assertEquals(
            listOf("/a.jpg", "/b.jpg", "/c.jpg", "/d.jpg"),
            vm.uiState.value.playlists.single().collageArtworkPaths,
        )
    }

    @Test
    fun looksPastFirstFourTracksWhenTheyShareAnAlbumToFindDistinctAlbums() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        // First four tracks are all the same album; a second album only appears as the 5th track.
        addTracksToPlaylist(
            playlistId = 1,
            1L to "/a.jpg",
            1L to "/a.jpg",
            1L to "/a.jpg",
            1L to "/a.jpg",
            2L to "/b.jpg",
        )
        playlistsFlow.value = listOf(Playlist(id = 1, name = "Favorites"))
        advanceUntilIdle()

        assertEquals(
            listOf("/a.jpg", "/b.jpg", "/a.jpg", "/b.jpg"),
            vm.uiState.value.playlists.single().collageArtworkPaths,
        )
    }

    @Test
    fun eachPlaylistGetsIndependentlyCorrectCollageArtwork() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        addTracksToPlaylist(playlistId = 1, 1L to "/a.jpg", 2L to "/b.jpg", 3L to "/c.jpg", 4L to "/d.jpg")
        addTracksToPlaylist(playlistId = 2, 5L to "/x.jpg", 6L to "/y.jpg")
        playlistsFlow.value = listOf(
            Playlist(id = 1, name = "Favorites"),
            Playlist(id = 2, name = "Workout"),
        )
        advanceUntilIdle()

        val playlistsById = vm.uiState.value.playlists.associateBy { it.playlist.id }
        assertEquals(
            listOf("/a.jpg", "/b.jpg", "/c.jpg", "/d.jpg"),
            playlistsById.getValue(1).collageArtworkPaths,
        )
        assertEquals(
            listOf("/x.jpg", "/y.jpg", "/x.jpg", "/y.jpg"),
            playlistsById.getValue(2).collageArtworkPaths,
        )
    }
}
