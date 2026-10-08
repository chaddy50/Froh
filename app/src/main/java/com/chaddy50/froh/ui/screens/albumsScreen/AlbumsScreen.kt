package com.chaddy50.froh.ui.screens.albumsScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.PerformancesRoute
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.navigation.TracksRoute
import com.chaddy50.froh.ui.composables.common.AddToPlaylistHandler
import com.chaddy50.froh.ui.composables.compact.SubGenreFilterButton
import com.chaddy50.froh.ui.composables.common.EntityScreen
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.composables.common.rememberAddToPlaylistState
import com.chaddy50.froh.ui.screens.albumsScreen.layouts.AlbumsScreenLayoutCompact
import com.chaddy50.froh.ui.screens.albumsScreen.layouts.AlbumsScreenLayoutExpanded
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
    val genreName by screenViewModel.genreName.collectAsStateWithLifecycle()
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

    val onPlay = if (uiState.albums.isNotEmpty()) {{ playbackViewModel.playTracksForAlbumArtist(albumArtistId, effectiveGenreId, false) }} else null
    val onShuffle = if (uiState.albums.isNotEmpty()) {{ playbackViewModel.playTracksForAlbumArtist(albumArtistId, effectiveGenreId, true) }} else null
    val onAddToPlaylist: (Long) -> Unit = { playlistId -> playlistViewModel.addAlbumArtistToPlaylist(playlistId, albumArtistId) }
    val onCreateAndAdd: (String) -> Unit = { name -> playlistViewModel.createPlaylistAndAddAlbumArtist(name, albumArtistId) }
    val onAlbumClick: (Album) -> Unit = { album ->
        if (isClassical) {
            appNavigator.push(PerformancesRoute(genreId = genreId, albumId = album.id, title = album.title))
        } else {
            appNavigator.push(TracksRoute(genreId = genreId, albumId = album.id, title = album.title))
        }
    }

    EntityScreen(
        uiState.isLoading,
        {
            when (LocalWindowWidthSizeClass.current) {
                WindowWidthSizeClass.EXPANDED -> AlbumsScreenLayoutExpanded(
                    genreName = genreName,
                    appNavigator = appNavigator,
                    entityHeaderState = entityHeaderState,
                    allPlaylists = allPlaylists,
                    onAddToPlaylist = onAddToPlaylist,
                    onCreateAndAdd = onCreateAndAdd,
                    onPlay = onPlay,
                    onShuffle = onShuffle,
                    subGenres = subGenres,
                    selectedSubGenreId = selectedSubGenreId,
                    onSubGenreSelected = { screenViewModel.updateSelectedSubGenreId(it) },
                    isClassical = isClassical,
                    albums = uiState.albums,
                    onAlbumClick = onAlbumClick,
                    addToPlaylistState = addToPlaylistState,
                )
                WindowWidthSizeClass.MEDIUM, WindowWidthSizeClass.COMPACT -> AlbumsScreenLayoutCompact(
                    entityHeaderState = entityHeaderState,
                    allPlaylists = allPlaylists,
                    onAddToPlaylist = onAddToPlaylist,
                    onCreateAndAdd = onCreateAndAdd,
                    albums = uiState.albums,
                    isClassical = isClassical,
                    onAlbumClick = onAlbumClick,
                    addToPlaylistState = addToPlaylistState,
                )
            }
        },
        onPlay = onPlay,
        onShuffle = onShuffle,
    )

    AddToPlaylistHandler(state = addToPlaylistState, allPlaylists = allPlaylists)
}
