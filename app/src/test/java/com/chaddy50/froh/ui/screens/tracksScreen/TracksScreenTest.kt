package com.chaddy50.froh.ui.screens.tracksScreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.PerformanceRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeAudioDbRepository
import com.chaddy50.froh.fakes.FakePerformanceDao
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.navigation.TracksRoute
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TracksScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun reportsRouteTitleAsTopBarTitle() {
        val performanceTitle = "Goldberg Variations"
        val screenViewModel = TracksScreenViewModel(
            TracksRoute(genreId = 1L, albumId = 2L, performanceId = -1L, title = performanceTitle),
            ClassicalGenreConfig(),
            TrackRepository(FakeTrackDao()),
            AlbumRepository(FakeAlbumDao()),
            AlbumArtistRepository(FakeAlbumArtistDao(), FakeAudioDbRepository()),
            PerformanceRepository(FakePerformanceDao()),
            PlaylistRepository(FakePlaylistDao()),
        )
        var reportedTitle = ""

        composeTestRule.setContent {
            TracksScreen(
                genreId = 1L,
                albumId = 2L,
                performanceId = null,
                title = performanceTitle,
                playbackViewModel = mockk(relaxed = true),
                playlistViewModel = PlaylistViewModel(TrackRepository(FakeTrackDao()), PlaylistRepository(FakePlaylistDao())),
                screenViewModel = screenViewModel,
                onTopBarContentChanged = { reportedTitle = it.title },
            )
        }

        assertEquals(performanceTitle, reportedTitle)
    }
}
