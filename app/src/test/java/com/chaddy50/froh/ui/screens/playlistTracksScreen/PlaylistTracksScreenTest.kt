package com.chaddy50.froh.ui.screens.playlistTracksScreen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.chaddy50.froh.fakes.testTrack
import com.chaddy50.froh.ui.screens.tracksScreen.TrackCard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlaylistTracksScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
