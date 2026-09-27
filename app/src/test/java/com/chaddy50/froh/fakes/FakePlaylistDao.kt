package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.dao.PlaylistDao
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.entity.PlaylistTrack
import com.chaddy50.froh.data.entity.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePlaylistDao(
    private val allPlaylistsFlow: MutableStateFlow<List<Playlist>> = MutableStateFlow(emptyList()),
    private val playlistByIdFlow: MutableStateFlow<Playlist?> = MutableStateFlow(null),
    private val tracksForPlaylistFlow: MutableStateFlow<List<Track>> = MutableStateFlow(emptyList()),
    private val playlistIdsContainingTrackFlow: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList()),
    private val playlistIdsContainingAlbumFlow: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList()),
    private val playlistIdsContainingAlbumArtistFlow: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList()),
    private val playlistIdsContainingGenreFlow: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList()),
) : PlaylistDao {
    val insertedPlaylists = mutableListOf<Playlist>()
    val updatedPlaylists = mutableListOf<Playlist>()
    val deletedPlaylists = mutableListOf<Playlist>()
    val insertedPlaylistTracks = mutableListOf<PlaylistTrack>()
    val deletedPlaylistTracks = mutableListOf<Pair<Long, Long>>()
    var nextPlaylistId = 1L

    override fun getAllPlaylists(): Flow<List<Playlist>> = allPlaylistsFlow
    override fun getPlaylistById(id: Long): Flow<Playlist?> = playlistByIdFlow
    override fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> = tracksForPlaylistFlow

    override suspend fun getMaxPosition(playlistId: Long): Int? {
        val matching = insertedPlaylistTracks.filter { it.playlistId == playlistId }
        return if (matching.isEmpty()) null else matching.maxOf { it.position }
    }

    override suspend fun insertPlaylist(playlist: Playlist): Long {
        val id = nextPlaylistId++
        insertedPlaylists.add(playlist.copy(id = id))
        return id
    }

    override suspend fun update(playlist: Playlist) {
        updatedPlaylists.add(playlist)
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        deletedPlaylists.add(playlist)
    }

    override suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrack) {
        insertedPlaylistTracks.add(playlistTrack)
    }

    override suspend fun deletePlaylistTrack(playlistId: Long, trackId: Long) {
        deletedPlaylistTracks.add(playlistId to trackId)
    }

    override fun getPlaylistIdsContainingTrack(trackId: Long): Flow<List<Long>> =
        playlistIdsContainingTrackFlow

    override fun getPlaylistIdsContainingAlbum(albumId: Long): Flow<List<Long>> =
        playlistIdsContainingAlbumFlow

    override fun getPlaylistIdsContainingAlbumArtist(albumArtistId: Long): Flow<List<Long>> =
        playlistIdsContainingAlbumArtistFlow

    override fun getPlaylistIdsContainingGenre(genreId: Long): Flow<List<Long>> =
        playlistIdsContainingGenreFlow
}
