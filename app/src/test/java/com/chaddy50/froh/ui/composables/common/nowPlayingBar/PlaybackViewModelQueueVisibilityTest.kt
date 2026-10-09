package com.chaddy50.froh.ui.composables.common.nowPlayingBar

import androidx.test.core.app.ApplicationProvider
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.preferences.FakeQueuePreferences
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeDeezerRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlaybackViewModelQueueVisibilityTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createPlaybackViewModel(queuePreferences: FakeQueuePreferences): PlaybackViewModel =
        PlaybackViewModel(
            ApplicationProvider.getApplicationContext(),
            ClassicalGenreConfig(),
            TrackRepository(FakeTrackDao()),
            PlaylistRepository(FakePlaylistDao()),
            AlbumArtistRepository(FakeAlbumArtistDao(), FakeDeezerRepository()),
            queuePreferences,
        )

    @Test
    fun reflectsPersistedQueueHiddenPreference() = runTest {
        val playbackViewModel = createPlaybackViewModel(FakeQueuePreferences(initialIsQueueHidden = true))

        assertTrue(playbackViewModel.isQueueHidden.first())
    }

    @Test
    fun toggleQueueHiddenFlipsThePersistedPreference() = runTest {
        val queuePreferences = FakeQueuePreferences(initialIsQueueHidden = false)
        val playbackViewModel = createPlaybackViewModel(queuePreferences)

        playbackViewModel.toggleQueueHidden()

        assertTrue(queuePreferences.isQueueHidden.first())
    }

    @Test
    fun togglingTwiceRestoresTheOriginalPreference() = runTest {
        val queuePreferences = FakeQueuePreferences(initialIsQueueHidden = false)
        val playbackViewModel = createPlaybackViewModel(queuePreferences)

        playbackViewModel.toggleQueueHidden()
        playbackViewModel.toggleQueueHidden()

        assertFalse(queuePreferences.isQueueHidden.first())
    }
}
