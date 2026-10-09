package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet

import androidx.compose.runtime.Composable
import androidx.media3.common.MediaItem
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.layouts.NowPlayingSheetLayoutCompact
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.layouts.NowPlayingSheetLayoutExpanded

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
    when (LocalWindowWidthSizeClass.current) {
        WindowWidthSizeClass.MEDIUM, WindowWidthSizeClass.COMPACT -> NowPlayingSheetLayoutCompact(
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
            onDismiss
        )
        WindowWidthSizeClass.EXPANDED -> NowPlayingSheetLayoutExpanded(
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
            onDismiss
        )
    }
}
