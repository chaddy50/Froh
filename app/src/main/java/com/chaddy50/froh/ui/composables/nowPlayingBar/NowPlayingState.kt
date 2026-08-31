package com.chaddy50.froh.ui.composables.nowPlayingBar

import android.app.Application
import android.content.ComponentName
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.chaddy50.froh.services.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NowPlayingState(
    application: Application,
    private val scope: CoroutineScope
) : Player.Listener {
    private val _currentTrack = MutableStateFlow<MediaItem?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition = _playbackPosition.asStateFlow()

    private val _isShuffleModeEnabled = MutableStateFlow(false)
    val isShuffleModeEnabled = _isShuffleModeEnabled.asStateFlow()

    private val _queue = MutableStateFlow<List<MediaItem>>(emptyList())
    val queue = _queue.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex = _currentTrackIndex.asStateFlow()

    /** The timeline window index behind each position in [queue]; they differ once shuffle is on. */
    private var queueWindowIndices: List<Int> = emptyList()

    private var controllerFuture: ListenableFuture<MediaController>
    val controller: MediaController? get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private var positionUpdateJob: Job? = null

    init {
        val sessionToken = SessionToken(application,
            ComponentName(application, PlaybackService::class.java)
        )
        controllerFuture =
            MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                // Controller is ready, attach a listener so that we get updates about the controller's state
                controller?.addListener(this)
                updateState()
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        _currentTrack.value = mediaItem
        updateState()
        updateQueue()
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        super.onTimelineChanged(timeline, reason)
        updateQueue()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        if (isPlaying) {
            startPositionUpdates()
        } else {
            stopPositionUpdates()
        }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        super.onShuffleModeEnabledChanged(shuffleModeEnabled)
        _isShuffleModeEnabled.value = shuffleModeEnabled
        updateQueue()
    }

    fun play() = controller?.play()
    fun pause() = controller?.pause()
    fun skipNext() { controller?.seekToNext() }
    fun skipPrevious() { controller?.seekToPrevious() }

    fun playOrPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun toggleShuffleMode() {
        controller?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }

    fun seekTo(positionMs: Long) {
        _playbackPosition.value = positionMs
        controller?.seekTo(positionMs)
    }

    fun skipToTrack(queueIndex: Int) {
        val windowIndex = queueWindowIndices.getOrNull(queueIndex) ?: return

        controller?.seekToDefaultPosition(windowIndex)
        controller?.play()
    }

    private fun updateQueue() {
        val controller = controller ?: return

        val playbackOrder = controller.currentTimeline.buildPlaybackOrder(controller.shuffleModeEnabled)
        queueWindowIndices = playbackOrder
        _queue.value = playbackOrder.map { windowIndex -> controller.getMediaItemAt(windowIndex) }
        _currentTrackIndex.value = playbackOrder.indexOf(controller.currentMediaItemIndex)
    }

    private fun startPositionUpdates() {
        stopPositionUpdates() // Ensure only one job is running
        positionUpdateJob = scope.launch {
            while (true) {
                _playbackPosition.value = controller?.currentPosition ?: 0
                delay(1000) // Update every second
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun updateState() {
        controller?.let {
            _currentTrack.value = it.currentMediaItem
            _isPlaying.value = it.isPlaying
            _playbackPosition.value = it.currentPosition
            _isShuffleModeEnabled.value = it.shuffleModeEnabled
            if (it.isPlaying) startPositionUpdates() else stopPositionUpdates()
        }
        updateQueue()
    }

    fun release() {
        controller?.removeListener(this)
        MediaController.releaseFuture(controllerFuture)
        stopPositionUpdates()
    }
}

/**
 * The window indices of this timeline in the order they will actually play. With shuffle off this
 * is simply `0, 1, 2, …`; with shuffle on it follows the player's shuffle order.
 */
internal fun Timeline.buildPlaybackOrder(isShuffleModeEnabled: Boolean): List<Int> {
    if (isEmpty) return emptyList()

    val playbackOrder = ArrayList<Int>(windowCount)
    var windowIndex = getFirstWindowIndex(isShuffleModeEnabled)
    while (windowIndex != C.INDEX_UNSET) {
        playbackOrder.add(windowIndex)
        // REPEAT_MODE_OFF rather than the player's repeat mode: REPEAT_MODE_ONE would return the
        // same index forever and REPEAT_MODE_ALL would wrap, so neither traversal terminates.
        windowIndex = getNextWindowIndex(windowIndex, Player.REPEAT_MODE_OFF, isShuffleModeEnabled)
    }
    return playbackOrder
}