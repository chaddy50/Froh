package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.AlbumArtwork
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.PlaybackControls
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.ProgressBar
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.QueueView
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.TopBar
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables.TrackInfo
import com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.getColorSchemeForAlbumArtwork
import kotlinx.coroutines.launch

private val LEFT_PANE_WIDTH = 580.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheetLayoutExpanded(
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
        modifier = Modifier.fillMaxSize()
    ) {
        MaterialTheme(colorScheme) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .width(LEFT_PANE_WIDTH)
                            .fillMaxHeight()
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

                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Queue",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${currentTrackIndex + 1} of ${queue.size}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                QueueView(
                                    queue,
                                    currentTrackIndex,
                                    onSkipToTrack,
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
