package com.chaddy50.froh.ui.screens.artistsScreen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeDeezerRepository
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.navigation.AlbumsRoute
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.ArtistsRoute
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
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
class ArtistsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun reportsRouteTitleAsTopBarTitle() {
        val genresFlow = MutableStateFlow(listOf(Genre(id = 5L, name = "Rock")))
        val screenViewModel = ArtistsScreenViewModel(
            ArtistsRoute(genreId = 5L, title = "Rock"),
            ClassicalGenreConfig(),
            AlbumArtistRepository(FakeAlbumArtistDao(), FakeDeezerRepository()),
            AlbumRepository(FakeAlbumDao()),
            GenreRepository(FakeGenreDao(allGenres = genresFlow)),
            PlaylistRepository(FakePlaylistDao()),
        )
        var reportedTitle = ""

        composeTestRule.setContent {
            ArtistsScreen(
                genreId = 5L,
                title = "Rock",
                playbackViewModel = mockk(relaxed = true),
                playlistViewModel = PlaylistViewModel(TrackRepository(FakeTrackDao()), PlaylistRepository(FakePlaylistDao())),
                appNavigator = rememberAppNavigator(HomeRoute),
                screenViewModel = screenViewModel,
                onTopBarContentChanged = { reportedTitle = it.title },
            )
        }

        assertEquals("Rock", reportedTitle)
    }

    private fun buildViewModel(
        genreId: Long = 5L,
        genreName: String = "Rock",
        artistsFlow: MutableStateFlow<List<AlbumArtist>> = MutableStateFlow(
            listOf(AlbumArtist(id = 1L, name = "Bayside", sortName = "Bayside"))
        ),
    ): ArtistsScreenViewModel {
        val genresFlow = MutableStateFlow(listOf(Genre(id = genreId, name = genreName)))
        return ArtistsScreenViewModel(
            ArtistsRoute(genreId = genreId, title = genreName),
            ClassicalGenreConfig(),
            AlbumArtistRepository(FakeAlbumArtistDao(artistsFlow), FakeDeezerRepository()),
            AlbumRepository(FakeAlbumDao()),
            GenreRepository(FakeGenreDao(allGenres = genresFlow)),
            PlaylistRepository(FakePlaylistDao()),
        )
    }

    private lateinit var capturedAppNavigator: AppNavigator

    private fun setWideLayoutContent(screenViewModel: ArtistsScreenViewModel) {
        composeTestRule.setContent {
            capturedAppNavigator = rememberAppNavigator(HomeRoute)
            CompositionLocalProvider(LocalWindowWidthSizeClass provides WindowWidthSizeClass.EXPANDED) {
                ArtistsScreen(
                    genreId = 5L,
                    title = "Rock",
                    playbackViewModel = mockk(relaxed = true),
                    playlistViewModel = PlaylistViewModel(TrackRepository(FakeTrackDao()), PlaylistRepository(FakePlaylistDao())),
                    appNavigator = capturedAppNavigator,
                    screenViewModel = screenViewModel,
                )
            }
        }
    }

    @Test
    fun wideLayoutRendersArtistNameAndInlinePlayShuffleButtons() {
        setWideLayoutContent(buildViewModel())

        composeTestRule.onNodeWithText("Bayside").assertIsDisplayed()
        composeTestRule.onNodeWithText("Play").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shuffle").assertIsDisplayed()
    }

    @Test
    fun wideLayoutTappingArtistCellNavigatesToAlbumsRoute() {
        setWideLayoutContent(buildViewModel())

        composeTestRule.onNodeWithText("Bayside").performClick()

        assertEquals(AlbumsRoute(genreId = 5L, albumArtistId = 1L, title = "Bayside"), capturedAppNavigator.currentKey)
    }

    @Test
    fun wideLayoutLongPressOnArtistCellOpensAddToPlaylistSheet() {
        setWideLayoutContent(buildViewModel())

        composeTestRule.onNodeWithText("Bayside").performTouchInput { longClick() }

        composeTestRule.onNodeWithText("Add to playlist").assertIsDisplayed()
    }
}
