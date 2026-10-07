package com.chaddy50.froh.ui.screens.performancesScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.navigation.TracksRoute
import com.chaddy50.froh.ui.composables.EntityCard
import com.chaddy50.froh.ui.composables.EntityScreen
import com.chaddy50.froh.ui.composables.entityHeader.EntityHeader
import com.chaddy50.froh.ui.composables.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel

@Composable
fun PerformancesScreen(
    genreId: Long,
    albumId: Long,
    title: String,
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    appNavigator: AppNavigator,
    onTopBarContentChanged: (TopBarContent) -> Unit = {},
    screenViewModel: PerformancesScreenViewModel,
) {
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
    val entityHeaderState by screenViewModel.entityHeaderState.collectAsStateWithLifecycle()
    val allPlaylists by playlistViewModel.allPlaylists.collectAsStateWithLifecycle()

    LaunchedEffect(title) {
        onTopBarContentChanged(TopBarContent(title = title))
    }

    EntityScreen(
        uiState.isLoading,
        {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    EntityHeader(
                        uiState = entityHeaderState,
                        type = EntityType.Album,
                        allPlaylists = allPlaylists,
                        onAddToPlaylist = { playlistId -> playlistViewModel.addAlbumToPlaylist(playlistId, albumId) },
                        onCreateAndAdd = { name -> playlistViewModel.createPlaylistAndAddAlbum(name, albumId) },
                    )
                }

                items(uiState.performances) { performance ->
                    EntityCard(
                        title = performance.artistName,
                        onClick = {
                            appNavigator.push(TracksRoute(genreId = genreId, albumId = albumId, performanceId = performance.id, title = uiState.screenTitle))
                        },
                        subtitle = performance.year,
                    )
                }
            }
        },
        onPlay = if (uiState.performances.isNotEmpty()) {{ playbackViewModel.playTracksForAlbumInGenre(albumId, screenViewModel.genreId, null, false) }} else null,
        onShuffle = if (uiState.performances.isNotEmpty()) {{ playbackViewModel.playTracksForAlbumInGenre(albumId, screenViewModel.genreId, null, true) }} else null,
    )
}
