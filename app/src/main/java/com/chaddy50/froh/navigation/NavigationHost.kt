package com.chaddy50.froh.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.chaddy50.froh.ui.composables.MusicScannerProgressBar
import com.chaddy50.froh.ui.composables.TopBar
import com.chaddy50.froh.ui.composables.nowPlayingBar.NowPlayingBar
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.NowPlayingSheet
import com.chaddy50.froh.ui.screens.HomeScreen
import com.chaddy50.froh.ui.screens.albumsScreen.AlbumsScreen
import com.chaddy50.froh.ui.screens.albumsScreen.AlbumsScreenViewModel
import com.chaddy50.froh.ui.screens.artistsScreen.ArtistsScreen
import com.chaddy50.froh.ui.screens.artistsScreen.ArtistsScreenViewModel
import com.chaddy50.froh.ui.screens.performancesScreen.PerformancesScreen
import com.chaddy50.froh.ui.screens.performancesScreen.PerformancesScreenViewModel
import com.chaddy50.froh.ui.screens.playlistTracksScreen.PlaylistTracksScreen
import com.chaddy50.froh.ui.screens.playlistTracksScreen.PlaylistTracksScreenViewModel
import com.chaddy50.froh.ui.screens.tracksScreen.TracksScreen
import com.chaddy50.froh.ui.screens.tracksScreen.TracksScreenViewModel
import com.chaddy50.froh.data.scanner.LibraryScanViewModel
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import com.chaddy50.froh.ui.screens.settingsScreen.SettingsScreen
import com.chaddy50.froh.ui.screens.settingsScreen.genreMappings.ClassicalGenreSettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationHost(
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    libraryScanViewModel: LibraryScanViewModel,
    appNavigator: AppNavigator = rememberAppNavigator(HomeRoute),
) {
    var shouldShowNowPlayingSheet by remember { mutableStateOf(false) }
    var homeTopBarContent by remember { mutableStateOf(TopBarContent(title = "Library")) }
    var albumsTopBarContent by remember { mutableStateOf(TopBarContent(title = "")) }
    var screenTopBarContent by remember { mutableStateOf(TopBarContent(title = "")) }
    val currentTrack by playbackViewModel.nowPlayingState.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by playbackViewModel.nowPlayingState.isPlaying.collectAsStateWithLifecycle()
    val playbackPosition by playbackViewModel.nowPlayingState.playbackPosition.collectAsStateWithLifecycle()
    val durationMs = currentTrack?.mediaMetadata?.durationMs ?: 0
    val isShuffleModeEnabled by playbackViewModel.nowPlayingState.isShuffleModeEnabled.collectAsStateWithLifecycle()
    val queue by playbackViewModel.nowPlayingState.queue.collectAsStateWithLifecycle()
    val currentTrackIndex by playbackViewModel.nowPlayingState.currentTrackIndex.collectAsStateWithLifecycle()
    val onPlayPause = { playbackViewModel.nowPlayingState.playOrPause() }
    val onSkipToTrack = { index: Int -> playbackViewModel.nowPlayingState.skipToTrack(index) }
    val onSkipToPreviousTrack = { playbackViewModel.nowPlayingState.skipPrevious() }
    val onSkipToNextTrack = { playbackViewModel.nowPlayingState.skipNext() }
    val onShuffleToggled = { playbackViewModel.nowPlayingState.toggleShuffleMode() }
    val onSeek = { positionMs: Long -> playbackViewModel.nowPlayingState.seekTo(positionMs) }

    val isScanInProgress by libraryScanViewModel.isScanInProgress.collectAsStateWithLifecycle()
    val scanProgress by libraryScanViewModel.scanProgress.collectAsStateWithLifecycle()

    val currentKey = appNavigator.currentKey
    val isOnAlbumsRoute = currentKey is AlbumsRoute
    val isOnHomeRoute = currentKey is HomeRoute
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val topBarTitle = when {
        currentKey == null -> ""
        currentKey is HomeRoute -> homeTopBarContent.title
        currentKey is AlbumsRoute -> albumsTopBarContent.title
        else -> screenTopBarContent.title
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    topBarTitle,
                    appNavigator,
                    scrollBehavior = scrollBehavior,
                    actions = {
                        if (isOnAlbumsRoute) {
                            albumsTopBarContent.actions(this)
                        }
                        if (isOnHomeRoute) {
                            homeTopBarContent.actions(this)
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    MusicScannerProgressBar(isScanInProgress, scanProgress)
                    NowPlayingBar(
                        currentTrack,
                        isPlaying,
                        playbackPosition,
                        durationMs,
                        isShuffleModeEnabled,
                        onPlayPause,
                        onSkipToNextTrack,
                        onShuffleToggled,
                        { shouldShowNowPlayingSheet = true },
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) { innerPadding ->
            NavDisplay(
                backStack = appNavigator.backStack,
                onBack = { appNavigator.pop() },
                modifier = Modifier
                    .padding(innerPadding)
                    .imePadding(),
                entryProvider = entryProvider {
                    entry<HomeRoute> {
                        HomeScreen(
                            playbackViewModel = playbackViewModel,
                            playlistViewModel = playlistViewModel,
                            appNavigator = appNavigator,
                            onTopBarContentChanged = { homeTopBarContent = it },
                        )
                    }
                    entry<ArtistsRoute> { route ->
                        val screenViewModel = hiltViewModel<ArtistsScreenViewModel, ArtistsScreenViewModel.Factory>(
                            creationCallback = { factory -> factory.create(route) }
                        )
                        ArtistsScreen(
                            genreId = route.genreId,
                            title = route.title,
                            playbackViewModel = playbackViewModel,
                            playlistViewModel = playlistViewModel,
                            appNavigator = appNavigator,
                            screenViewModel = screenViewModel,
                            onTopBarContentChanged = { screenTopBarContent = it },
                        )
                    }
                    entry<AlbumsRoute> { route ->
                        val screenViewModel = hiltViewModel<AlbumsScreenViewModel, AlbumsScreenViewModel.Factory>(
                            creationCallback = { factory -> factory.create(route) }
                        )
                        AlbumsScreen(
                            genreId = route.genreId,
                            albumArtistId = route.albumArtistId,
                            playbackViewModel = playbackViewModel,
                            playlistViewModel = playlistViewModel,
                            appNavigator = appNavigator,
                            screenViewModel = screenViewModel,
                            onTopBarContentChanged = { albumsTopBarContent = it },
                        )
                    }
                    entry<PerformancesRoute> { route ->
                        val screenViewModel = hiltViewModel<PerformancesScreenViewModel, PerformancesScreenViewModel.Factory>(
                            creationCallback = { factory -> factory.create(route) }
                        )
                        PerformancesScreen(
                            genreId = route.genreId,
                            albumId = route.albumId,
                            title = route.title,
                            playbackViewModel = playbackViewModel,
                            playlistViewModel = playlistViewModel,
                            appNavigator = appNavigator,
                            screenViewModel = screenViewModel,
                            onTopBarContentChanged = { screenTopBarContent = it },
                        )
                    }
                    entry<TracksRoute> { route ->
                        val screenViewModel = hiltViewModel<TracksScreenViewModel, TracksScreenViewModel.Factory>(
                            creationCallback = { factory -> factory.create(route) }
                        )
                        TracksScreen(
                            genreId = route.genreId,
                            albumId = route.albumId,
                            performanceId = if (route.performanceId == -1L) null else route.performanceId,
                            title = route.title,
                            playbackViewModel = playbackViewModel,
                            playlistViewModel = playlistViewModel,
                            screenViewModel = screenViewModel,
                            onTopBarContentChanged = { screenTopBarContent = it },
                        )
                    }
                    entry<PlaylistTracksRoute> { route ->
                        val screenViewModel = hiltViewModel<PlaylistTracksScreenViewModel, PlaylistTracksScreenViewModel.Factory>(
                            creationCallback = { factory -> factory.create(route) }
                        )
                        PlaylistTracksScreen(
                            playlistId = route.playlistId,
                            title = route.title,
                            playbackViewModel = playbackViewModel,
                            playlistViewModel = playlistViewModel,
                            screenViewModel = screenViewModel,
                            onTopBarContentChanged = { screenTopBarContent = it },
                        )
                    }
                    entry<SettingsRoute> {
                        SettingsScreen(
                            appNavigator = appNavigator,
                            onTopBarContentChanged = { screenTopBarContent = it },
                        )
                    }
                    entry<ClassicalGenreSettingsRoute> {
                        ClassicalGenreSettingsScreen(
                            onNavigateBack = { appNavigator.pop() },
                            onRebuildLibrary = { libraryScanViewModel.rebuildLibrary() },
                            onTopBarContentChanged = { screenTopBarContent = it },
                        )
                    }
                },
            )
        }

        if (shouldShowNowPlayingSheet) {
            NowPlayingSheet(
                currentTrack,
                isPlaying,
                playbackPosition,
                durationMs,
                isShuffleModeEnabled,
                queue,
                currentTrackIndex,
                onShuffleToggled,
                onPlayPause,
                onSkipToPreviousTrack,
                onSkipToNextTrack,
                onSkipToTrack,
                onSeek,
                { shouldShowNowPlayingSheet = false }
            )
        }
    }
}
