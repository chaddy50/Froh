package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val NOW_PLAYING_DESCRIPTION = "Now playing"

@RunWith(RobolectricTestRunner::class)
class QueueViewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /** The title [buildQueue] gives the track at [index]; keeps assertions tied to the fixture. */
    private fun trackTitle(index: Int) = "Track $index"

    private fun buildQueue(count: Int): List<MediaItem> = (0 until count).map { index ->
        MediaItem.Builder()
            .setMediaId(index.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(trackTitle(index))
                    .setArtist("Artist $index")
                    .setDurationMs(125_000)
                    .build()
            )
            .build()
    }

    private fun setContent(
        queue: List<MediaItem> = buildQueue(3),
        currentTrackIndex: Int = 0,
        onTrackClicked: (Int) -> Unit = {}
    ) {
        composeTestRule.setContent {
            QueueView(queue, currentTrackIndex, onTrackClicked)
        }
    }

    @Test
    fun rendersTracksInSuppliedOrder() {
        val library = buildQueue(3)
        setContent(queue = listOf(2, 0, 1).map { library[it] })

        // Rows must descend the screen in the supplied order — merely being present would hold
        // for any ordering, which is the thing under test.
        val rowTops = listOf(2, 0, 1).map { index ->
            composeTestRule.onNodeWithText(trackTitle(index)).fetchSemanticsNode().positionInRoot.y
        }

        assert(rowTops == rowTops.sorted()) { "Expected rows ordered 2, 0, 1 but tops were $rowTops" }
    }

    @Test
    fun currentTrackShowsNowPlayingIndicator() {
        setContent(currentTrackIndex = 1)

        composeTestRule.onNodeWithContentDescription(NOW_PLAYING_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun nonCurrentTracksHaveNoNowPlayingIndicator() {
        setContent(currentTrackIndex = 1)

        val indicators = composeTestRule
            .onAllNodesWithContentDescription(NOW_PLAYING_DESCRIPTION)
            .fetchSemanticsNodes()
        assert(indicators.size == 1)
    }

    @Test
    fun clickingTrackInvokesCallbackWithItsQueuePosition() {
        val library = buildQueue(3)
        var clickedIndex = -1
        // Reordered so position and track number diverge: track 0 sits at queue position 1. An
        // identity-ordered queue cannot tell the two apart, which is the contract skipToTrack relies on.
        setContent(
            queue = listOf(2, 0, 1).map { library[it] },
            onTrackClicked = { clickedIndex = it }
        )

        composeTestRule.onNodeWithText(trackTitle(0)).performClick()

        assert(clickedIndex == 1)
    }

    @Test
    fun scrollsToCurrentTrackOnFirstComposition() {
        setContent(queue = buildQueue(50), currentTrackIndex = 40)

        composeTestRule.onNodeWithText(trackTitle(40)).assertIsDisplayed()
    }

    @Test
    fun scrollsToCurrentTrackWhenQueueReorders() {
        val originalQueue = buildQueue(50)
        var queue by mutableStateOf(originalQueue)
        var currentTrackIndex by mutableIntStateOf(0)
        composeTestRule.setContent {
            QueueView(queue, currentTrackIndex) {}
        }

        composeTestRule.runOnIdle {
            queue = originalQueue.reversed()
            currentTrackIndex = 40
        }
        composeTestRule.waitForIdle()

        // Position 40 of the reversed queue holds track 9.
        composeTestRule.onNodeWithText(trackTitle(9)).assertIsDisplayed()
    }

    @Test
    fun doesNotScrollWhenOnlyCurrentTrackIndexChanges() {
        val queue = buildQueue(50)
        var currentTrackIndex by mutableIntStateOf(0)
        composeTestRule.setContent {
            QueueView(queue, currentTrackIndex) {}
        }

        composeTestRule.runOnIdle { currentTrackIndex = 40 }
        composeTestRule.waitForIdle()

        assert(composeTestRule.onAllNodesWithText(trackTitle(40)).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun doesNotScrollWhenQueueIsReplacedByAnEqualList() {
        var queue by mutableStateOf(buildQueue(50))
        var currentTrackIndex by mutableIntStateOf(0)
        composeTestRule.setContent {
            QueueView(queue, currentTrackIndex) {}
        }

        // A rebuilt-but-equal list is what a track advance produces; it must not re-scroll.
        composeTestRule.runOnIdle {
            queue = buildQueue(50)
            currentTrackIndex = 40
        }
        composeTestRule.waitForIdle()

        assert(composeTestRule.onAllNodesWithText(trackTitle(40)).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun emptyQueueRendersNoRowsAndDoesNotCrash() {
        setContent(queue = emptyList(), currentTrackIndex = -1)

        val indicators = composeTestRule
            .onAllNodesWithContentDescription(NOW_PLAYING_DESCRIPTION)
            .fetchSemanticsNodes()
        assert(indicators.isEmpty())
    }
}
