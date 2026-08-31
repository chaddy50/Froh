package com.chaddy50.froh.ui.composables.nowPlayingBar

import androidx.media3.common.Player
import com.chaddy50.froh.fakes.FakeTimeline
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

class NowPlayingStateTest {

    // A traversal that fails to terminate would otherwise hang the suite rather than fail it.
    @get:Rule
    val timeout: Timeout = Timeout.seconds(5)

    @Test
    fun shuffleDisabledReturnsTimelineOrder() {
        val timeline = FakeTimeline(windowCount = 5, shuffledOrder = listOf(3, 0, 4, 1, 2))

        assert(timeline.buildPlaybackOrder(isShuffleModeEnabled = false) == listOf(0, 1, 2, 3, 4))
    }

    @Test
    fun shuffleEnabledReturnsShuffledOrder() {
        val timeline = FakeTimeline(windowCount = 5, shuffledOrder = listOf(3, 0, 4, 1, 2))

        val playbackOrder = timeline.buildPlaybackOrder(isShuffleModeEnabled = true)

        assert(playbackOrder == listOf(3, 0, 4, 1, 2))
        assert(playbackOrder != listOf(0, 1, 2, 3, 4))
    }

    @Test
    fun shuffleEnabledVisitsEveryWindowExactlyOnce() {
        val timeline = FakeTimeline(windowCount = 5, shuffledOrder = listOf(3, 0, 4, 1, 2))

        val playbackOrder = timeline.buildPlaybackOrder(isShuffleModeEnabled = true)

        assert(playbackOrder.size == 5)
        assert(playbackOrder.toSet() == (0 until 5).toSet())
    }

    @Test
    fun traversalRequestsRepeatModeOff() {
        val timeline = FakeTimeline(windowCount = 5, shuffledOrder = listOf(3, 0, 4, 1, 2))

        timeline.buildPlaybackOrder(isShuffleModeEnabled = true)

        assert(timeline.requestedRepeatModes.isNotEmpty())
        assert(timeline.requestedRepeatModes.all { it == Player.REPEAT_MODE_OFF })
    }

    @Test
    fun shuffleOrderNotStartingAtZeroIsHonored() {
        val timeline = FakeTimeline(windowCount = 3, shuffledOrder = listOf(2, 0, 1))

        assert(timeline.buildPlaybackOrder(isShuffleModeEnabled = true).first() == 2)
    }

    @Test
    fun emptyTimelineReturnsEmptyList() {
        val timeline = FakeTimeline(windowCount = 0)

        assert(timeline.buildPlaybackOrder(isShuffleModeEnabled = false).isEmpty())
        assert(timeline.buildPlaybackOrder(isShuffleModeEnabled = true).isEmpty())
    }

    @Test
    fun singleWindowTimelineReturnsSingleIndex() {
        val timeline = FakeTimeline(windowCount = 1)

        assert(timeline.buildPlaybackOrder(isShuffleModeEnabled = false) == listOf(0))
        assert(timeline.buildPlaybackOrder(isShuffleModeEnabled = true) == listOf(0))
    }
}
