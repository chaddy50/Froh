package com.chaddy50.froh.ui.screens.playlistTracksScreen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.fakes.testTrack
import com.chaddy50.froh.navigation.PlaylistTracksRoute
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import com.chaddy50.froh.ui.screens.tracksScreen.TrackCard
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PlaylistTracksScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun reportsRouteTitleAsTopBarTitle() {
        val screenViewModel = PlaylistTracksScreenViewModel(PlaylistTracksRoute(playlistId = 1L, title = "Favorites"), PlaylistRepository(FakePlaylistDao()))
        var reportedTitle = ""

        composeTestRule.setContent {
            PlaylistTracksScreen(
                playlistId = 1L,
                title = "Favorites",
                playbackViewModel = mockk<PlaybackViewModel>(relaxed = true),
                playlistViewModel = PlaylistViewModel(TrackRepository(FakeTrackDao()), PlaylistRepository(FakePlaylistDao())),
                screenViewModel = screenViewModel,
                onTopBarContentChanged = { reportedTitle = it.title },
            )
        }

        assertEquals("Favorites", reportedTitle)
    }

    @Test
    fun doesNotShowTrackNumberForPlaylistTracks() {
        val tracks = listOf(testTrack(title = "Sonata No. 14", number = 5))
        composeTestRule.setContent {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tracks) { track ->
                    TrackCard(
                        track = track,
                        isCurrentlyPlaying = false,
                        onTrackClicked = {},
                        onTrackLongPressed = {},
                        showTrackNumber = false,
                    )
                }
            }
        }
        composeTestRule.onNodeWithText("Sonata No. 14").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertDoesNotExist()
    }
}
