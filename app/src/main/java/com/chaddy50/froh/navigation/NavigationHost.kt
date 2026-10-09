package com.chaddy50.froh.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.chaddy50.froh.ui.composables.common.MusicScannerProgressBar
import com.chaddy50.froh.ui.composables.compact.TopBar
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.layouts.MiniPlayerCompact
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.layouts.MiniPlayerExpanded
import com.chaddy50.froh.ui.composables.expanded.ContentPane
import com.chaddy50.froh.ui.composables.expanded.NavigationDrawer
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
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
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
    val windowWidthSizeClass = rememberWindowWidthSizeClass()
    val isWideLayout = windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
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
    val isQueueHidden by playbackViewModel.isQueueHidden.collectAsStateWithLifecycle()
    val onQueueHiddenToggled = { playbackViewModel.toggleQueueHidden() }

    val isScanInProgress by libraryScanViewModel.isScanInProgress.collectAsStateWithLifecycle()
    val scanProgress by libraryScanViewModel.scanProgress.collectAsStateWithLifecycle()

    val currentKey = appNavigator.currentKey
    val isOnAlbumsRoute = currentKey is AlbumsRoute
    val isOnHomeRoute = currentKey is HomeRoute
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val topBarTitle = when (currentKey) {
        is HomeRoute -> homeTopBarContent.title
        is AlbumsRoute -> albumsTopBarContent.title
        else -> screenTopBarContent.title
    }

    CompositionLocalProvider(LocalWindowWidthSizeClass provides windowWidthSizeClass) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            if (isWideLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationDrawer(
                        playlistViewModel = playlistViewModel,
                        appNavigator = appNavigator,
                    )
                    ContentPane(
                        isScanInProgress = isScanInProgress,
                        scanProgress = scanProgress,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        NavigationEntryDisplay(
                            modifier = Modifier.fillMaxSize(),
                            appNavigator = appNavigator,
                            libraryScanViewModel = libraryScanViewModel,
                            playbackViewModel = playbackViewModel,
                            playlistViewModel = playlistViewModel,
                            onHomeTopBarContentChanged = { homeTopBarContent = it },
                            onAlbumsTopBarContentChanged = { albumsTopBarContent = it },
                            onScreenTopBarContentChanged = { screenTopBarContent = it },
                        )
                        currentTrack?.let { track ->
                            MiniPlayerExpanded(
                                currentTrack = track,
                                isPlaying = isPlaying,
                                playbackPosition = playbackPosition,
                                durationMs = durationMs,
                                onPlayPause = onPlayPause,
                                onSkipToNextTrack = onSkipToNextTrack,
                                onSkipToPreviousTrack = onSkipToPreviousTrack,
                                onExpand = { shouldShowNowPlayingSheet = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(start = 32.dp, end = 32.dp, bottom = 24.dp),
                            )
                        }
                    }
                }
            } else {
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
                            currentTrack?.let { track ->
                                MiniPlayerCompact(
                                    track,
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
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) { innerPadding ->
                    NavigationEntryDisplay(
                        modifier = Modifier
                            .padding(innerPadding)
                            .imePadding(),
                        appNavigator = appNavigator,
                        libraryScanViewModel = libraryScanViewModel,
                        playbackViewModel = playbackViewModel,
                        playlistViewModel = playlistViewModel,
                        onHomeTopBarContentChanged = { homeTopBarContent = it },
                        onAlbumsTopBarContentChanged = { albumsTopBarContent = it },
                        onScreenTopBarContentChanged = { screenTopBarContent = it },
                    )
                }
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
                    isQueueHidden,
                    onQueueHiddenToggled,
                    { shouldShowNowPlayingSheet = false }
                )
            }
        }
    }
}

@Composable
private fun NavigationEntryDisplay(
    modifier: Modifier,
    appNavigator: AppNavigator,
    libraryScanViewModel: LibraryScanViewModel,
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    onHomeTopBarContentChanged: (TopBarContent) -> Unit,
    onAlbumsTopBarContentChanged: (TopBarContent) -> Unit,
    onScreenTopBarContentChanged: (TopBarContent) -> Unit,
) {
    NavDisplay(
        backStack = appNavigator.backStack,
        onBack = { appNavigator.pop() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    playbackViewModel = playbackViewModel,
                    playlistViewModel = playlistViewModel,
                    appNavigator = appNavigator,
                    onTopBarContentChanged = onHomeTopBarContentChanged,
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
                    onTopBarContentChanged = onScreenTopBarContentChanged,
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
                    onTopBarContentChanged = onAlbumsTopBarContentChanged,
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
                    onTopBarContentChanged = onScreenTopBarContentChanged,
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
                    appNavigator = appNavigator,
                    screenViewModel = screenViewModel,
                    onTopBarContentChanged = onScreenTopBarContentChanged,
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
                    onTopBarContentChanged = onScreenTopBarContentChanged,
                )
            }
            entry<SettingsRoute> {
                SettingsScreen(
                    appNavigator = appNavigator,
                    onTopBarContentChanged = onScreenTopBarContentChanged,
                )
            }
            entry<ClassicalGenreSettingsRoute> {
                ClassicalGenreSettingsScreen(
                    onNavigateBack = { appNavigator.pop() },
                    onRebuildLibrary = { libraryScanViewModel.rebuildLibrary() },
                    onTopBarContentChanged = onScreenTopBarContentChanged,
                )
            }
        },
    )
}
