package com.chaddy50.froh.ui.composables.common.nowPlayingBar.layouts

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MiniPlayerExpandedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun track(title: String = "Track", artist: String = "Artist"): MediaItem =
        MediaItem.Builder()
            .setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist(artist).build())
            .build()

    @Test
    fun rendersNothingWhenNoTrackIsPlaying() {
        composeTestRule.setContent {
            MiniPlayerExpanded(
                currentTrack = null,
                isPlaying = false,
                playbackPosition = 0,
                durationMs = 0,
                onPlayPause = {},
                onSkipToNextTrack = {},
                onSkipToPreviousTrack = {},
                onExpand = {},
            )
        }

        composeTestRule.onNodeWithText("Track").assertDoesNotExist()
    }

    @Test
    fun rendersTitleAndArtistWhenTrackIsPlaying() {
        composeTestRule.setContent {
            MiniPlayerExpanded(
                currentTrack = track(),
                isPlaying = true,
                playbackPosition = 0,
                durationMs = 100,
                onPlayPause = {},
                onSkipToNextTrack = {},
                onSkipToPreviousTrack = {},
                onExpand = {},
            )
        }

        composeTestRule.onNodeWithText("Track").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artist").assertIsDisplayed()
    }

    @Test
    fun tappingPlayPauseInvokesCallback() {
        var called = false
        composeTestRule.setContent {
            MiniPlayerExpanded(
                currentTrack = track(),
                isPlaying = false,
                playbackPosition = 0,
                durationMs = 100,
                onPlayPause = { called = true },
                onSkipToNextTrack = {},
                onSkipToPreviousTrack = {},
                onExpand = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Play").performClick()

        assertTrue(called)
    }

    @Test
    fun tappingSkipNextInvokesCallback() {
        var called = false
        composeTestRule.setContent {
            MiniPlayerExpanded(
                currentTrack = track(),
                isPlaying = true,
                playbackPosition = 0,
                durationMs = 100,
                onPlayPause = {},
                onSkipToNextTrack = { called = true },
                onSkipToPreviousTrack = {},
                onExpand = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Skip to next track").performClick()

        assertTrue(called)
    }

    @Test
    fun tappingSkipToPreviousTrackInvokesCallback() {
        var called = false
        composeTestRule.setContent {
            MiniPlayerExpanded(
                currentTrack = track(),
                isPlaying = true,
                playbackPosition = 0,
                durationMs = 100,
                onPlayPause = {},
                onSkipToNextTrack = {},
                onSkipToPreviousTrack = { called = true },
                onExpand = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Skip to previous track").performClick()

        assertTrue(called)
    }

    @Test
    fun tappingOutsideButtonsInvokesExpandCallback() {
        var expanded = false
        composeTestRule.setContent {
            MiniPlayerExpanded(
                currentTrack = track(),
                isPlaying = true,
                playbackPosition = 0,
                durationMs = 100,
                onPlayPause = {},
                onSkipToNextTrack = {},
                onSkipToPreviousTrack = {},
                onExpand = { expanded = true },
            )
        }

        // Click within the title/artist column, well clear of the icon buttons that sit
        // further right in the row (merged semantics make a text-targeted click land on
        // the whole pill's center, which can overlap a button).
        composeTestRule.onRoot().performTouchInput { click(position = Offset(40f, 38f)) }

        assertTrue(expanded)
    }

    @Test
    fun tappingPlayPauseDoesNotAlsoInvokeExpandCallback() {
        var expanded = false
        var playPauseCalled = false
        composeTestRule.setContent {
            MiniPlayerExpanded(
                currentTrack = track(),
                isPlaying = false,
                playbackPosition = 0,
                durationMs = 100,
                onPlayPause = { playPauseCalled = true },
                onSkipToNextTrack = {},
                onSkipToPreviousTrack = {},
                onExpand = { expanded = true },
            )
        }

        composeTestRule.onNodeWithContentDescription("Play").performClick()

        assertTrue(playPauseCalled)
        assertFalse(expanded)
    }

    @Test
    fun rendersCorrectlyUnderLightTheme() {
        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                MiniPlayerExpanded(
                    currentTrack = track(),
                    isPlaying = true,
                    playbackPosition = 0,
                    durationMs = 100,
                    onPlayPause = {},
                    onSkipToNextTrack = {},
                    onSkipToPreviousTrack = {},
                    onExpand = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Track").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artist").assertIsDisplayed()
    }

    @Test
    fun rendersCorrectlyUnderDarkTheme() {
        composeTestRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                MiniPlayerExpanded(
                    currentTrack = track(),
                    isPlaying = true,
                    playbackPosition = 0,
                    durationMs = 100,
                    onPlayPause = {},
                    onSkipToNextTrack = {},
                    onSkipToPreviousTrack = {},
                    onExpand = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Track").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artist").assertIsDisplayed()
    }
}
