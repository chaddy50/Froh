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

private const val CLASSICAL_GENRE_TITLE = "Classical"

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
class TopBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clickingBackIconInvokesAnActiveScreenBackHandlerInsteadOfPoppingDirectly() {
        var appNavigator: AppNavigator? = null
        var wasScreenBackHandlerInvoked = false
        composeTestRule.setContent {
            val navigator = rememberAppNavigator(HomeRoute)
            appNavigator = navigator
            remember(Unit) { navigator.push(ArtistsRoute(genreId = 1L, title = CLASSICAL_GENRE_TITLE)) }
            BackHandler { wasScreenBackHandlerInvoked = true }
            TopBar(
                title = CLASSICAL_GENRE_TITLE,
                appNavigator = navigator,
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            )
        }

        composeTestRule.onNodeWithContentDescription("Go back").performClick()

        assertTrue(wasScreenBackHandlerInvoked)
        assertEquals(ArtistsRoute(genreId = 1L, title = CLASSICAL_GENRE_TITLE), appNavigator?.currentKey)
    }

    @Test
    fun clickingBackIconStillPopsTheBackStackWhenNoScreenBackHandlerIsRegistered() {
        var appNavigator: AppNavigator? = null
        composeTestRule.setContent {
            val navigator = rememberAppNavigator(HomeRoute)
            appNavigator = navigator
            remember(Unit) { navigator.push(ArtistsRoute(genreId = 1L, title = CLASSICAL_GENRE_TITLE)) }
            // Mirrors NavDisplay's own `onBack = { navigator.pop() }` fallback registration.
            BackHandler { navigator.pop() }
            TopBar(
                title = CLASSICAL_GENRE_TITLE,
                appNavigator = navigator,
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            )
        }

        composeTestRule.onNodeWithContentDescription("Go back").performClick()

        assertEquals(HomeRoute, appNavigator?.currentKey)
    }
}
