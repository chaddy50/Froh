package com.chaddy50.froh.ui.screens.albumsScreen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.ComposerRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeArtworkDownloader
import com.chaddy50.froh.fakes.FakeAudioDbRepository
import com.chaddy50.froh.fakes.FakeComposerDao
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakeOpenOpusRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.navigation.AlbumsRoute
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.navigation.rememberAppNavigator
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AlbumsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())

    private fun createScreenViewModel(genreId: Long, classicalGenreId: Long?): AlbumsScreenViewModel {
        val albumArtistDao = FakeAlbumArtistDao(MutableStateFlow(listOf(AlbumArtist(id = 1, name = "Bach", sortName = "Bach"))))
        val config = ClassicalGenreConfig().apply { this.classicalGenreId = classicalGenreId }
        return AlbumsScreenViewModel(
            AlbumsRoute(genreId = genreId, albumArtistId = 1L, title = "Bach"),
            config,
            AlbumRepository(FakeAlbumDao()),
            AlbumArtistRepository(albumArtistDao, FakeAudioDbRepository()),
            GenreRepository(FakeGenreDao(allGenres = genresFlow)),
            PlaylistRepository(FakePlaylistDao()),
            ComposerRepository(FakeComposerDao(), FakeOpenOpusRepository(), FakeArtworkDownloader(), albumArtistDao),
        )
    }

    // Mocked rather than constructed: the real PlaybackViewModel binds a MediaController to
    // PlaybackService, which NPEs when Robolectric's idling loop drives the fake service connection.
    private fun createPlaybackViewModel(): PlaybackViewModel = mockk(relaxed = true)

    private fun createPlaylistViewModel() =
        PlaylistViewModel(TrackRepository(FakeTrackDao()), PlaylistRepository(FakePlaylistDao()))

    private fun setAlbumsScreenContent(screenViewModel: AlbumsScreenViewModel, genreId: Long) {
        composeTestRule.setContent {
            var topBarActions by remember { mutableStateOf<@Composable RowScope.() -> Unit>({}) }
            AlbumsScreen(
                genreId = genreId,
                albumArtistId = 1L,
                playbackViewModel = createPlaybackViewModel(),
                playlistViewModel = createPlaylistViewModel(),
                appNavigator = rememberAppNavigator(HomeRoute),
                screenViewModel = screenViewModel,
                onTopBarContentChanged = { topBarContent: TopBarContent -> topBarActions = topBarContent.actions },
            )
            Row { topBarActions() }
        }
    }

    @Test
    fun showsSubGenreFilterWhenComposerHasMultipleSubGenres() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 20L, name = "Keyboard", parentGenreId = 10L),
            Genre(id = 21L, name = "Concerto", parentGenreId = 10L),
        )
        setAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L), genreId = 10L)

        composeTestRule.onNodeWithContentDescription("Filter by sub-genre").assertIsDisplayed()
    }

    @Test
    fun hidesSubGenreFilterWhenComposerHasOnlyOneSubGenre() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 20L, name = "Keyboard", parentGenreId = 10L),
        )
        setAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L), genreId = 10L)

        composeTestRule.onNodeWithContentDescription("Filter by sub-genre").assertDoesNotExist()
    }

    @Test
    fun selectingSubGenreUpdatesScreenViewModelSelection() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 20L, name = "Keyboard", parentGenreId = 10L),
            Genre(id = 21L, name = "Concerto", parentGenreId = 10L),
        )
        val screenViewModel = createScreenViewModel(genreId = 10L, classicalGenreId = 10L)
        setAlbumsScreenContent(screenViewModel, genreId = 10L)

        composeTestRule.onNodeWithContentDescription("Filter by sub-genre").performClick()
        composeTestRule.onNodeWithText("Keyboard").performClick()

        assertEquals(20L, screenViewModel.selectedSubGenreId.value)
    }

    @Test
    fun selectingAllClearsSubGenreSelection() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 20L, name = "Keyboard", parentGenreId = 10L),
            Genre(id = 21L, name = "Concerto", parentGenreId = 10L),
        )
        val screenViewModel = createScreenViewModel(genreId = 10L, classicalGenreId = 10L)
        setAlbumsScreenContent(screenViewModel, genreId = 10L)

        composeTestRule.onNodeWithContentDescription("Filter by sub-genre").performClick()
        composeTestRule.onNodeWithText("Keyboard").performClick()
        composeTestRule.onNodeWithContentDescription("Filter by sub-genre").performClick()
        composeTestRule.onNodeWithText("All").performClick()

        assertNull(screenViewModel.selectedSubGenreId.value)
    }
}
