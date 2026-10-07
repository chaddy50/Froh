package com.chaddy50.froh.ui.screens.artistsScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.navigation.AlbumsRoute
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.ui.composables.AddToPlaylistHandler
import com.chaddy50.froh.ui.composables.EntityCard
import com.chaddy50.froh.ui.composables.EntityScreen
import com.chaddy50.froh.ui.composables.entityHeader.EntityHeader
import com.chaddy50.froh.ui.composables.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.composables.rememberAddToPlaylistState
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel

@Composable
fun ArtistsScreen(
    genreId: Long,
    title: String,
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    appNavigator: AppNavigator,
    onTopBarContentChanged: (TopBarContent) -> Unit = {},
    screenViewModel: ArtistsScreenViewModel,
) {
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
    val entityHeaderState by screenViewModel.entityHeaderState.collectAsStateWithLifecycle()
    val allPlaylists by playlistViewModel.allPlaylists.collectAsStateWithLifecycle()

    LaunchedEffect(title) {
        onTopBarContentChanged(TopBarContent(title = title))
    }

    val addToPlaylistState = rememberAddToPlaylistState<AlbumArtist>(
        getPlaylistMembership = { artist -> playlistViewModel.getPlaylistsThatAlbumArtistIsAlreadyIn(artist.id) },
        onAdd = { playlistId, artist -> playlistViewModel.addAlbumArtistToPlaylist(playlistId, artist.id) },
        onCreateAndAdd = { name, artist -> playlistViewModel.createPlaylistAndAddAlbumArtist(name, artist.id) },
    )

    EntityScreen(
        uiState.isLoading,
        {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    EntityHeader(
                        uiState = entityHeaderState,
                        type = EntityType.Genre,
                        allPlaylists = allPlaylists,
                        onAddToPlaylist = { playlistId -> playlistViewModel.addGenreToPlaylist(playlistId, genreId) },
                        onCreateAndAdd = { name -> playlistViewModel.createPlaylistAndAddGenre(name, genreId) },
                    )
                }

                items(uiState.artists) { artistWithSubtitle ->
                    EntityCard(
                        artistWithSubtitle.artist.name,
                        onClick = {
                            appNavigator.push(AlbumsRoute(genreId = genreId, albumArtistId = artistWithSubtitle.artist.id, title = artistWithSubtitle.artist.name))
                        },
                        onLongClick = { addToPlaylistState.show(artistWithSubtitle.artist) },
                        artworkPath = artistWithSubtitle.artist.portraitPath,
                        subtitle = artistWithSubtitle.subtitle,
                    )
                }
            }

        },
        onPlay = if (uiState.artists.isNotEmpty()) {{ playbackViewModel.playTracksForGenre(genreId, false) }} else null,
        onShuffle = if (uiState.artists.isNotEmpty()) {{ playbackViewModel.playTracksForGenre(genreId, true) }} else null,
    )

    AddToPlaylistHandler(state = addToPlaylistState, allPlaylists = allPlaylists)
}
