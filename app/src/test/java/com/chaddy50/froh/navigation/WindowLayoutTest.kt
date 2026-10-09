package com.chaddy50.froh.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WindowLayoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Composable
    private fun WindowWidthSizeClassLabel() {
        Text(rememberWindowWidthSizeClass().name)
    }

    @Test
    fun isCompactBelowMediumWidth() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(320.dp, 800.dp))) {
                WindowWidthSizeClassLabel()
            }
        }
        composeTestRule.onNodeWithText("COMPACT").assertIsDisplayed()
    }

    @Test
    fun isMediumBetweenMediumAndExpandedWidth() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(600.dp, 800.dp))) {
                WindowWidthSizeClassLabel()
            }
        }
        composeTestRule.onNodeWithText("MEDIUM").assertIsDisplayed()
    }

    @Test
    fun isExpandedAtExpandedWidth() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(840.dp, 800.dp))) {
                WindowWidthSizeClassLabel()
            }
        }
        composeTestRule.onNodeWithText("EXPANDED").assertIsDisplayed()
    }
}
