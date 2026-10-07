package com.chaddy50.froh.ui.screens.performancesScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.PerformanceRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.navigation.PerformancesRoute
import com.chaddy50.froh.ui.composables.entityHeader.EntityHeaderState
import com.chaddy50.froh.utilities.formatMillisecondsIntoMinutesAndSeconds
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PerformanceScreenUiState(
    val screenTitle: String = "Performance",
    val performances: List<Performance> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = PerformancesScreenViewModel.Factory::class)
class PerformancesScreenViewModel @AssistedInject constructor(
    @Assisted route: PerformancesRoute,
    classicalGenreConfig: ClassicalGenreConfig,
    performanceRepository: PerformanceRepository,
    albumRepository: AlbumRepository,
    albumArtistRepository: AlbumArtistRepository,
    trackRepository: TrackRepository,
    playlistRepository: PlaylistRepository,
) : ViewModel() {
    val genreId: Long
    val uiState: StateFlow<PerformanceScreenUiState>
    val entityHeaderState: StateFlow<EntityHeaderState>

    @AssistedFactory
    interface Factory {
        fun create(route: PerformancesRoute): PerformancesScreenViewModel
    }

    init {
        val albumId = route.albumId
        genreId = route.genreId
        val classicalGenreId = classicalGenreConfig.classicalGenreId
        val isClassical = genreId == classicalGenreId

        val albumTitle: Flow<String> = albumRepository.getAlbumById(albumId)
            .filterNotNull()
            .map { it.title }

        val performances: Flow<List<Performance>> =
            performanceRepository.getPerformancesForAlbumForGenre(albumId, genreId)

        uiState = combine(performances, albumTitle) { performances, albumTitle ->
            PerformanceScreenUiState(albumTitle, performances, false)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PerformanceScreenUiState(isLoading = true),
        )

        entityHeaderState = albumRepository.getAlbumById(albumId).flatMapLatest { album ->
            if (album == null) {
                return@flatMapLatest flowOf(EntityHeaderState(isLoading = false))
            }

            combine(
                albumArtistRepository.getAlbumArtistById(album.artistId),
                trackRepository.getTracksForAlbum(albumId),
                performanceRepository.getNumberOfPerformancesForAlbumForGenre(albumId, genreId),
                playlistRepository.getPlaylistIdsContainingAlbum(album.id),
            ) { albumArtist, tracks, numberOfPerformances, playlistsThatAlbumIsAlreadyIn ->
                val albumDurationMs = tracks.sumOf { it.duration.inWholeMilliseconds }
                var items: List<String> = listOf()
                if (!isClassical) {
                    items = items.plus(album.year)
                }
                if (isClassical) {
                    items = items.plus("$numberOfPerformances performances")
                } else {
                    items = items.plus("${tracks.size} tracks")
                }
                if (!isClassical) {
                    items = items.plus(formatMillisecondsIntoMinutesAndSeconds(albumDurationMs))
                }
                EntityHeaderState(
                    album.title,
                    albumArtist?.name ?: "Unknown Artist",
                    items.joinToString(" - "),
                    if (isClassical) null else album.artworkPath,
                    false,
                    playlistsThatAlbumIsAlreadyIn,
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            EntityHeaderState(),
        )
    }
}
