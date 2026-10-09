package com.chaddy50.froh.ui.composables.common.entityHeader.layouts

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val FAVORITES_PLAYLIST_TITLE = "Favorites"
private const val RENAME_PLAYLIST = "Rename playlist"

@RunWith(RobolectricTestRunner::class)
class EntityHeaderLayoutCompactTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsRenameIconForPlaylistType() {
        composeTestRule.setContent {
            EntityHeaderLayoutCompact(
                uiState = EntityHeaderState(title = FAVORITES_PLAYLIST_TITLE, subtitle = "2 tracks", isLoading = false),
                type = EntityType.Playlist,
            )
        }
        composeTestRule.onNodeWithContentDescription(RENAME_PLAYLIST).assertIsDisplayed()
    }

    @Test
    fun doesNotShowRenameIconForNonPlaylistType() {
        composeTestRule.setContent {
            EntityHeaderLayoutCompact(
                uiState = EntityHeaderState(title = "Beethoven", subtitle = "", isLoading = false),
                type = EntityType.Artist,
            )
        }
        composeTestRule.onNodeWithContentDescription(RENAME_PLAYLIST).assertDoesNotExist()
    }

    @Test
    fun tappingRenameIconOpensDialogPrefilledWithCurrentTitle() {
        composeTestRule.setContent {
            EntityHeaderLayoutCompact(
                uiState = EntityHeaderState(title = FAVORITES_PLAYLIST_TITLE, subtitle = "", isLoading = false),
                type = EntityType.Playlist,
            )
        }
        composeTestRule.onNodeWithContentDescription(RENAME_PLAYLIST).performClick()

        composeTestRule.onNodeWithText(RENAME_PLAYLIST).assertIsDisplayed()
        composeTestRule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun confirmingNewNameInvokesOnRenameCallback() {
        var renamedTo: String? = null
        composeTestRule.setContent {
            EntityHeaderLayoutCompact(
                uiState = EntityHeaderState(title = FAVORITES_PLAYLIST_TITLE, subtitle = "", isLoading = false),
                type = EntityType.Playlist,
                onRename = { renamedTo = it },
            )
        }
        composeTestRule.onNodeWithContentDescription(RENAME_PLAYLIST).performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("Workout")
        composeTestRule.onNodeWithText("Rename").performClick()

        assertEquals("Workout", renamedTo)
    }
}
