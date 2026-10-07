package com.chaddy50.froh.ui.screens.performancesScreen

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
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.PerformancesRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
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
class PerformancesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun reportsRouteTitleAsTopBarTitle() {
        val performanceTitle = "Goldberg Variations"
        val screenViewModel = PerformancesScreenViewModel(
            PerformancesRoute(genreId = 1L, albumId = 2L, title = performanceTitle),
            ClassicalGenreConfig(),
            PerformanceRepository(FakePerformanceDao()),
            AlbumRepository(FakeAlbumDao()),
            AlbumArtistRepository(FakeAlbumArtistDao(), FakeAudioDbRepository()),
            TrackRepository(FakeTrackDao()),
            PlaylistRepository(FakePlaylistDao()),
        )
        var reportedTitle = ""

        composeTestRule.setContent {
            PerformancesScreen(
                genreId = 1L,
                albumId = 2L,
                title = performanceTitle,
                playbackViewModel = mockk(relaxed = true),
                playlistViewModel = PlaylistViewModel(TrackRepository(FakeTrackDao()), PlaylistRepository(FakePlaylistDao())),
                appNavigator = rememberAppNavigator(HomeRoute),
                screenViewModel = screenViewModel,
                onTopBarContentChanged = { reportedTitle = it.title },
            )
        }

        assertEquals(performanceTitle, reportedTitle)
    }
}
