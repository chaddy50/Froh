package com.chaddy50.froh.ui.composables.compact

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.ArtistsRoute
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
class TopBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clickingBackIconInvokesAnActiveScreenBackHandlerInsteadOfPoppingDirectly() {
        lateinit var appNavigator: AppNavigator
        var wasScreenBackHandlerInvoked = false
        composeTestRule.setContent {
            appNavigator = rememberAppNavigator(HomeRoute)
            remember(Unit) { appNavigator.push(ArtistsRoute(genreId = 1L, title = "Classical")) }
            BackHandler { wasScreenBackHandlerInvoked = true }
            TopBar(
                title = "Classical",
                appNavigator = appNavigator,
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            )
        }

        composeTestRule.onNodeWithContentDescription("Go back").performClick()

        assertTrue(wasScreenBackHandlerInvoked)
        assertEquals(ArtistsRoute(genreId = 1L, title = "Classical"), appNavigator.currentKey)
    }

    @Test
    fun clickingBackIconStillPopsTheBackStackWhenNoScreenBackHandlerIsRegistered() {
        lateinit var appNavigator: AppNavigator
        composeTestRule.setContent {
            appNavigator = rememberAppNavigator(HomeRoute)
            remember(Unit) { appNavigator.push(ArtistsRoute(genreId = 1L, title = "Classical")) }
            // Mirrors NavDisplay's own `onBack = { appNavigator.pop() }` fallback registration.
            BackHandler { appNavigator.pop() }
            TopBar(
                title = "Classical",
                appNavigator = appNavigator,
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            )
        }

        composeTestRule.onNodeWithContentDescription("Go back").performClick()

        assertEquals(HomeRoute, appNavigator.currentKey)
    }
}
