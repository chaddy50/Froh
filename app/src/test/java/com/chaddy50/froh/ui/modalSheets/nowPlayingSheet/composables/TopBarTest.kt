package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TopBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(
        isShuffleModeEnabled: Boolean = false,
        onDismiss: () -> Unit = {},
        onShuffleToggled: () -> Unit = {},
        showQueueToggle: Boolean = false,
        isQueueHidden: Boolean = false,
        onQueueHiddenToggled: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            TopBar(
                onDismiss = onDismiss,
                isShuffleModeEnabled = isShuffleModeEnabled,
                onShuffleToggled = onShuffleToggled,
                showQueueToggle = showQueueToggle,
                isQueueHidden = isQueueHidden,
                onQueueHiddenToggled = onQueueHiddenToggled
            )
        }
    }

    @Test
    fun displaysDismissButton() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Close").assertIsDisplayed()
    }

    @Test
    fun queueButtonIsNotPresent() {
        setContent()
        assert(composeTestRule.onAllNodesWithContentDescription("Queue").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun displaysShuffleButton() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Shuffle").assertIsDisplayed()
    }

    @Test
    fun displaysShuffleButtonWhenEnabled() {
        setContent(isShuffleModeEnabled = true)
        composeTestRule.onNodeWithContentDescription("Shuffle").assertIsDisplayed()
    }

    @Test
    fun clickingDismissTriggersCallback() {
        var clicked = false
        setContent(onDismiss = { clicked = true })
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        assert(clicked)
    }

    @Test
    fun clickingShuffleTriggersCallback() {
        var clicked = false
        setContent(onShuffleToggled = { clicked = true })
        composeTestRule.onNodeWithContentDescription("Shuffle").performClick()
        assert(clicked)
    }

    @Test
    fun displaysQueueButtonWhenToggleShown() {
        setContent(showQueueToggle = true)
        composeTestRule.onNodeWithContentDescription("Queue").assertIsDisplayed()
    }

    @Test
    fun displaysQueueButtonWhenHidden() {
        setContent(showQueueToggle = true, isQueueHidden = true)
        composeTestRule.onNodeWithContentDescription("Queue").assertIsDisplayed()
    }

    @Test
    fun clickingQueueButtonTriggersCallback() {
        var clicked = false
        setContent(showQueueToggle = true, onQueueHiddenToggled = { clicked = true })
        composeTestRule.onNodeWithContentDescription("Queue").performClick()
        assert(clicked)
    }
}
