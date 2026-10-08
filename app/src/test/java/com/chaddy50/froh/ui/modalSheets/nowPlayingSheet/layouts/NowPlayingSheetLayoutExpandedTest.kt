package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.layouts

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1400dp-h2400dp")
class NowPlayingSheetLayoutExpandedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun buildQueue(count: Int): List<MediaItem> = (0 until count).map { index ->
        MediaItem.Builder()
            .setMediaId(index.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Track $index")
                    .setArtist("Artist $index")
                    .setDurationMs(125_000)
                    .build()
            )
            .build()
    }

    private fun setContent(
        currentTrack: MediaItem? = buildQueue(1).first(),
        isPlaying: Boolean = false,
        playbackPosition: Long = 0,
        durationMs: Long = 0,
        isShuffleModeEnabled: Boolean = false,
        queue: List<MediaItem> = buildQueue(1),
        currentTrackIndex: Int = 0,
        onShuffleToggled: () -> Unit = {},
        onPlayPause: () -> Unit = {},
        onSkipToPreviousTrack: () -> Unit = {},
        onSkipToNextTrack: () -> Unit = {},
        onSkipToTrack: (Int) -> Unit = {},
        onSeek: (Long) -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            NowPlayingSheetLayoutExpanded(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                playbackPosition = playbackPosition,
                durationMs = durationMs,
                isShuffleModeEnabled = isShuffleModeEnabled,
                queue = queue,
                currentTrackIndex = currentTrackIndex,
                onShuffleToggled = onShuffleToggled,
                onPlayPause = onPlayPause,
                onSkipToPreviousTrack = onSkipToPreviousTrack,
                onSkipToNextTrack = onSkipToNextTrack,
                onSkipToTrack = onSkipToTrack,
                onSeek = onSeek,
                onDismiss = onDismiss,
            )
        }
    }

    @Test
    fun rendersLeftPaneTrackInfoAndRightPaneQueueWithPositionIndicator() {
        val queue = buildQueue(8)
        setContent(currentTrack = queue[1], queue = queue, currentTrackIndex = 1)

        // "Track 1" is expected twice: once as the left pane's track title, once as the
        // current row in the right pane's queue list.
        composeTestRule.onAllNodesWithText("Track 1").assertCountEquals(2)
        composeTestRule.onNodeWithText("Queue").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 of 8").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Skip Next").assertIsDisplayed()
    }

    // Dismiss/queue-tap interactions aren't covered: input dispatch into a ModalBottomSheet's
    // Dialog window isn't reliable here, same as NowPlayingSheetLayoutCompactTest.
}
