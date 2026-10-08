package com.chaddy50.froh.ui.screens.artistsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.navigation.ArtistsRoute
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.utilities.chooseAlbumLabel
import com.chaddy50.froh.utilities.chooseArtistLabel
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ArtistWithSubtitle(
    val artist: AlbumArtist,
    val subtitle: String,
)

data class ArtistsScreenUiState(
    val screenTitle: String = "Artists",
    val artists: List<ArtistWithSubtitle> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = ArtistsScreenViewModel.Factory::class)
class ArtistsScreenViewModel @AssistedInject constructor(
    @Assisted route: ArtistsRoute,
    classicalGenreConfig: ClassicalGenreConfig,
    albumArtistRepository: AlbumArtistRepository,
    albumRepository: AlbumRepository,
    genreRepository: GenreRepository,
    playlistRepository: PlaylistRepository,
) : ViewModel() {
    val uiState: StateFlow<ArtistsScreenUiState>
    val entityHeaderState: StateFlow<EntityHeaderState>

    @AssistedFactory
    interface Factory {
        fun create(route: ArtistsRoute): ArtistsScreenViewModel
    }

    init {
        val genreId = route.genreId
        val classicalGenreId = classicalGenreConfig.classicalGenreId
        val isClassical = genreId == classicalGenreId

        val screenTitle = route.title
        val albumLabel = chooseAlbumLabel(isClassical)

        uiState = albumArtistRepository.getAlbumArtistsForGenre(genreId)
            .flatMapLatest { artists ->
                if (artists.isEmpty()) {
                    flowOf(artists.map { ArtistWithSubtitle(it, "") })
                } else {
                    val countFlows = artists.map { artist ->
                        albumRepository.getNumberOfAlbumsForAlbumArtistInGenre(artist.id, genreId)
                    }
                    combine(countFlows) { counts ->
                        artists.mapIndexed { index, artist ->
                            ArtistWithSubtitle(artist, "${counts[index]} $albumLabel")
                        }
                    }
                }
            }
            .map { artistsWithSubtitles ->
                ArtistsScreenUiState(
                    screenTitle = screenTitle,
                    artists = artistsWithSubtitles,
                    isLoading = false,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ArtistsScreenUiState(isLoading = true),
            )

        entityHeaderState = combine(
            genreRepository.getGenreById(genreId),
            albumArtistRepository.getNumberOfAlbumArtistsForGenre(genreId),
            playlistRepository.getPlaylistIdsContainingGenre(genreId),
        ) { genre, numberOfAlbumArtists, playlistsThatGenreIsAlreadyIn ->
            val artistLabel = chooseArtistLabel(genre?.id == classicalGenreId)
            EntityHeaderState(
                genre?.name ?: "Genre",
                "$numberOfAlbumArtists $artistLabel",
                null,
                null,
                false,
                playlistsThatGenreIsAlreadyIn,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            EntityHeaderState(),
        )

    }
}
