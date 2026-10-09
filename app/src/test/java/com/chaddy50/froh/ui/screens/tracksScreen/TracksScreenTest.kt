package com.chaddy50.froh.ui.screens.tracksScreen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.media3.common.MediaItem
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.PerformanceRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeDeezerRepository
import com.chaddy50.froh.fakes.FakePerformanceDao
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.TracksRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
import com.chaddy50.froh.ui.composables.common.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.seconds

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
            AlbumArtistRepository(FakeAlbumArtistDao(), FakeDeezerRepository()),
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
                appNavigator = rememberAppNavigator(HomeRoute),
                screenViewModel = screenViewModel,
                onTopBarContentChanged = { reportedTitle = it.title },
            )
        }

        assertEquals(performanceTitle, reportedTitle)
    }

    private fun track(id: Long, number: Int, title: String, discNumber: Int) = Track(
        id = id,
        uri = "file://$id",
        title = title,
        number = number,
        albumId = 2L,
        albumName = "Album",
        artistId = 1L,
        artistName = "Artist",
        albumArtistId = 1L,
        albumArtistName = "Artist",
        genreId = 1L,
        genreName = "Classical",
        parentGenreId = null,
        parentGenreName = null,
        duration = 180.seconds,
        discNumber = discNumber,
        year = "1955",
    )

    private fun createScreenViewModel(
        albumTitle: String,
        tracks: List<Track> = emptyList(),
        artistName: String = "Artist",
    ): TracksScreenViewModel {
        val albums = MutableStateFlow(
            listOf(Album(id = 2L, title = albumTitle, catalogueSortIndex = null, artistId = 1L, year = "1955"))
        )
        val albumArtists = MutableStateFlow(listOf(AlbumArtist(id = 1L, name = artistName, sortName = artistName)))
        return TracksScreenViewModel(
            TracksRoute(genreId = 1L, albumId = 2L, performanceId = -1L, title = albumTitle),
            ClassicalGenreConfig(),
            TrackRepository(FakeTrackDao(MutableStateFlow(tracks))),
            AlbumRepository(FakeAlbumDao(albums)),
            AlbumArtistRepository(FakeAlbumArtistDao(albumArtists), FakeDeezerRepository()),
            PerformanceRepository(FakePerformanceDao()),
            PlaylistRepository(FakePlaylistDao()),
        )
    }

    private lateinit var capturedAppNavigator: AppNavigator

    // Relaxed-mocking the whole PlaybackViewModel leaves nowPlayingState.currentTrack as an
    // untyped relaxed mock, which crashes collectAsStateWithLifecycle() with a ClassCastException
    // the moment a TrackCard actually reads it — stub that one chain with a real StateFlow.
    private fun createPlaybackViewModel(): PlaybackViewModel {
        val playbackViewModel: PlaybackViewModel = mockk(relaxed = true)
        every { playbackViewModel.nowPlayingState.currentTrack } returns MutableStateFlow<MediaItem?>(null)
        return playbackViewModel
    }

    private fun setWideLayoutContent(
        screenViewModel: TracksScreenViewModel,
        albumTitle: String,
        playbackViewModel: PlaybackViewModel = createPlaybackViewModel(),
    ) {
        composeTestRule.setContent {
            capturedAppNavigator = rememberAppNavigator(HomeRoute)
            CompositionLocalProvider(LocalWindowWidthSizeClass provides WindowWidthSizeClass.EXPANDED) {
                TracksScreen(
                    genreId = 1L,
                    albumId = 2L,
                    performanceId = null,
                    title = albumTitle,
                    playbackViewModel = playbackViewModel,
                    playlistViewModel = PlaylistViewModel(TrackRepository(FakeTrackDao()), PlaylistRepository(FakePlaylistDao())),
                    appNavigator = capturedAppNavigator,
                    screenViewModel = screenViewModel,
                )
            }
        }
    }

    @Test
    fun wideLayoutShowsBackAffordance() {
        setWideLayoutContent(createScreenViewModel(albumTitle = "Goldberg Variations"), albumTitle = "Goldberg Variations")

        composeTestRule.onNodeWithContentDescription("Go back").assertIsDisplayed()
    }

    @Test
    fun wideLayoutBackAffordanceShowsAlbumArtistNameNotAlbumTitle() {
        setWideLayoutContent(
            createScreenViewModel(albumTitle = "Goldberg Variations", artistName = "Glenn Gould"),
            albumTitle = "Goldberg Variations",
        )

        // The artist name is also the entity header's subtitle, so a correct back affordance
        // means it now appears twice; before the fix it only appeared once (header only).
        composeTestRule.onAllNodesWithText("Glenn Gould").assertCountEquals(2)
    }

    @Test
    fun wideLayoutShowsDiscStickyHeadersForMultiDiscAlbum() {
        val tracks = listOf(
            track(id = 1L, number = 1, title = "Aria", discNumber = 1),
            track(id = 2L, number = 1, title = "Variation 1", discNumber = 2),
        )
        setWideLayoutContent(createScreenViewModel(albumTitle = "Goldberg Variations", tracks = tracks), albumTitle = "Goldberg Variations")

        composeTestRule.onNodeWithText("Disc 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disc 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aria").assertIsDisplayed()
        composeTestRule.onNodeWithText("Variation 1").assertIsDisplayed()
    }

    @Test
    fun wideLayoutTappingTrackPlaysIt() {
        val tracks = listOf(track(id = 1L, number = 1, title = "Aria", discNumber = 1))
        val playbackViewModel = createPlaybackViewModel()
        setWideLayoutContent(
            createScreenViewModel(albumTitle = "Goldberg Variations", tracks = tracks),
            albumTitle = "Goldberg Variations",
            playbackViewModel = playbackViewModel,
        )

        composeTestRule.onNodeWithText("Aria").performClick()

        verify { playbackViewModel.playTrack(tracks[0], tracks) }
    }
}
