package com.chaddy50.froh.ui.screens.performancesScreen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Performance
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
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.PerformancesRoute
import com.chaddy50.froh.navigation.TracksRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

    private fun createScreenViewModel(
        albumTitle: String,
        performances: List<Performance> = emptyList(),
        artistName: String = "Artist",
    ): PerformancesScreenViewModel {
        val albums = MutableStateFlow(
            listOf(Album(id = 2L, title = albumTitle, catalogueSortIndex = null, artistId = 1L, year = "1955"))
        )
        val albumArtists = MutableStateFlow(listOf(AlbumArtist(id = 1L, name = artistName, sortName = artistName)))
        return PerformancesScreenViewModel(
            PerformancesRoute(genreId = 1L, albumId = 2L, title = albumTitle),
            ClassicalGenreConfig(),
            PerformanceRepository(FakePerformanceDao(MutableStateFlow(performances))),
            AlbumRepository(FakeAlbumDao(albums)),
            AlbumArtistRepository(FakeAlbumArtistDao(albumArtists), FakeAudioDbRepository()),
            TrackRepository(FakeTrackDao()),
            PlaylistRepository(FakePlaylistDao()),
        )
    }

    private lateinit var capturedAppNavigator: AppNavigator

    private fun setWideLayoutContent(screenViewModel: PerformancesScreenViewModel, albumTitle: String) {
        composeTestRule.setContent {
            capturedAppNavigator = rememberAppNavigator(HomeRoute)
            CompositionLocalProvider(LocalWindowWidthSizeClass provides WindowWidthSizeClass.EXPANDED) {
                PerformancesScreen(
                    genreId = 1L,
                    albumId = 2L,
                    title = albumTitle,
                    playbackViewModel = mockk(relaxed = true),
                    playlistViewModel = PlaylistViewModel(TrackRepository(FakeTrackDao()), PlaylistRepository(FakePlaylistDao())),
                    appNavigator = capturedAppNavigator,
                    screenViewModel = screenViewModel,
                )
            }
        }
    }

    @Test
    fun wideLayoutShowsBackAffordance() {
        // "Goldberg Variations" text itself is ambiguous: both the back affordance and the
        // entity header show the album title, so the content-description is the unambiguous
        // signal that the back affordance specifically rendered.
        setWideLayoutContent(createScreenViewModel(albumTitle = "Goldberg Variations"), albumTitle = "Goldberg Variations")

        composeTestRule.onNodeWithContentDescription("Go back").assertIsDisplayed()
    }

    @Test
    fun wideLayoutBackAffordanceShowsAlbumArtistNameNotAlbumTitle() {
        setWideLayoutContent(
            createScreenViewModel(albumTitle = "Goldberg Variations", artistName = "Johann Sebastian Bach"),
            albumTitle = "Goldberg Variations",
        )

        // The artist name is also the entity header's subtitle, so a correct back affordance
        // means it now appears twice; before the fix it only appeared once (header only).
        composeTestRule.onAllNodesWithText("Johann Sebastian Bach").assertCountEquals(2)
    }

    @Test
    fun wideLayoutRendersPerformanceRows() {
        val performances = listOf(
            Performance(id = 10L, albumId = 2L, albumName = "Goldberg Variations", artistId = 1L, artistName = "Glenn Gould", year = "1955", genreId = 1L)
        )
        setWideLayoutContent(createScreenViewModel(albumTitle = "Goldberg Variations", performances = performances), albumTitle = "Goldberg Variations")

        composeTestRule.onNodeWithText("Glenn Gould").assertIsDisplayed()
        composeTestRule.onNodeWithText("1955").assertIsDisplayed()
    }

    @Test
    fun wideLayoutTappingPerformanceNavigatesToTracksRoute() {
        val performances = listOf(
            Performance(id = 10L, albumId = 2L, albumName = "Goldberg Variations", artistId = 1L, artistName = "Glenn Gould", year = "1955", genreId = 1L)
        )
        setWideLayoutContent(createScreenViewModel(albumTitle = "Goldberg Variations", performances = performances), albumTitle = "Goldberg Variations")

        composeTestRule.onNodeWithText("Glenn Gould").performClick()

        assertEquals(
            TracksRoute(genreId = 1L, albumId = 2L, performanceId = 10L, title = "Goldberg Variations"),
            capturedAppNavigator.currentKey,
        )
    }
}
