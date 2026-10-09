package com.chaddy50.froh.ui.screens.tracksScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.ui.composables.common.AddToPlaylistHandler
import com.chaddy50.froh.ui.composables.common.EntityScreen
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.composables.common.rememberAddToPlaylistState
import com.chaddy50.froh.ui.screens.tracksScreen.layouts.TracksScreenLayoutCompact
import com.chaddy50.froh.ui.screens.tracksScreen.layouts.TracksScreenLayoutExpanded
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TracksScreen(
    genreId: Long,
    albumId: Long,
    performanceId: Long?,
    title: String,
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    appNavigator: AppNavigator,
    onTopBarContentChanged: (TopBarContent) -> Unit = {},
    screenViewModel: TracksScreenViewModel,
) {
    val currentTrack by playbackViewModel.nowPlayingState.currentTrack.collectAsStateWithLifecycle()
    val entityHeaderState by screenViewModel.entityHeaderState.collectAsStateWithLifecycle()
    val allPlaylists by playlistViewModel.allPlaylists.collectAsStateWithLifecycle()
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(title) {
        onTopBarContentChanged(TopBarContent(title = title))
    }

    val addToPlaylistState = rememberAddToPlaylistState<Track>(
        getPlaylistMembership = { track -> playlistViewModel.getPlaylistsThatTrackIsAlreadyIn(track.id) },
        onAdd = { playlistId, track -> playlistViewModel.addTrackToPlaylist(playlistId, track) },
        onCreateAndAdd = { name, track -> playlistViewModel.createPlaylistAndAddTrack(name, track) },
    )

    val groupedTracks = uiState.tracks.groupBy { it.discNumber }
    val doesAlbumHaveMultipleDiscs = groupedTracks.size > 1
    val onAddToPlaylist: (Long) -> Unit = { playlistId -> playlistViewModel.addAlbumToPlaylist(playlistId, albumId) }
    val onCreateAndAdd: (String) -> Unit = { name -> playlistViewModel.createPlaylistAndAddAlbum(name, albumId) }
    val onPlay = if (uiState.tracks.isNotEmpty()) {{ playbackViewModel.playTracksForAlbum(albumId, performanceId, false) }} else null
    val onShuffle = if (uiState.tracks.isNotEmpty()) {{ playbackViewModel.playTracksForAlbum(albumId, performanceId, true) }} else null

    val trackListItems: LazyListScope.() -> Unit = {
        groupedTracks.forEach { (discNumber, tracks) ->
            if (doesAlbumHaveMultipleDiscs && (discNumber > 0)) {
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Disc $discNumber",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
            items(tracks) { track ->
                TrackCard(
                    track,
                    currentTrack?.mediaId == track.id.toString(),
                    { playbackViewModel.playTrack(track, uiState.tracks) },
                    onTrackLongPressed = { addToPlaylistState.show(it) },
                    showTrackNumber = !uiState.isClassical,
                )
            }
        }
    }

    EntityScreen(
        uiState.isLoading,
        {
            when (LocalWindowWidthSizeClass.current) {
                WindowWidthSizeClass.EXPANDED -> TracksScreenLayoutExpanded(
                    albumArtistName = uiState.albumArtist?.name ?: "Unknown Artist",
                    appNavigator = appNavigator,
                    entityHeaderState = entityHeaderState,
                    allPlaylists = allPlaylists,
                    onAddToPlaylist = onAddToPlaylist,
                    onCreateAndAdd = onCreateAndAdd,
                    onPlay = onPlay,
                    onShuffle = onShuffle,
                    trackListItems = trackListItems,
                )
                WindowWidthSizeClass.MEDIUM, WindowWidthSizeClass.COMPACT -> TracksScreenLayoutCompact(
                    entityHeaderState = entityHeaderState,
                    allPlaylists = allPlaylists,
                    onAddToPlaylist = onAddToPlaylist,
                    onCreateAndAdd = onCreateAndAdd,
                    trackListItems = trackListItems,
                )
            }
        },
        onPlay = onPlay,
        onShuffle = onShuffle,
    )

    AddToPlaylistHandler(state = addToPlaylistState, allPlaylists = allPlaylists)
}
