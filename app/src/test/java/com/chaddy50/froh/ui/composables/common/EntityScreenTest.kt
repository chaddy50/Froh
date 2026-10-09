package com.chaddy50.froh.ui.composables.common

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val PLAY = "Play"
private const val SHUFFLE = "Shuffle"

@RunWith(RobolectricTestRunner::class)
class EntityScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsFabsWhenBothCallbacksProvided() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {},
                onPlay = {},
                onShuffle = {},
            )
        }
        composeTestRule.onNodeWithContentDescription(PLAY).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(SHUFFLE).assertIsDisplayed()
    }

    @Test
    fun hidesFabsWhenBothCallbacksNull() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {},
                onPlay = null,
                onShuffle = null,
            )
        }
        composeTestRule.onNodeWithContentDescription(PLAY).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(SHUFFLE).assertDoesNotExist()
    }

    @Test
    fun hidesFabsWhenOnlyPlayNull() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {},
                onPlay = null,
                onShuffle = {},
            )
        }
        composeTestRule.onNodeWithContentDescription(PLAY).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(SHUFFLE).assertDoesNotExist()
    }

    @Test
    fun hidesFabsWhenOnlyShuffleNull() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {},
                onPlay = {},
                onShuffle = null,
            )
        }
        composeTestRule.onNodeWithContentDescription(PLAY).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(SHUFFLE).assertDoesNotExist()
    }

    @Test
    fun hidesFabsOnWideLayoutEvenWhenBothCallbacksProvided() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalWindowWidthSizeClass provides WindowWidthSizeClass.EXPANDED) {
                EntityScreen(
                    isLoading = false,
                    content = {},
                    onPlay = {},
                    onShuffle = {},
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(PLAY).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(SHUFFLE).assertDoesNotExist()
    }
}
