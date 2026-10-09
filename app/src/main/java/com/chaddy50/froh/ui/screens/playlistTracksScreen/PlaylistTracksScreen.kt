package com.chaddy50.froh.ui.screens.playlistTracksScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.ui.composables.common.EntityScreen
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutCompact
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.ui.screens.tracksScreen.TrackCard
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel

@Composable
fun PlaylistTracksScreen(
    playlistId: Long,
    title: String,
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    onTopBarContentChanged: (TopBarContent) -> Unit = {},
    screenViewModel: PlaylistTracksScreenViewModel,
) {
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
    val entityHeaderState by screenViewModel.entityHeaderState.collectAsStateWithLifecycle()
    val currentTrack by playbackViewModel.nowPlayingState.currentTrack.collectAsStateWithLifecycle()

    LaunchedEffect(title) {
        onTopBarContentChanged(TopBarContent(title = title))
    }

    var trackWithMenu by remember { mutableStateOf<Track?>(null) }

    EntityScreen(
        isLoading = uiState.isLoading,
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = if (LocalWindowWidthSizeClass.current == WindowWidthSizeClass.EXPANDED) PaddingValues(bottom = 120.dp) else PaddingValues(),
            ) {
                item {
                    EntityHeaderLayoutCompact(
                        uiState = entityHeaderState,
                        type = EntityType.Playlist,
                        onRename = { newName ->
                            uiState.playlist?.let { playlist ->
                                playlistViewModel.renamePlaylist(playlist, newName)
                            }
                        },
                    )
                }
                items(uiState.tracks) { track ->
                    Box {
                        TrackCard(
                            track = track,
                            isCurrentlyPlaying = currentTrack?.mediaId == track.id.toString(),
                            onTrackClicked = { playbackViewModel.playTrack(track, uiState.tracks) },
                            onTrackLongPressed = { trackWithMenu = track },
                            showTrackNumber = false,
                        )
                        DropdownMenu(
                            expanded = trackWithMenu?.id == track.id,
                            onDismissRequest = { trackWithMenu = null },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remove from playlist") },
                                onClick = {
                                    playlistViewModel.removeTrackFromPlaylist(playlistId, track.id)
                                    trackWithMenu = null
                                },
                            )
                        }
                    }
                }
            }
        },
        onPlay = if (uiState.tracks.isNotEmpty()) {{ playbackViewModel.playTracksForPlaylist(playlistId, false) }} else null,
        onShuffle = if (uiState.tracks.isNotEmpty()) {{ playbackViewModel.playTracksForPlaylist(playlistId, true) }} else null,
    )
}
