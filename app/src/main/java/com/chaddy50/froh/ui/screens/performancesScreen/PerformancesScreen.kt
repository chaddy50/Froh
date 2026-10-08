package com.chaddy50.froh.ui.screens.performancesScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.navigation.TracksRoute
import com.chaddy50.froh.ui.composables.common.EntityScreen
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.performancesScreen.layouts.PerformancesScreenLayoutCompact
import com.chaddy50.froh.ui.screens.performancesScreen.layouts.PerformancesScreenLayoutExpanded
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

    val onAddToPlaylist: (Long) -> Unit = { playlistId -> playlistViewModel.addAlbumToPlaylist(playlistId, albumId) }
    val onCreateAndAdd: (String) -> Unit = { name -> playlistViewModel.createPlaylistAndAddAlbum(name, albumId) }
    val onPlay = if (uiState.performances.isNotEmpty()) {{ playbackViewModel.playTracksForAlbumInGenre(albumId, screenViewModel.genreId, null, false) }} else null
    val onShuffle = if (uiState.performances.isNotEmpty()) {{ playbackViewModel.playTracksForAlbumInGenre(albumId, screenViewModel.genreId, null, true) }} else null
    val onPerformanceClick: (Performance) -> Unit = { performance ->
        appNavigator.push(TracksRoute(genreId = genreId, albumId = albumId, performanceId = performance.id, title = uiState.screenTitle))
    }

    EntityScreen(
        uiState.isLoading,
        {
            when (LocalWindowWidthSizeClass.current) {
                WindowWidthSizeClass.EXPANDED -> PerformancesScreenLayoutExpanded(
                    albumArtistName = uiState.albumArtist?.name ?: "Unknown Artist",
                    appNavigator = appNavigator,
                    entityHeaderState = entityHeaderState,
                    allPlaylists = allPlaylists,
                    onAddToPlaylist = onAddToPlaylist,
                    onCreateAndAdd = onCreateAndAdd,
                    onPlay = onPlay,
                    onShuffle = onShuffle,
                    performances = uiState.performances,
                    onPerformanceClick = onPerformanceClick,
                )
                WindowWidthSizeClass.MEDIUM, WindowWidthSizeClass.COMPACT -> PerformancesScreenLayoutCompact(
                    entityHeaderState = entityHeaderState,
                    allPlaylists = allPlaylists,
                    onAddToPlaylist = onAddToPlaylist,
                    onCreateAndAdd = onCreateAndAdd,
                    performances = uiState.performances,
                    onPerformanceClick = onPerformanceClick,
                )
            }
        },
        onPlay = onPlay,
        onShuffle = onShuffle,
    )
}
