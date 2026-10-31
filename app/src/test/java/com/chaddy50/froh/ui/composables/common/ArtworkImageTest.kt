package com.chaddy50.froh.ui.composables.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val ALBUM_ARTWORK_DESCRIPTION = "Album artwork"

@RunWith(RobolectricTestRunner::class)
class ArtworkImageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsPlaceholderWhenModelIsNull() {
        composeTestRule.setContent {
            ArtworkImage(model = null, contentDescription = ALBUM_ARTWORK_DESCRIPTION)
        }

        composeTestRule.onNode(hasTestTag(ARTWORK_PLACEHOLDER_TEST_TAG)).assertIsDisplayed()
    }

    @Test
    fun hidesPlaceholderWhenModelIsProvided() {
        composeTestRule.setContent {
            ArtworkImage(model = "/fake/path.jpg", contentDescription = ALBUM_ARTWORK_DESCRIPTION)
        }

        composeTestRule.onNode(hasTestTag(ARTWORK_PLACEHOLDER_TEST_TAG)).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(ALBUM_ARTWORK_DESCRIPTION).assertIsDisplayed()
    }
}
