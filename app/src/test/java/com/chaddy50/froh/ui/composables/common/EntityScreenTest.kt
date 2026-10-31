package com.chaddy50.froh.ui.composables.common

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val PLAY = "Play"
private const val SHUFFLE = "Shuffle"
private const val POSITION_TOLERANCE_DP = 1f

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

    @Test
    fun centersLoadingSpinnerOnCompactLayout() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = true,
                content = {},
            )
        }

        assertLoadingSpinnerIsCentered()
    }

    @Test
    fun centersLoadingSpinnerOnWideLayout() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalWindowWidthSizeClass provides WindowWidthSizeClass.EXPANDED) {
                EntityScreen(
                    isLoading = true,
                    content = {},
                )
            }
        }

        assertLoadingSpinnerIsCentered()
    }

    private fun assertLoadingSpinnerIsCentered() {
        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()
        val spinnerBounds = composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .getUnclippedBoundsInRoot()

        val spinnerCenterX = (spinnerBounds.left + spinnerBounds.right) / 2
        val spinnerCenterY = (spinnerBounds.top + spinnerBounds.bottom) / 2
        val rootCenterX = (rootBounds.left + rootBounds.right) / 2
        val rootCenterY = (rootBounds.top + rootBounds.bottom) / 2

        assertEquals(rootCenterX.value, spinnerCenterX.value, POSITION_TOLERANCE_DP)
        assertEquals(rootCenterY.value, spinnerCenterY.value, POSITION_TOLERANCE_DP)
    }
}
