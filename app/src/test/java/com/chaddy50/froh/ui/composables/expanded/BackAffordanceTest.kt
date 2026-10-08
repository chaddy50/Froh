package com.chaddy50.froh.ui.composables.expanded

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.ArtistsRoute
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackAffordanceTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersLabel() {
        composeTestRule.setContent {
            BackAffordance(label = "Classical", appNavigator = rememberAppNavigator(HomeRoute))
        }

        composeTestRule.onNodeWithText("Classical").assertIsDisplayed()
    }

    @Test
    fun tappingPopsTheBackStack() {
        lateinit var appNavigator: AppNavigator
        composeTestRule.setContent {
            appNavigator = rememberAppNavigator(HomeRoute)
            remember(Unit) { appNavigator.push(ArtistsRoute(genreId = 1L, title = "Classical")) }
            BackAffordance(label = "Classical", appNavigator = appNavigator)
        }

        composeTestRule.onNodeWithText("Classical").performClick()

        assertEquals(HomeRoute, appNavigator.currentKey)
    }
}
