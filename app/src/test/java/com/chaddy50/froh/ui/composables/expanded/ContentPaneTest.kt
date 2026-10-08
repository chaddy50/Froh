package com.chaddy50.froh.ui.composables.expanded

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContentPaneTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsScanProgressBarWhenScanInProgress() {
        composeTestRule.setContent {
            ContentPane(isScanInProgress = true, scanProgress = 0.5f) {
                Text("Content", modifier = Modifier.fillMaxSize())
            }
        }

        composeTestRule.onNodeWithText("Scanning music...").assertIsDisplayed()
    }

    @Test
    fun hidesScanProgressBarWhenNoScanInProgress() {
        composeTestRule.setContent {
            ContentPane(isScanInProgress = false, scanProgress = 0f) {
                Text("Content", modifier = Modifier.fillMaxSize())
            }
        }

        composeTestRule.onNodeWithText("Scanning music...").assertDoesNotExist()
    }

    @Test
    fun scanProgressBarRendersAboveContent() {
        composeTestRule.setContent {
            ContentPane(isScanInProgress = true, scanProgress = 0.5f) {
                Text("Content", modifier = Modifier.fillMaxSize())
            }
        }

        val scanBarTop = composeTestRule.onNodeWithText("Scanning music...")
            .fetchSemanticsNode().positionInRoot.y
        val contentTop = composeTestRule.onNodeWithText("Content")
            .fetchSemanticsNode().positionInRoot.y

        assertTrue(scanBarTop < contentTop)
    }

    @Test
    fun rendersContentWhenNoScanInProgress() {
        composeTestRule.setContent {
            ContentPane(isScanInProgress = false, scanProgress = 0f) {
                Text("Content", modifier = Modifier.fillMaxSize())
            }
        }

        composeTestRule.onNodeWithText("Content").assertIsDisplayed()
    }
}
