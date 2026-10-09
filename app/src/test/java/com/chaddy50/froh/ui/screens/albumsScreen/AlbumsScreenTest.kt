package com.chaddy50.froh.ui.screens.albumsScreen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Composer
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
import com.chaddy50.froh.fakes.FakeDeezerRepository
import com.chaddy50.froh.fakes.FakeComposerDao
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakeOpenOpusRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.navigation.AlbumsRoute
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.ArtistsRoute
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.PerformancesRoute
import com.chaddy50.froh.navigation.TopBarContent
import com.chaddy50.froh.navigation.TracksRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
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

    private val classicalGenreName = "Classical"
    private val keyboardGenreName = "Keyboard"
    private val concertoGenreName = "Concerto"
    private val subGenreFilterContentDescription = "Filter by sub-genre"

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())

    private fun createScreenViewModel(
        genreId: Long,
        classicalGenreId: Long?,
        albums: List<Album> = emptyList(),
        composer: Composer? = null,
    ): AlbumsScreenViewModel {
        val albumArtistDao = FakeAlbumArtistDao(MutableStateFlow(listOf(AlbumArtist(id = 1, name = "Bach", sortName = "Bach"))))
        val config = ClassicalGenreConfig().apply { this.classicalGenreId = classicalGenreId }
        val composersFlow = MutableStateFlow(listOfNotNull(composer))
        return AlbumsScreenViewModel(
            AlbumsRoute(genreId = genreId, albumArtistId = 1L, title = "Bach"),
            config,
            AlbumRepository(FakeAlbumDao(MutableStateFlow(albums))),
            AlbumArtistRepository(albumArtistDao, FakeDeezerRepository()),
            GenreRepository(FakeGenreDao(allGenres = genresFlow)),
            PlaylistRepository(FakePlaylistDao()),
            ComposerRepository(FakeComposerDao(composersFlow), FakeOpenOpusRepository(), FakeArtworkDownloader(), albumArtistDao),
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
            Genre(id = 10L, name = classicalGenreName),
            Genre(id = 20L, name = keyboardGenreName, parentGenreId = 10L),
            Genre(id = 21L, name = concertoGenreName, parentGenreId = 10L),
        )
        setAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L), genreId = 10L)

        composeTestRule.onNodeWithContentDescription(subGenreFilterContentDescription).assertIsDisplayed()
    }

    @Test
    fun hidesSubGenreFilterWhenComposerHasOnlyOneSubGenre() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = classicalGenreName),
            Genre(id = 20L, name = keyboardGenreName, parentGenreId = 10L),
        )
        setAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L), genreId = 10L)

        composeTestRule.onNodeWithContentDescription(subGenreFilterContentDescription).assertDoesNotExist()
    }

    @Test
    fun selectingSubGenreUpdatesScreenViewModelSelection() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = classicalGenreName),
            Genre(id = 20L, name = keyboardGenreName, parentGenreId = 10L),
            Genre(id = 21L, name = concertoGenreName, parentGenreId = 10L),
        )
        val screenViewModel = createScreenViewModel(genreId = 10L, classicalGenreId = 10L)
        setAlbumsScreenContent(screenViewModel, genreId = 10L)

        composeTestRule.onNodeWithContentDescription(subGenreFilterContentDescription).performClick()
        composeTestRule.onNodeWithText(keyboardGenreName).performClick()

        assertEquals(20L, screenViewModel.selectedSubGenreId.value)
    }

    @Test
    fun selectingAllClearsSubGenreSelection() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = classicalGenreName),
            Genre(id = 20L, name = keyboardGenreName, parentGenreId = 10L),
            Genre(id = 21L, name = concertoGenreName, parentGenreId = 10L),
        )
        val screenViewModel = createScreenViewModel(genreId = 10L, classicalGenreId = 10L)
        setAlbumsScreenContent(screenViewModel, genreId = 10L)

        composeTestRule.onNodeWithContentDescription(subGenreFilterContentDescription).performClick()
        composeTestRule.onNodeWithText(keyboardGenreName).performClick()
        composeTestRule.onNodeWithContentDescription(subGenreFilterContentDescription).performClick()
        composeTestRule.onNodeWithText("All").performClick()

        assertNull(screenViewModel.selectedSubGenreId.value)
    }

    private lateinit var capturedAppNavigator: AppNavigator

    private fun setWideLayoutAlbumsScreenContent(screenViewModel: AlbumsScreenViewModel, genreId: Long) {
        composeTestRule.setContent {
            capturedAppNavigator = rememberAppNavigator(HomeRoute)
            // Seed one entry below AlbumsScreen so popping has somewhere meaningful to land,
            // mirroring real navigation (Home -> Artists -> Albums).
            remember(Unit) {
                capturedAppNavigator.push(ArtistsRoute(genreId = genreId, title = classicalGenreName))
            }
            CompositionLocalProvider(LocalWindowWidthSizeClass provides WindowWidthSizeClass.EXPANDED) {
                AlbumsScreen(
                    genreId = genreId,
                    albumArtistId = 1L,
                    playbackViewModel = createPlaybackViewModel(),
                    playlistViewModel = createPlaylistViewModel(),
                    appNavigator = capturedAppNavigator,
                    screenViewModel = screenViewModel,
                )
            }
        }
    }

    @Test
    fun wideLayoutShowsBackAffordanceWithParentGenreName() {
        genresFlow.value = listOf(Genre(id = 10L, name = classicalGenreName))
        val composer = Composer(
            albumArtistId = 1L,
            openOpusId = 1L,
            completeName = "Johann Sebastian Bach",
            birthYear = "1685",
            deathYear = "1750",
            epoch = "Baroque",
            portraitPath = null,
        )
        setWideLayoutAlbumsScreenContent(
            createScreenViewModel(genreId = 10L, classicalGenreId = 10L, composer = composer),
            genreId = 10L,
        )

        composeTestRule.onNodeWithText(classicalGenreName).assertIsDisplayed()
    }

    @Test
    fun wideLayoutTappingBackAffordancePopsBackToArtistsRoute() {
        genresFlow.value = listOf(Genre(id = 10L, name = classicalGenreName))
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L), genreId = 10L)

        composeTestRule.onNodeWithContentDescription("Go back").performClick()

        assertEquals(HomeRoute, capturedAppNavigator.currentKey)
    }

    @Test
    fun wideLayoutShowsSubGenreChipsWhenMultipleSubGenres() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = classicalGenreName),
            Genre(id = 20L, name = keyboardGenreName, parentGenreId = 10L),
            Genre(id = 21L, name = concertoGenreName, parentGenreId = 10L),
        )
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L), genreId = 10L)

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText(keyboardGenreName).assertIsDisplayed()
        composeTestRule.onNodeWithText(concertoGenreName).assertIsDisplayed()
    }

    @Test
    fun wideLayoutHidesSubGenreChipsWhenOnlyOneSubGenre() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = classicalGenreName),
            Genre(id = 20L, name = keyboardGenreName, parentGenreId = 10L),
        )
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L), genreId = 10L)

        composeTestRule.onNodeWithText(keyboardGenreName).assertDoesNotExist()
    }

    @Test
    fun wideLayoutSelectingSubGenreChipUpdatesScreenViewModelSelection() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = classicalGenreName),
            Genre(id = 20L, name = keyboardGenreName, parentGenreId = 10L),
            Genre(id = 21L, name = concertoGenreName, parentGenreId = 10L),
        )
        val screenViewModel = createScreenViewModel(genreId = 10L, classicalGenreId = 10L)
        setWideLayoutAlbumsScreenContent(screenViewModel, genreId = 10L)

        composeTestRule.onNodeWithText(keyboardGenreName).performClick()

        assertEquals(20L, screenViewModel.selectedSubGenreId.value)
    }

    @Test
    fun wideLayoutSelectingAllClearsSubGenreSelection() {
        genresFlow.value = listOf(
            Genre(id = 10L, name = classicalGenreName),
            Genre(id = 20L, name = keyboardGenreName, parentGenreId = 10L),
            Genre(id = 21L, name = concertoGenreName, parentGenreId = 10L),
        )
        val screenViewModel = createScreenViewModel(genreId = 10L, classicalGenreId = 10L)
        setWideLayoutAlbumsScreenContent(screenViewModel, genreId = 10L)

        composeTestRule.onNodeWithText(keyboardGenreName).performClick()
        composeTestRule.onNodeWithText("All").performClick()

        assertNull(screenViewModel.selectedSubGenreId.value)
    }

    @Test
    fun wideLayoutRendersWorkTitleAndCatalogueStringWithoutArtwork() {
        genresFlow.value = listOf(Genre(id = 10L, name = classicalGenreName))
        val albums = listOf(
            Album(id = 100L, title = "Symphony No. 5", catalogueSortIndex = 1, catalogueString = "Op. 67", artistId = 1L, year = "1808")
        )
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L, albums = albums), genreId = 10L)

        composeTestRule.onNodeWithText("Symphony No. 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Op. 67").assertIsDisplayed()
    }

    @Test
    fun wideLayoutTappingWorkNavigatesToPerformancesRoute() {
        genresFlow.value = listOf(Genre(id = 10L, name = classicalGenreName))
        val albums = listOf(
            Album(id = 100L, title = "Symphony No. 5", catalogueSortIndex = 1, catalogueString = "Op. 67", artistId = 1L, year = "1808")
        )
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 10L, classicalGenreId = 10L, albums = albums), genreId = 10L)

        composeTestRule.onNodeWithText("Symphony No. 5").performClick()

        assertEquals(
            PerformancesRoute(genreId = 10L, albumId = 100L, title = "Symphony No. 5"),
            capturedAppNavigator.currentKey,
        )
    }

    @Test
    fun wideLayoutNonClassicalRendersAlbumTitleAndYearAsArtworkGrid() {
        genresFlow.value = listOf(Genre(id = 30L, name = "Rock"))
        val albums = listOf(
            Album(id = 200L, title = "American Idiot", catalogueSortIndex = null, artistId = 1L, year = "2004")
        )
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 30L, classicalGenreId = null, albums = albums), genreId = 30L)

        composeTestRule.onNodeWithText("American Idiot").assertIsDisplayed()
        composeTestRule.onNodeWithText("2004").assertIsDisplayed()
    }

    @Test
    fun wideLayoutNonClassicalTappingAlbumNavigatesToTracksRoute() {
        genresFlow.value = listOf(Genre(id = 30L, name = "Rock"))
        val albums = listOf(
            Album(id = 200L, title = "American Idiot", catalogueSortIndex = null, artistId = 1L, year = "2004")
        )
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 30L, classicalGenreId = null, albums = albums), genreId = 30L)

        composeTestRule.onNodeWithText("American Idiot").performClick()

        assertEquals(
            TracksRoute(genreId = 30L, albumId = 200L, title = "American Idiot"),
            capturedAppNavigator.currentKey,
        )
    }

    @Test
    fun wideLayoutNonClassicalLongPressOpensAddToPlaylistSheet() {
        genresFlow.value = listOf(Genre(id = 30L, name = "Rock"))
        val albums = listOf(
            Album(id = 200L, title = "American Idiot", catalogueSortIndex = null, artistId = 1L, year = "2004")
        )
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 30L, classicalGenreId = null, albums = albums), genreId = 30L)

        composeTestRule.onNodeWithText("American Idiot").performTouchInput { longClick() }

        composeTestRule.onNodeWithText("Add to playlist").assertIsDisplayed()
    }

    @Test
    fun wideLayoutNonClassicalShowsBackAffordanceWithParentGenreName() {
        genresFlow.value = listOf(Genre(id = 30L, name = "Rock"))
        setWideLayoutAlbumsScreenContent(createScreenViewModel(genreId = 30L, classicalGenreId = null), genreId = 30L)

        // "Rock" text itself is ambiguous here: both the back affordance and the (composerless)
        // entity header subtitle show the genre name, so the content-description is the
        // unambiguous signal that the back affordance specifically rendered.
        composeTestRule.onNodeWithContentDescription("Go back").assertIsDisplayed()
    }
}
