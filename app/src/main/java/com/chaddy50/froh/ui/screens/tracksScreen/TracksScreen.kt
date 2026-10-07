package com.chaddy50.froh.ui.screens.tracksScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.ui.composables.AddToPlaylistHandler
import com.chaddy50.froh.ui.composables.EntityScreen
import com.chaddy50.froh.ui.composables.entityHeader.EntityHeader
import com.chaddy50.froh.ui.composables.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.composables.rememberAddToPlaylistState
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
        },
        onPlay = if (uiState.tracks.isNotEmpty()) {{ playbackViewModel.playTracksForAlbum(albumId, performanceId, false) }} else null,
        onShuffle = if (uiState.tracks.isNotEmpty()) {{ playbackViewModel.playTracksForAlbum(albumId, performanceId, true) }} else null,
    )

    AddToPlaylistHandler(state = addToPlaylistState, allPlaylists = allPlaylists)
}
