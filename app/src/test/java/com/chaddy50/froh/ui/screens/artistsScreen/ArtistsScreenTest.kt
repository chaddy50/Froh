package com.chaddy50.froh.ui.screens.artistsScreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeAudioDbRepository
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.navigation.ArtistsRoute
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
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
            AlbumArtistRepository(FakeAlbumArtistDao(), FakeAudioDbRepository()),
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
}
