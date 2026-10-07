package com.chaddy50.froh.ui.screens.playlistTracksScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.navigation.PlaylistTracksRoute
import com.chaddy50.froh.ui.composables.entityHeader.EntityHeaderState
import com.chaddy50.froh.utilities.formatMillisecondsIntoMinutesAndSeconds
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.entity.Track

data class PlaylistTracksScreenState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = PlaylistTracksScreenViewModel.Factory::class)
class PlaylistTracksScreenViewModel @AssistedInject constructor(
    @Assisted route: PlaylistTracksRoute,
    playlistRepository: PlaylistRepository,
) : ViewModel() {
    val uiState: StateFlow<PlaylistTracksScreenState>
    val entityHeaderState: StateFlow<EntityHeaderState>

    @AssistedFactory
    interface Factory {
        fun create(route: PlaylistTracksRoute): PlaylistTracksScreenViewModel
    }

    init {
        val playlistId = route.playlistId

        val stateFlow = playlistRepository.getPlaylistById(playlistId).flatMapLatest { playlist ->
            if (playlist == null) {
                flowOf(PlaylistTracksScreenState(isLoading = false))
            } else {
                playlistRepository.getTracksForPlaylist(playlistId).flatMapLatest { tracks ->
                    flowOf(PlaylistTracksScreenState(playlist, tracks, false))
                }
            }
        }

        uiState = stateFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlaylistTracksScreenState(isLoading = true),
        )

        entityHeaderState = combine(
            playlistRepository.getPlaylistById(playlistId),
            playlistRepository.getTracksForPlaylist(playlistId),
        ) { playlist, tracks ->
            val playlistDurationMs = tracks.sumOf { it.duration.inWholeMilliseconds }
            EntityHeaderState(
                playlist?.name ?: "Playlist",
                "${tracks.size} tracks - ${formatMillisecondsIntoMinutesAndSeconds(playlistDurationMs)}",
                null,
                null,
                false,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            EntityHeaderState(),
        )
    }
}
