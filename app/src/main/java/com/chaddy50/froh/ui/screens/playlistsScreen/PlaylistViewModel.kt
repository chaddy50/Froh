package com.chaddy50.froh.ui.screens.playlistsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {
    val allPlaylists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- Playlist query methods ---

    fun getPlaylistsThatTrackIsAlreadyIn(trackId: Long): Flow<Set<Long>> =
        playlistRepository.getPlaylistIdsContainingTrack(trackId)

    fun getPlaylistsThatAlbumIsAlreadyIn(albumId: Long): Flow<Set<Long>> =
        playlistRepository.getPlaylistIdsContainingAlbum(albumId)

    fun getPlaylistsThatAlbumArtistIsAlreadyIn(albumArtistId: Long): Flow<Set<Long>> =
        playlistRepository.getPlaylistIdsContainingAlbumArtist(albumArtistId)

    fun getPlaylistsThatGenreIsAlreadyIn(genreId: Long): Flow<Set<Long>> =
        playlistRepository.getPlaylistIdsContainingGenre(genreId)

    // --- Playlist mutation methods ---

    fun addGenreToPlaylist(playlistId: Long, genreId: Long) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksForGenre(genreId).first()
            tracks.forEach { track -> playlistRepository.addTrackToPlaylist(playlistId, track.id) }
        }
    }

    fun addAlbumArtistToPlaylist(playlistId: Long, albumArtistId: Long) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksForAlbumArtist(albumArtistId).first()
            tracks.forEach { track -> playlistRepository.addTrackToPlaylist(playlistId, track.id) }
        }
    }

    fun addAlbumToPlaylist(playlistId: Long, albumId: Long) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksForAlbum(albumId).first()
            tracks.forEach { track -> playlistRepository.addTrackToPlaylist(playlistId, track.id) }
        }
    }

    fun addPlaylistTracksToPlaylist(targetPlaylistId: Long, sourcePlaylistId: Long) {
        viewModelScope.launch {
            val tracks = playlistRepository.getTracksForPlaylist(sourcePlaylistId).first()
            tracks.forEach { track -> playlistRepository.addTrackToPlaylist(targetPlaylistId, track.id) }
        }
    }

    fun createPlaylistAndAddGenre(name: String, genreId: Long) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            val tracks = trackRepository.getTracksForGenre(genreId).first()
            tracks.forEach { track -> playlistRepository.addTrackToPlaylist(playlistId, track.id) }
        }
    }

    fun createPlaylistAndAddAlbumArtist(name: String, albumArtistId: Long) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            val tracks = trackRepository.getTracksForAlbumArtist(albumArtistId).first()
            tracks.forEach { track -> playlistRepository.addTrackToPlaylist(playlistId, track.id) }
        }
    }

    fun createPlaylistAndAddAlbum(name: String, albumId: Long) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            val tracks = trackRepository.getTracksForAlbum(albumId).first()
            tracks.forEach { track -> playlistRepository.addTrackToPlaylist(playlistId, track.id) }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { playlistRepository.createPlaylist(name) }
    }

    fun createPlaylistAndAddTrack(name: String, track: Track) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            playlistRepository.addTrackToPlaylist(playlistId, track.id)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlist) }
    }

    fun renamePlaylist(playlist: Playlist, newName: String) {
        viewModelScope.launch { playlistRepository.update(playlist.copy(name = newName)) }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch { playlistRepository.addTrackToPlaylist(playlistId, track.id) }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch { playlistRepository.removeTrackFromPlaylist(playlistId, trackId) }
    }
}
