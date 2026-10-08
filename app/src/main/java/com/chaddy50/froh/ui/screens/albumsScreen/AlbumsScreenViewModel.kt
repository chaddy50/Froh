package com.chaddy50.froh.ui.screens.albumsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.ComposerRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.navigation.AlbumsRoute
import com.chaddy50.froh.utilities.chooseAlbumLabel
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class AlbumsScreenUiState(
    val screenTitle: String = "Artists",
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = true
)

private const val DEFAULT_GENRE_NAME = "Genre"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = AlbumsScreenViewModel.Factory::class)
class AlbumsScreenViewModel @AssistedInject constructor(
    @Assisted route: AlbumsRoute,
    classicalGenreConfig: ClassicalGenreConfig,
    albumRepository: AlbumRepository,
    albumArtistRepository: AlbumArtistRepository,
    genreRepository: GenreRepository,
    playlistRepository: PlaylistRepository,
    composerRepository: ComposerRepository,
) : ViewModel() {
    private val _selectedSubGenreId = MutableStateFlow<Long?>(null)
    val selectedSubGenreId = _selectedSubGenreId.asStateFlow()

    val isClassical: Boolean
    val genreId: Long
    val genreName: StateFlow<String>
    val subGenres: StateFlow<List<Genre>>
    val uiState: StateFlow<AlbumsScreenUiState>
    val entityHeaderState: StateFlow<EntityHeaderState>

    @AssistedFactory
    interface Factory {
        fun create(route: AlbumsRoute): AlbumsScreenViewModel
    }

    init {
        genreId = route.genreId
        val albumArtistId = route.albumArtistId
        val classicalGenreId = classicalGenreConfig.classicalGenreId
        isClassical = genreId == classicalGenreId

        subGenres = if (genreId == classicalGenreId) {
            genreRepository.getSubGenresForAlbumArtist(genreId, albumArtistId)
        } else {
            flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        genreName = genreRepository.getGenreById(genreId)
            .map { it?.name ?: DEFAULT_GENRE_NAME }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_GENRE_NAME)

        val albums: StateFlow<List<Album>> = _selectedSubGenreId
            .flatMapLatest { selectedSubGenreId ->
                val effectiveGenreId = selectedSubGenreId ?: genreId
                albumRepository.getAlbumsForArtistInGenre(albumArtistId, effectiveGenreId, isClassical)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val artistName = albumArtistRepository.getAlbumArtistName(albumArtistId)

        val subGenreName = _selectedSubGenreId.mapLatest { selectedSubGenreId ->
            if (selectedSubGenreId != null) {
                genreRepository.getGenreName(selectedSubGenreId)
            } else {
                null
            }
        }


        uiState = combine(albums, artistName, subGenreName, _selectedSubGenreId) { albums, artistName, subGenreName, selectedSubGenreId ->
            val screenTitle = if (selectedSubGenreId == null) artistName else "$artistName - $subGenreName"

            AlbumsScreenUiState(
                screenTitle ?: "Albums",
                albums,
                false,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AlbumsScreenUiState(isLoading = true),
        )

        entityHeaderState = combine(
            albumArtistRepository.getAlbumArtistById(albumArtistId),
            genreRepository.getGenreById(genreId),
            albumRepository.getNumberOfAlbumsForAlbumArtistInGenre(albumArtistId, genreId),
            composerRepository.getComposerForAlbumArtist(albumArtistId),
            playlistRepository.getPlaylistIdsContainingAlbumArtist(albumArtistId),
        ) { albumArtist, genre, numberOfAlbums, composer, playlistsThatAlbumArtistIsAlreadyIn ->
            val albumsLabel = chooseAlbumLabel(isClassical)

            if (composer != null) {
                val lifespan = listOfNotNull(composer.birthYear, composer.deathYear)
                    .joinToString("–")
                val subtitle = listOfNotNull(composer.epoch, lifespan.ifEmpty { null })
                    .joinToString(" - ")

                EntityHeaderState(
                    composer.completeName,
                    subtitle,
                    "$numberOfAlbums $albumsLabel",
                    composer.portraitPath,
                    false,
                    playlistsThatAlbumArtistIsAlreadyIn,
                )
            } else {
                EntityHeaderState(
                    albumArtist?.name ?: "Artist",
                    genre?.name ?: DEFAULT_GENRE_NAME,
                    "$numberOfAlbums $albumsLabel",
                    albumArtist?.portraitPath,
                    false,
                    playlistsThatAlbumArtistIsAlreadyIn,
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            EntityHeaderState(),
        )

    }

    fun updateSelectedSubGenreId(subGenreId: Long?) {
        _selectedSubGenreId.value = subGenreId
    }
}
