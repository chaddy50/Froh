package com.chaddy50.froh.ui.composables.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class RenamePlaylistDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsTextFieldPrefilledWithCurrentNameAndRenameButton() {
        composeTestRule.setContent {
            RenamePlaylistDialog(
                currentName = "Favorites",
                onConfirm = {},
                onDismiss = {},
            )
        }
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rename").assertIsDisplayed()
    }
}
