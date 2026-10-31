package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables

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
class AlbumArtworkTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsPlaceholderWhenCurrentTrackHasNoArtwork() {
        val currentTrack = MediaItem.Builder()
            .setMediaMetadata(MediaMetadata.Builder().setTitle("Track").build())
            .build()

        composeTestRule.setContent {
            AlbumArtwork(currentTrack)
        }

        composeTestRule.onNode(hasTestTag(ARTWORK_PLACEHOLDER_TEST_TAG)).assertIsDisplayed()
    }
}
