package com.chaddy50.froh.data.repository

import com.chaddy50.froh.data.dao.PlaylistDao
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.entity.PlaylistTrack
import com.chaddy50.froh.data.entity.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepository(private val playlistDao: PlaylistDao) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getPlaylistById(id: Long): Flow<Playlist?> = playlistDao.getPlaylistById(id)

    fun getTracksForPlaylist(id: Long): Flow<List<Track>> = playlistDao.getTracksForPlaylist(id)

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun update(playlist: Playlist) {
        playlistDao.update(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val maxPosition = playlistDao.getMaxPosition(playlistId) ?: -1
        playlistDao.insertPlaylistTrack(
            PlaylistTrack(
                playlistId = playlistId,
                trackId = trackId,
                position = maxPosition + 1,
            )
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDao.deletePlaylistTrack(playlistId, trackId)
    }

    fun getPlaylistIdsContainingTrack(trackId: Long): Flow<Set<Long>> =
        playlistDao.getPlaylistIdsContainingTrack(trackId).map { it.toSet() }

    fun getPlaylistIdsContainingAlbum(albumId: Long): Flow<Set<Long>> =
        playlistDao.getPlaylistIdsContainingAlbum(albumId).map { it.toSet() }

    fun getPlaylistIdsContainingAlbumArtist(albumArtistId: Long): Flow<Set<Long>> =
        playlistDao.getPlaylistIdsContainingAlbumArtist(albumArtistId).map { it.toSet() }

    fun getPlaylistIdsContainingGenre(genreId: Long): Flow<Set<Long>> =
        playlistDao.getPlaylistIdsContainingGenre(genreId).map { it.toSet() }
}
