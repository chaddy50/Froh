package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet

import android.view.Window
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.AlbumArtwork
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.PagerIndicator
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.PlaybackControls
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.ProgressBar
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.QueueView
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.TopBar
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.TrackInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NowPlayingSheet(
    currentTrack: MediaItem?,
    isPlaying: Boolean,
    playbackPosition: Long,
    durationMs: Long,
    isShuffleModeEnabled: Boolean,
    queue: List<MediaItem>,
    currentTrackIndex: Int,
    onShuffleToggled: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipToPreviousTrack: () -> Unit,
    onSkipToNextTrack: () -> Unit,
    onSkipToTrack: (Int) -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pagerState = rememberPagerState(pageCount = { 2 })
    val colorScheme = getColorSchemeForAlbumArtwork(
        currentTrack?.mediaMetadata?.artworkUri?.let { "file://$it".toUri() },
        MaterialTheme.colorScheme
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        containerColor = colorScheme.surface,
        modifier = Modifier
            .fillMaxSize()
    ) {
        MaterialTheme(colorScheme) {
            ApplySystemBarTheme(MaterialTheme.colorScheme.surface)

            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    TopBar(
                        onDismiss = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onDismiss()
                            }
                        },
                        isShuffleModeEnabled = isShuffleModeEnabled,
                        onShuffleToggled = onShuffleToggled
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        if (page == 1) {
                            QueueView(queue, currentTrackIndex, onSkipToTrack)
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AlbumArtwork(currentTrack)

                                TrackInfo(currentTrack)

                                ProgressBar(
                                    playbackPosition,
                                    durationMs,
                                    onSeek
                                )

                                PlaybackControls(
                                    isPlaying,
                                    onPlayPause,
                                    onSkipToPreviousTrack,
                                    onSkipToNextTrack
                                )
                            }
                        }
                    }

                    PagerIndicator(
                        pageCount = 2,
                        currentPage = pagerState.currentPage
                    )
                }
            }
        }
    }
}

// The sheet renders in its own dialog window, which never receives the activity's
// enableEdgeToEdge() setup, and Material3 pins both appearance flags to the system theme
// when it builds that window — neither reflects the album-derived surface.
@androidx.annotation.VisibleForTesting
internal fun applyAlbumThemeToSystemBars(window: Window, shouldUseDarkIcons: Boolean) {
    // Left enabled, the platform paints its own scrim over the album-tinted surface
    // behind the navigation bar in 2-/3-button navigation.
    window.isNavigationBarContrastEnforced = false

    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = shouldUseDarkIcons
        isAppearanceLightNavigationBars = shouldUseDarkIcons
    }
}

@Composable
private fun ApplySystemBarTheme(surfaceColor: Color) {
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window ?: return
    val shouldUseDarkIcons = shouldUseDarkSystemBarIcons(surfaceColor)

    // Keyed on the derived boolean rather than the surface color, which animates for 500ms
    // on every track change.
    LaunchedEffect(dialogWindow, shouldUseDarkIcons) {
        applyAlbumThemeToSystemBars(dialogWindow, shouldUseDarkIcons)
    }
}