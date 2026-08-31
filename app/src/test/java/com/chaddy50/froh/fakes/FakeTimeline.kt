package com.chaddy50.froh.fakes

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline

/**
 * A [Timeline] that models a shuffle order the way Media3's own `Timeline.RemotableTimeline` does —
 * the timeline a real [androidx.media3.session.MediaController] receives across the session
 * boundary.
 *
 * @param windowCount how many windows the timeline holds.
 * @param shuffledOrder the window indices in shuffle order; defaults to the unshuffled order.
 */
class FakeTimeline(
    private val windowCount: Int,
    private val shuffledOrder: List<Int> = (0 until windowCount).toList()
) : Timeline() {
    /** Every [getNextWindowIndex] repeat mode this timeline has been asked for, in call order. */
    val requestedRepeatModes = mutableListOf<Int>()

    init {
        require(shuffledOrder.sorted() == (0 until windowCount).toList()) {
            "shuffledOrder must be a permutation of 0 until $windowCount, but was $shuffledOrder"
        }
    }

    override fun getWindowCount(): Int = windowCount

    override fun getFirstWindowIndex(shuffleModeEnabled: Boolean): Int {
        if (isEmpty) return C.INDEX_UNSET

        return if (shuffleModeEnabled) shuffledOrder.first() else 0
    }

    override fun getLastWindowIndex(shuffleModeEnabled: Boolean): Int {
        if (isEmpty) return C.INDEX_UNSET

        return if (shuffleModeEnabled) shuffledOrder.last() else windowCount - 1
    }

    override fun getNextWindowIndex(
        windowIndex: Int,
        repeatMode: Int,
        shuffleModeEnabled: Boolean
    ): Int {
        requestedRepeatModes.add(repeatMode)

        if (repeatMode == Player.REPEAT_MODE_ONE) return windowIndex

        val isLastWindow = windowIndex == getLastWindowIndex(shuffleModeEnabled)
        if (isLastWindow) {
            return if (repeatMode == Player.REPEAT_MODE_ALL) {
                getFirstWindowIndex(shuffleModeEnabled)
            } else {
                C.INDEX_UNSET
            }
        }

        if (!shuffleModeEnabled) return windowIndex + 1

        return shuffledOrder[shuffledOrder.indexOf(windowIndex) + 1]
    }

    // Unused by the playback-order traversal; fail loudly rather than return a misleading value.
    override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long) =
        throw UnsupportedOperationException("FakeTimeline models window ordering only")

    override fun getPeriodCount() =
        throw UnsupportedOperationException("FakeTimeline models window ordering only")

    override fun getPeriod(periodIndex: Int, period: Period, setIds: Boolean) =
        throw UnsupportedOperationException("FakeTimeline models window ordering only")

    override fun getIndexOfPeriod(uid: Any) =
        throw UnsupportedOperationException("FakeTimeline models window ordering only")

    override fun getUidOfPeriod(periodIndex: Int) =
        throw UnsupportedOperationException("FakeTimeline models window ordering only")
}
