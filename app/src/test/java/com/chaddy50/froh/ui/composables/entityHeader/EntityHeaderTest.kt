package com.chaddy50.froh.ui.composables.entityHeader

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EntityHeaderTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsRenameIconForPlaylistType() {
        composeTestRule.setContent {
            EntityHeader(
                uiState = EntityHeaderState(title = "Favorites", subtitle = "2 tracks", isLoading = false),
                type = EntityType.Playlist,
            )
        }
        composeTestRule.onNodeWithContentDescription("Rename playlist").assertIsDisplayed()
    }

    @Test
    fun doesNotShowRenameIconForNonPlaylistType() {
        composeTestRule.setContent {
            EntityHeader(
                uiState = EntityHeaderState(title = "Beethoven", subtitle = "", isLoading = false),
                type = EntityType.Artist,
            )
        }
        composeTestRule.onNodeWithContentDescription("Rename playlist").assertDoesNotExist()
    }

    @Test
    fun tappingRenameIconOpensDialogPrefilledWithCurrentTitle() {
        composeTestRule.setContent {
            EntityHeader(
                uiState = EntityHeaderState(title = "Favorites", subtitle = "", isLoading = false),
                type = EntityType.Playlist,
            )
        }
        composeTestRule.onNodeWithContentDescription("Rename playlist").performClick()

        composeTestRule.onNodeWithText("Rename playlist").assertIsDisplayed()
        composeTestRule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun confirmingNewNameInvokesOnRenameCallback() {
        var renamedTo: String? = null
        composeTestRule.setContent {
            EntityHeader(
                uiState = EntityHeaderState(title = "Favorites", subtitle = "", isLoading = false),
                type = EntityType.Playlist,
                onRename = { renamedTo = it },
            )
        }
        composeTestRule.onNodeWithContentDescription("Rename playlist").performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("Workout")
        composeTestRule.onNodeWithText("Rename").performClick()

        assertEquals("Workout", renamedTo)
    }
}
