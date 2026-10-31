package com.chaddy50.froh.ui.composables.common.nowPlayingBar.layouts

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.chaddy50.froh.ui.composables.common.ARTWORK_PLACEHOLDER_TEST_TAG
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MiniPlayerCompactTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun track(): MediaItem =
        MediaItem.Builder()
            .setMediaMetadata(MediaMetadata.Builder().setTitle("Track").setArtist("Artist").build())
            .build()

    @Test
    fun showsPlaceholderWhenTrackHasNoArtwork() {
        composeTestRule.setContent {
            MiniPlayerCompact(
                currentTrack = track(),
                isPlaying = true,
                playbackPosition = 0,
                durationMs = 100,
                isShuffleModeEnabled = false,
                onPlayPause = {},
                onSkipToNextTrack = {},
                onShuffleToggled = {},
                onExpand = {},
            )
        }

        composeTestRule
            .onNode(hasTestTag(ARTWORK_PLACEHOLDER_TEST_TAG), useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
