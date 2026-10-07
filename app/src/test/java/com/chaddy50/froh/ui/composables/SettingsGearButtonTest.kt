package com.chaddy50.froh.ui.composables

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsGearButtonTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clickingGearInvokesOnClick() {
        var wasClicked = false
        composeTestRule.setContent {
            SettingsGearButton(onClick = { wasClicked = true })
        }

        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        assertTrue(wasClicked)
    }
}
