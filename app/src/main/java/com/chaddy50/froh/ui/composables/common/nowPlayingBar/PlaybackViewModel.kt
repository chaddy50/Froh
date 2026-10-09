package com.chaddy50.froh.ui.composables.common.nowPlayingBar

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.data.preferences.IQueuePreferences
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.utilities.chooseAlbumArtworkPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val application: Application,
    private val classicalGenreConfig: ClassicalGenreConfig,
    private val trackRepository: TrackRepository,
    private val playlistRepository: PlaylistRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    private val queuePreferences: IQueuePreferences,
) : ViewModel() {
    val nowPlayingState = NowPlayingState(application, viewModelScope)
    private val controller: MediaController? get() = nowPlayingState.controller

    val isQueueHidden: StateFlow<Boolean> = queuePreferences.isQueueHidden
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleQueueHidden() {
        viewModelScope.launch {
            queuePreferences.setQueueHidden(!isQueueHidden.value)
        }
    }

    fun playAllTracks(shuffled: Boolean) {
        viewModelScope.launch {
            val tracks = trackRepository.getAllTracks().first()
            playTracks(tracks, shuffled)
        }
    }

    fun playTracksForGenre(genreId: Long, shuffled: Boolean) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksForGenre(genreId).first()
            playTracks(tracks, shuffled)
        }
    }

    fun playTracksForAlbumArtist(albumArtistId: Long, genreId: Long, shuffled: Boolean) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksForAlbumArtistInGenre(albumArtistId, genreId).first()
            playTracks(tracks, shuffled)
        }
    }

    fun playTracksForAlbum(albumId: Long, performanceId: Long?, shuffled: Boolean) {
        viewModelScope.launch {
            val tracks = if (performanceId != null) {
                trackRepository.getTracksForPerformance(performanceId).first()
            } else {
                trackRepository.getTracksForAlbum(albumId).first()
            }
            playTracks(tracks, shuffled)
        }
    }

    fun playTracksForAlbumInGenre(albumId: Long, genreId: Long, performanceId: Long?, shuffled: Boolean) {
        viewModelScope.launch {
            val tracks = if (performanceId != null) {
                trackRepository.getTracksForPerformance(performanceId).first()
            } else {
                trackRepository.getTracksForAlbumInGenre(albumId, genreId).first()
            }
            playTracks(tracks, shuffled)
        }
    }

    fun playTracksForPlaylist(playlistId: Long, shuffled: Boolean) {
        viewModelScope.launch {
            val tracks = playlistRepository.getTracksForPlaylist(playlistId).first()
            playTracks(tracks, shuffled)
        }
    }

    private suspend fun playTracks(tracks: List<Track>, shuffled: Boolean) {
        if (tracks.isNotEmpty()) {
            val mediaItems = buildMediaItems(tracks, classicalGenreConfig.classicalGenreId, albumArtistRepository)
            controller?.let { controller ->
                controller.shuffleModeEnabled = shuffled
                controller.setMediaItems(mediaItems)
                controller.prepare()
                controller.play()
            }
        }
    }

    fun playTrack(track: Track, allTracks: List<Track>) {
        viewModelScope.launch {
            val mediaItems = buildMediaItems(allTracks, classicalGenreConfig.classicalGenreId, albumArtistRepository)
            val trackIndex = allTracks.indexOfFirst { it.id == track.id }

            controller?.let { controller ->
                controller.setMediaItems(mediaItems, trackIndex, 0)
                controller.prepare()
                controller.play()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        nowPlayingState.release()
    }
}

@VisibleForTesting
internal suspend fun buildMediaItems(
    tracks: List<Track>,
    classicalGenreId: Long?,
    albumArtistRepository: AlbumArtistRepository,
): List<MediaItem> {
    val albumArtistPortraits = tracks.map { it.albumArtistId }.distinct()
        .associateWith { albumArtistRepository.getAlbumArtistById(it).first()?.portraitPath }

    return tracks.map { track ->
        val isClassicalTrack = track.parentGenreId == classicalGenreId
        var artist = track.artistName
        if (isClassicalTrack) {
            artist += " - ${track.year}"
        }
        val artworkPath = chooseAlbumArtworkPath(
            isClassicalTrack, track.artworkPath, albumArtistPortraits[track.albumArtistId],
        )

        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(artist)
            .setGenre(track.genreName)
            .setAlbumArtist(track.albumArtistName)
            .setAlbumTitle(track.albumName)
            .setDurationMs(track.duration.inWholeMilliseconds)
            .setArtworkUri(artworkPath?.toUri())
            .build()

        MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.uri)
            .setMediaMetadata(metadata)
            .build()
    }
}
