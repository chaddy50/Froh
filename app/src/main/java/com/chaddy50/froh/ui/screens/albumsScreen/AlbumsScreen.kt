package com.chaddy50.froh.ui.screens.albumsScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.PerformancesRoute
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.navigation.TracksRoute
import com.chaddy50.froh.ui.composables.AddToPlaylistHandler
import com.chaddy50.froh.ui.composables.EntityCard
import com.chaddy50.froh.ui.composables.EntityScreen
import com.chaddy50.froh.ui.composables.SubGenreFilterButton
import com.chaddy50.froh.ui.composables.entityHeader.EntityHeader
import com.chaddy50.froh.ui.composables.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.composables.rememberAddToPlaylistState
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel

@Composable
fun AlbumsScreen(
    genreId: Long,
    albumArtistId: Long,
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    appNavigator: AppNavigator,
    onTopBarContentChanged: (TopBarContent) -> Unit = {},
    screenViewModel: AlbumsScreenViewModel,
) {
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
    val entityHeaderState by screenViewModel.entityHeaderState.collectAsStateWithLifecycle()
    val allPlaylists by playlistViewModel.allPlaylists.collectAsStateWithLifecycle()
    val isClassical = screenViewModel.isClassical
    val subGenres by screenViewModel.subGenres.collectAsStateWithLifecycle()
    val selectedSubGenreId by screenViewModel.selectedSubGenreId.collectAsStateWithLifecycle()
    val effectiveGenreId = selectedSubGenreId ?: screenViewModel.genreId

    LaunchedEffect(uiState.screenTitle, subGenres, selectedSubGenreId) {
        onTopBarContentChanged(
            TopBarContent(
                title = uiState.screenTitle,
                actions = {
                    if (subGenres.size > 1) {
                        SubGenreFilterButton(
                            subGenres = subGenres,
                            selectedSubGenreId = selectedSubGenreId,
                            onSubGenreSelected = { screenViewModel.updateSelectedSubGenreId(it) },
                        )
                    }
                },
            )
        )
    }

    val addToPlaylistState = rememberAddToPlaylistState<Album>(
        getPlaylistMembership = { album -> playlistViewModel.getPlaylistsThatAlbumIsAlreadyIn(album.id) },
        onAdd = { playlistId, album -> playlistViewModel.addAlbumToPlaylist(playlistId, album.id) },
        onCreateAndAdd = { name, album -> playlistViewModel.createPlaylistAndAddAlbum(name, album.id) },
    )

    EntityScreen(
        uiState.isLoading,
        {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    EntityHeader(
                        uiState = entityHeaderState,
                        type = EntityType.AlbumArtist,
                        allPlaylists = allPlaylists,
                        onAddToPlaylist = { playlistId -> playlistViewModel.addAlbumArtistToPlaylist(playlistId, albumArtistId) },
                        onCreateAndAdd = { name -> playlistViewModel.createPlaylistAndAddAlbumArtist(name, albumArtistId) },
                    )
                }

                items(uiState.albums) { album ->
                    EntityCard(
                        title = album.title,
                        onClick = {
                            if (isClassical) {
                                appNavigator.push(PerformancesRoute(genreId = genreId, albumId = album.id, title = album.title))
                            } else {
                                appNavigator.push(TracksRoute(genreId = genreId, albumId = album.id, title = album.title))
                            }
                        },
                        onLongClick = { addToPlaylistState.show(album) },
                        artworkPath = if (!isClassical) album.artworkPath else null,
                        subtitle = if (isClassical) album.catalogueString else album.year,
                    )
                }
            }

        },
        onPlay = if (uiState.albums.isNotEmpty()) {{ playbackViewModel.playTracksForAlbumArtist(albumArtistId, effectiveGenreId, false) }} else null,
        onShuffle = if (uiState.albums.isNotEmpty()) {{ playbackViewModel.playTracksForAlbumArtist(albumArtistId, effectiveGenreId, true) }} else null,
    )

    AddToPlaylistHandler(state = addToPlaylistState, allPlaylists = allPlaylists)
}
