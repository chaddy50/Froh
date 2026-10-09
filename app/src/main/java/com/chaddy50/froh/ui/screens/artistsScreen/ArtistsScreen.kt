package com.chaddy50.froh.ui.screens.artistsScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.navigation.AlbumsRoute
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.ui.composables.common.AddToPlaylistHandler
import com.chaddy50.froh.ui.composables.common.EntityScreen
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.composables.common.rememberAddToPlaylistState
import com.chaddy50.froh.ui.screens.artistsScreen.layouts.ArtistsScreenLayoutCompact
import com.chaddy50.froh.ui.screens.artistsScreen.layouts.ArtistsScreenLayoutExpanded
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

    val onPlay = if (uiState.artists.isNotEmpty()) {{ playbackViewModel.playTracksForGenre(genreId, false) }} else null
    val onShuffle = if (uiState.artists.isNotEmpty()) {{ playbackViewModel.playTracksForGenre(genreId, true) }} else null
    val onAddToPlaylist: (Long) -> Unit = { playlistId -> playlistViewModel.addGenreToPlaylist(playlistId, genreId) }
    val onCreateAndAdd: (String) -> Unit = { name -> playlistViewModel.createPlaylistAndAddGenre(name, genreId) }
    val onArtistClick: (AlbumArtist) -> Unit = { artist ->
        appNavigator.push(AlbumsRoute(genreId = genreId, albumArtistId = artist.id, title = artist.name))
    }

    EntityScreen(
        uiState.isLoading,
        {
            when (LocalWindowWidthSizeClass.current) {
                WindowWidthSizeClass.EXPANDED -> ArtistsScreenLayoutExpanded(
                    entityHeaderState = entityHeaderState,
                    allPlaylists = allPlaylists,
                    onAddToPlaylist = onAddToPlaylist,
                    onCreateAndAdd = onCreateAndAdd,
                    onPlay = onPlay,
                    onShuffle = onShuffle,
                    artists = uiState.artists,
                    onArtistClick = onArtistClick,
                    addToPlaylistState = addToPlaylistState,
                )
                WindowWidthSizeClass.MEDIUM, WindowWidthSizeClass.COMPACT -> ArtistsScreenLayoutCompact(
                    entityHeaderState = entityHeaderState,
                    allPlaylists = allPlaylists,
                    onAddToPlaylist = onAddToPlaylist,
                    onCreateAndAdd = onCreateAndAdd,
                    artists = uiState.artists,
                    onArtistClick = onArtistClick,
                    addToPlaylistState = addToPlaylistState,
                )
            }
        },
        onPlay = onPlay,
        onShuffle = onShuffle,
    )

    AddToPlaylistHandler(state = addToPlaylistState, allPlaylists = allPlaylists)
}
