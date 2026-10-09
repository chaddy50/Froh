package com.chaddy50.froh.ui.composables.expanded

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.entity.Playlist
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
import com.chaddy50.froh.navigation.PlaylistTracksRoute
import com.chaddy50.froh.navigation.SettingsRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
import com.chaddy50.froh.ui.screens.genresScreen.GenresScreenViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NavigationDrawerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playlistDao = FakePlaylistDao(
        allPlaylistsFlow = MutableStateFlow(listOf(Playlist(id = 1, name = "Favorites"))),
    )
    private val playlistViewModel = PlaylistViewModel(
        TrackRepository(FakeTrackDao()),
        PlaylistRepository(playlistDao),
    )
    private val genresScreenViewModel = GenresScreenViewModel(
        GenreRepository(FakeGenreDao(topLevelGenresFlow = MutableStateFlow(listOf(Genre(id = 5L, name = "Rock"))))),
        AlbumArtistRepository(FakeAlbumArtistDao(), FakeDeezerRepository()),
        AlbumRepository(FakeAlbumDao()),
        ClassicalGenreConfig(),
    )

    private lateinit var appNavigator: AppNavigator

    private fun setDrawerContent(screenViewModel: GenresScreenViewModel = genresScreenViewModel) {
        composeTestRule.setContent {
            appNavigator = rememberAppNavigator(HomeRoute)
            NavigationDrawer(
                playlistViewModel = playlistViewModel,
                appNavigator = appNavigator,
                screenViewModel = screenViewModel,
            )
        }
    }

    private fun manyGenresScreenViewModel(genreCount: Int = 60): GenresScreenViewModel =
        GenresScreenViewModel(
            GenreRepository(
                FakeGenreDao(
                    topLevelGenresFlow = MutableStateFlow(
                        (1..genreCount).map { Genre(id = it.toLong(), name = "Genre $it") }
                    )
                )
            ),
            AlbumArtistRepository(FakeAlbumArtistDao(), FakeAudioDbRepository()),
            AlbumRepository(FakeAlbumDao()),
            ClassicalGenreConfig(),
        )

    @Test
    fun listsEachTopLevelGenreWithItsStatsSubtitle() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Rock").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 artists · 0 albums").assertIsDisplayed()
    }

    @Test
    fun listsEachPlaylist() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    }

    @Test
    fun tappingGenreNavigatesToItsArtistsRoute() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Rock").performClick()

        assertEquals(ArtistsRoute(genreId = 5L, title = "Rock"), appNavigator.currentKey)
    }

    @Test
    fun tappingGenreResetsBackStackToHomeAndTheSelectedGenre() {
        setDrawerContent()
        appNavigator.push(ArtistsRoute(genreId = 99L, title = "Other"))
        appNavigator.push(AlbumsRoute(genreId = 99L, albumArtistId = 1L, title = "Artist"))

        composeTestRule.onNodeWithText("Rock").performClick()

        assertEquals(listOf(HomeRoute, ArtistsRoute(genreId = 5L, title = "Rock")), appNavigator.backStack.toList())
    }

    @Test
    fun tappingPlaylistNavigatesToItsPlaylistTracksRoute() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Favorites").performClick()

        assertEquals(PlaylistTracksRoute(playlistId = 1, title = "Favorites"), appNavigator.currentKey)
    }

    @Test
    fun tappingPlaylistResetsBackStackToHomeAndTheSelectedPlaylist() {
        setDrawerContent()
        appNavigator.push(ArtistsRoute(genreId = 99L, title = "Other"))
        appNavigator.push(AlbumsRoute(genreId = 99L, albumArtistId = 1L, title = "Artist"))

        composeTestRule.onNodeWithText("Favorites").performClick()

        assertEquals(
            listOf(HomeRoute, PlaylistTracksRoute(playlistId = 1, title = "Favorites")),
            appNavigator.backStack.toList(),
        )
    }

    @Test
    fun showsGearIconForSettings() {
        setDrawerContent()

        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
    }

    @Test
    fun tappingSettingsNavigatesToSettingsRoute() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Settings").performClick()

        assertTrue(appNavigator.currentKey is SettingsRoute)
    }

    @Test
    fun tappingSettingsResetsBackStackToHomeAndSettings() {
        setDrawerContent()
        appNavigator.push(ArtistsRoute(genreId = 99L, title = "Other"))
        appNavigator.push(AlbumsRoute(genreId = 99L, albumArtistId = 1L, title = "Artist"))

        composeTestRule.onNodeWithText("Settings").performClick()

        assertEquals(listOf(HomeRoute, SettingsRoute), appNavigator.backStack.toList())
    }

    @Test
    fun tappingCreatePlaylistButtonThenConfirmingCreatesPlaylist() {
        setDrawerContent()

        composeTestRule.onNodeWithContentDescription("Create playlist").performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("Workout")
        composeTestRule.onNodeWithText("Create").performClick()

        assertEquals(1, playlistDao.insertedPlaylists.size)
        assertEquals("Workout", playlistDao.insertedPlaylists[0].name)
    }

    @Test
    fun longPressOnPlaylistShowsRenameAndDeleteMenuWithoutOpeningDeleteDialog() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Favorites").performTouchInput { longClick() }

        composeTestRule.onNodeWithText("Rename").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete \"Favorites\"?").assertDoesNotExist()
    }

    @Test
    fun tappingDeleteInMenuOpensDeleteConfirmationDialogThenConfirmingDeletesPlaylist() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Favorites").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Delete \"Favorites\"?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").performClick()

        assertEquals(1, playlistDao.deletedPlaylists.size)
        assertEquals("Favorites", playlistDao.deletedPlaylists[0].name)
    }

    @Test
    fun tappingRenameThenConfirmingNewNameRenamesThePlaylist() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Favorites").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Rename").performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("Workout")
        composeTestRule.onNodeWithText("Rename").performClick()

        assertEquals(1, playlistDao.updatedPlaylists.size)
        assertEquals("Workout", playlistDao.updatedPlaylists[0].name)
    }

    @Test
    fun settingsStaysDisplayedAndClickableWhenGenresOverflowTheDrawerHeight() {
        setDrawerContent(manyGenresScreenViewModel())

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").performClick()

        assertTrue(appNavigator.currentKey is SettingsRoute)
    }

    @Test
    fun scrollingRevealsAGenreThatOverflowedTheDrawerHeightAndItRemainsClickable() {
        setDrawerContent(manyGenresScreenViewModel())
        val lastGenreName = "Genre 60"

        composeTestRule.onNodeWithText(lastGenreName).assertIsNotDisplayed()

        composeTestRule.onNodeWithText(lastGenreName).performScrollTo()
        composeTestRule.onNodeWithText(lastGenreName).assertIsDisplayed()
        composeTestRule.onNodeWithText(lastGenreName).performClick()

        assertEquals(ArtistsRoute(genreId = 60L, title = lastGenreName), appNavigator.currentKey)
    }

    @Test
    fun scrollingRevealsAPlaylistThatOverflowedBehindManyGenresAndItRemainsClickable() {
        setDrawerContent(manyGenresScreenViewModel())

        composeTestRule.onNodeWithText("Favorites").assertIsNotDisplayed()

        composeTestRule.onNodeWithText("Favorites").performScrollTo()
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeTestRule.onNodeWithText("Favorites").performClick()

        assertEquals(PlaylistTracksRoute(playlistId = 1, title = "Favorites"), appNavigator.currentKey)
    }

    @Test
    fun rendersCorrectlyUnderLightTheme() {
        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                NavigationDrawer(
                    playlistViewModel = playlistViewModel,
                    appNavigator = rememberAppNavigator(HomeRoute),
                    screenViewModel = genresScreenViewModel,
                )
            }
        }

        composeTestRule.onNodeWithText("Froh").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rock").assertIsDisplayed()
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    }

    @Test
    fun rendersCorrectlyUnderDarkTheme() {
        composeTestRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                NavigationDrawer(
                    playlistViewModel = playlistViewModel,
                    appNavigator = rememberAppNavigator(HomeRoute),
                    screenViewModel = genresScreenViewModel,
                )
            }
        }

        composeTestRule.onNodeWithText("Froh").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rock").assertIsDisplayed()
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    }
}
