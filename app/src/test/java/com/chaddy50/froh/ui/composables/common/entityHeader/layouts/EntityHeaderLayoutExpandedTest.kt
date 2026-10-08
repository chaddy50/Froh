package com.chaddy50.froh.ui.composables.common.entityHeader.layouts

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EntityHeaderLayoutExpandedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersTitleSubtitleAndDetails() {
        composeTestRule.setContent {
            EntityHeaderLayoutExpanded(
                uiState = EntityHeaderState(
                    title = "Beethoven",
                    subtitle = "Early Romantic - 1770–1827",
                    details = "14 works",
                    isLoading = false,
                ),
                type = EntityType.AlbumArtist,
            )
        }

        composeTestRule.onNodeWithText("Beethoven").assertIsDisplayed()
        composeTestRule.onNodeWithText("Early Romantic - 1770–1827").assertIsDisplayed()
        composeTestRule.onNodeWithText("14 works").assertIsDisplayed()
    }

    @Test
    fun showsAddToPlaylistIconForApplicableType() {
        composeTestRule.setContent {
            EntityHeaderLayoutExpanded(
                uiState = EntityHeaderState(title = "Classical", subtitle = "48 composers", isLoading = false),
                type = EntityType.Genre,
            )
        }

        composeTestRule.onNodeWithContentDescription("Add to playlist").assertIsDisplayed()
    }

    @Test
    fun hidesAddToPlaylistIconForNonApplicableType() {
        composeTestRule.setContent {
            EntityHeaderLayoutExpanded(
                uiState = EntityHeaderState(title = "Tracks", subtitle = "", isLoading = false),
                type = EntityType.Track,
            )
        }

        composeTestRule.onNodeWithContentDescription("Add to playlist").assertDoesNotExist()
    }

    @Test
    fun tappingPlayInvokesCallback() {
        var played = false
        composeTestRule.setContent {
            EntityHeaderLayoutExpanded(
                uiState = EntityHeaderState(title = "Classical", subtitle = "", isLoading = false),
                type = EntityType.Genre,
                onPlay = { played = true },
                onShuffle = {},
            )
        }

        composeTestRule.onNodeWithText("Play").performClick()

        assertTrue(played)
    }

    @Test
    fun tappingShuffleInvokesCallback() {
        var shuffled = false
        composeTestRule.setContent {
            EntityHeaderLayoutExpanded(
                uiState = EntityHeaderState(title = "Classical", subtitle = "", isLoading = false),
                type = EntityType.Genre,
                onPlay = {},
                onShuffle = { shuffled = true },
            )
        }

        composeTestRule.onNodeWithText("Shuffle").performClick()

        assertTrue(shuffled)
    }

    @Test
    fun hidesPlayAndShuffleWhenCallbacksAreNull() {
        composeTestRule.setContent {
            EntityHeaderLayoutExpanded(
                uiState = EntityHeaderState(title = "Classical", subtitle = "", isLoading = false),
                type = EntityType.Genre,
                onPlay = null,
                onShuffle = null,
            )
        }

        composeTestRule.onNodeWithText("Play").assertDoesNotExist()
        composeTestRule.onNodeWithText("Shuffle").assertDoesNotExist()
    }
}
