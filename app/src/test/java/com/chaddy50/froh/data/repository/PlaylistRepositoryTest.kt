package com.chaddy50.froh.data.repository

import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.fakes.FakePlaylistDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistRepositoryTest {

    @Test
    fun updateDelegatesToDaoWithGivenPlaylist() = runTest {
        val dao = FakePlaylistDao()
        val repo = PlaylistRepository(dao)
        val playlist = Playlist(id = 1, name = "Renamed")

        repo.update(playlist)

        assertEquals(listOf(playlist), dao.updatedPlaylists)
    }

    @Test
    fun addTrackToPlaylistInsertsWithNextPosition() = runTest {
        val dao = FakePlaylistDao()
        val repo = PlaylistRepository(dao)

        repo.addTrackToPlaylist(playlistId = 1, trackId = 10)
        repo.addTrackToPlaylist(playlistId = 1, trackId = 20)

        val tracks = dao.insertedPlaylistTracks
        assertEquals(2, tracks.size)
        assertEquals(0, tracks[0].position)
        assertEquals(1, tracks[1].position)
    }

    @Test
    fun addTrackToPlaylistStartsAtZeroWhenEmpty() = runTest {
        val dao = FakePlaylistDao()
        val repo = PlaylistRepository(dao)

        repo.addTrackToPlaylist(playlistId = 1, trackId = 10)

        val track = dao.insertedPlaylistTracks.single()
        assertEquals(1L, track.playlistId)
        assertEquals(10L, track.trackId)
        assertEquals(0, track.position)
    }
}
