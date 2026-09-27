package com.chaddy50.froh.ui.screens.playlistsScreen

import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.fakes.testTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())
    private val trackDao = FakeTrackDao(tracksFlow)
    private val playlistDao = FakePlaylistDao()

    private fun createViewModel(): PlaylistViewModel {
        return PlaylistViewModel(
            TrackRepository(trackDao),
            PlaylistRepository(playlistDao),
        )
    }

    // --- Mutation: add to existing playlist ---

    @Test
    fun addGenreToPlaylistAddsAllGenreTracks() = runTest {
        tracksFlow.value = listOf(
            testTrack(id = 1, genreId = 10),
            testTrack(id = 2, genreId = 10),
            testTrack(id = 3, genreId = 99),
        )
        val vm = createViewModel()
        vm.addGenreToPlaylist(playlistId = 5, genreId = 10)
        advanceUntilIdle()
        val added = playlistDao.insertedPlaylistTracks
        assertEquals(2, added.size)
        assertEquals(setOf(1L, 2L), added.map { it.trackId }.toSet())
        added.forEach { assertEquals(5L, it.playlistId) }
    }

    @Test
    fun addAlbumArtistToPlaylistAddsAllArtistTracks() = runTest {
        tracksFlow.value = listOf(
            testTrack(id = 1, albumArtistId = 20),
            testTrack(id = 2, albumArtistId = 20),
            testTrack(id = 3, albumArtistId = 99),
        )
        val vm = createViewModel()
        vm.addAlbumArtistToPlaylist(playlistId = 5, albumArtistId = 20)
        advanceUntilIdle()
        val added = playlistDao.insertedPlaylistTracks
        assertEquals(2, added.size)
        assertEquals(setOf(1L, 2L), added.map { it.trackId }.toSet())
    }

    @Test
    fun addAlbumToPlaylistAddsAllAlbumTracks() = runTest {
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 30),
            testTrack(id = 2, albumId = 30),
            testTrack(id = 3, albumId = 99),
        )
        val vm = createViewModel()
        vm.addAlbumToPlaylist(playlistId = 5, albumId = 30)
        advanceUntilIdle()
        val added = playlistDao.insertedPlaylistTracks
        assertEquals(2, added.size)
        assertEquals(setOf(1L, 2L), added.map { it.trackId }.toSet())
    }

    @Test
    fun addPlaylistTracksToPlaylistCopiesTracks() = runTest {
        val sourceTracks = listOf(testTrack(id = 1), testTrack(id = 2))
        val sourcePlaylistDao = FakePlaylistDao(
            tracksForPlaylistFlow = kotlinx.coroutines.flow.MutableStateFlow(sourceTracks),
        )
        val vm = PlaylistViewModel(
            TrackRepository(trackDao),
            PlaylistRepository(sourcePlaylistDao),
        )
        vm.addPlaylistTracksToPlaylist(targetPlaylistId = 5, sourcePlaylistId = 1)
        advanceUntilIdle()
        val added = sourcePlaylistDao.insertedPlaylistTracks
        assertEquals(2, added.size)
        assertEquals(setOf(1L, 2L), added.map { it.trackId }.toSet())
        added.forEach { assertEquals(5L, it.playlistId) }
    }

    @Test
    fun addTrackToPlaylistAddsSingleTrack() = runTest {
        val track = testTrack(id = 7)
        val vm = createViewModel()
        vm.addTrackToPlaylist(playlistId = 5, track = track)
        advanceUntilIdle()
        val added = playlistDao.insertedPlaylistTracks
        assertEquals(1, added.size)
        assertEquals(7L, added[0].trackId)
        assertEquals(5L, added[0].playlistId)
    }

    // --- Mutation: create playlist and add ---

    @Test
    fun createPlaylistAndAddGenreCreatesAndAdds() = runTest {
        tracksFlow.value = listOf(
            testTrack(id = 1, genreId = 10),
            testTrack(id = 2, genreId = 10),
        )
        val vm = createViewModel()
        vm.createPlaylistAndAddGenre("My Genre Playlist", genreId = 10)
        advanceUntilIdle()
        assertEquals(1, playlistDao.insertedPlaylists.size)
        assertEquals("My Genre Playlist", playlistDao.insertedPlaylists[0].name)
        val added = playlistDao.insertedPlaylistTracks
        assertEquals(2, added.size)
        added.forEach { assertEquals(playlistDao.insertedPlaylists[0].id, it.playlistId) }
    }

    @Test
    fun createPlaylistAndAddAlbumArtistCreatesAndAdds() = runTest {
        tracksFlow.value = listOf(
            testTrack(id = 1, albumArtistId = 20),
            testTrack(id = 2, albumArtistId = 20),
        )
        val vm = createViewModel()
        vm.createPlaylistAndAddAlbumArtist("Artist Playlist", albumArtistId = 20)
        advanceUntilIdle()
        assertEquals(1, playlistDao.insertedPlaylists.size)
        assertEquals("Artist Playlist", playlistDao.insertedPlaylists[0].name)
        assertEquals(2, playlistDao.insertedPlaylistTracks.size)
    }

    @Test
    fun createPlaylistAndAddAlbumCreatesAndAdds() = runTest {
        tracksFlow.value = listOf(
            testTrack(id = 1, albumId = 30),
            testTrack(id = 2, albumId = 30),
        )
        val vm = createViewModel()
        vm.createPlaylistAndAddAlbum("Album Playlist", albumId = 30)
        advanceUntilIdle()
        assertEquals(1, playlistDao.insertedPlaylists.size)
        assertEquals("Album Playlist", playlistDao.insertedPlaylists[0].name)
        assertEquals(2, playlistDao.insertedPlaylistTracks.size)
    }

    @Test
    fun createPlaylistCreatesEmptyPlaylist() = runTest {
        val vm = createViewModel()
        vm.createPlaylist("Empty Playlist")
        advanceUntilIdle()
        assertEquals(1, playlistDao.insertedPlaylists.size)
        assertEquals("Empty Playlist", playlistDao.insertedPlaylists[0].name)
        assertEquals(0, playlistDao.insertedPlaylistTracks.size)
    }

    @Test
    fun createPlaylistAndAddTrackCreatesAndAddsSingle() = runTest {
        val track = testTrack(id = 7)
        val vm = createViewModel()
        vm.createPlaylistAndAddTrack("Single Track Playlist", track)
        advanceUntilIdle()
        assertEquals(1, playlistDao.insertedPlaylists.size)
        assertEquals("Single Track Playlist", playlistDao.insertedPlaylists[0].name)
        val added = playlistDao.insertedPlaylistTracks
        assertEquals(1, added.size)
        assertEquals(7L, added[0].trackId)
    }

    // --- Mutation: delete and remove ---

    @Test
    fun deletePlaylistDelegatesToRepository() = runTest {
        val playlist = Playlist(id = 1, name = "To Delete")
        val vm = createViewModel()
        vm.deletePlaylist(playlist)
        advanceUntilIdle()
        assertEquals(1, playlistDao.deletedPlaylists.size)
        assertEquals(1L, playlistDao.deletedPlaylists[0].id)
    }

    @Test
    fun renamePlaylistDelegatesToRepositoryWithNewName() = runTest {
        val playlist = Playlist(id = 1, name = "Old Name")
        val vm = createViewModel()
        vm.renamePlaylist(playlist, "New Name")
        advanceUntilIdle()
        assertEquals(1, playlistDao.updatedPlaylists.size)
        assertEquals(1L, playlistDao.updatedPlaylists[0].id)
        assertEquals("New Name", playlistDao.updatedPlaylists[0].name)
    }

    @Test
    fun removeTrackFromPlaylistDelegatesToRepository() = runTest {
        val vm = createViewModel()
        vm.removeTrackFromPlaylist(playlistId = 5, trackId = 7)
        advanceUntilIdle()
        assertEquals(1, playlistDao.deletedPlaylistTracks.size)
        assertEquals(5L to 7L, playlistDao.deletedPlaylistTracks[0])
    }
}
