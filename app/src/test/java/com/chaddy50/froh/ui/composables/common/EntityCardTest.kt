package com.chaddy50.froh.ui.composables.common

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val GLENN_GOULD = "Glenn Gould"
private const val GOLDBERG_VARIATIONS = "Goldberg Variations"

@RunWith(RobolectricTestRunner::class)
class EntityCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysTitle() {
        composeTestRule.setContent {
            EntityCard(title = GLENN_GOULD, subtitle = "1981")
        }
        composeTestRule.onNodeWithText(GLENN_GOULD).assertIsDisplayed()
    }

    @Test
    fun displaysSubtitle() {
        composeTestRule.setContent {
            EntityCard(title = GLENN_GOULD, subtitle = "1981")
        }
        composeTestRule.onNodeWithText("1981").assertIsDisplayed()
    }

    @Test
    fun titleAndSubtitleBothVisible() {
        composeTestRule.setContent {
            EntityCard(title = GLENN_GOULD, subtitle = "1981")
        }
        composeTestRule.onNodeWithText(GLENN_GOULD).assertIsDisplayed()
        composeTestRule.onNodeWithText("1981").assertIsDisplayed()
    }

    @Test
    fun subtitleHiddenWhenNull() {
        composeTestRule.setContent {
            EntityCard(title = GLENN_GOULD)
        }
        composeTestRule.onNodeWithText(GLENN_GOULD).assertIsDisplayed()
    }

    @Test
    fun displaysCatalogueNumberAsSubtitle() {
        composeTestRule.setContent {
            EntityCard(title = GOLDBERG_VARIATIONS, subtitle = "988")
        }
        composeTestRule.onNodeWithText(GOLDBERG_VARIATIONS).assertIsDisplayed()
        composeTestRule.onNodeWithText("988").assertIsDisplayed()
    }

    @Test
    fun classicalAlbumShowsCatalogueStringAsSubtitle() {
        composeTestRule.setContent {
            EntityCard(title = GOLDBERG_VARIATIONS, subtitle = "BWV 988")
        }
        composeTestRule.onNodeWithText(GOLDBERG_VARIATIONS).assertIsDisplayed()
        composeTestRule.onNodeWithText("BWV 988").assertIsDisplayed()
    }

    @Test
    fun classicalAlbumShowsNoSubtitleWhenCatalogueStringNull() {
        composeTestRule.setContent {
            EntityCard(title = GOLDBERG_VARIATIONS, subtitle = null)
        }
        composeTestRule.onNodeWithText(GOLDBERG_VARIATIONS).assertIsDisplayed()
    }

    @Test
    fun nonClassicalAlbumShowsYearAsSubtitle() {
        composeTestRule.setContent {
            EntityCard(title = "The Wall", subtitle = "1979")
        }
        composeTestRule.onNodeWithText("The Wall").assertIsDisplayed()
        composeTestRule.onNodeWithText("1979").assertIsDisplayed()
    }

    @Test
    fun displaysArtworkWhenProvided() {
        composeTestRule.setContent {
            EntityCard(title = GLENN_GOULD, artworkPath = "/fake/path.jpg")
        }
        composeTestRule.onNodeWithContentDescription("Glenn Gould Artwork").assertIsDisplayed()
    }

    @Test
    fun displaysIconWhenProvided() {
        composeTestRule.setContent {
            EntityCard(title = "Classical", icon = Icons.Filled.MusicNote)
        }
        composeTestRule.onNodeWithContentDescription("Classical Icon").assertIsDisplayed()
    }

    @Test
    fun prefersArtworkOverIcon() {
        composeTestRule.setContent {
            EntityCard(
                title = GLENN_GOULD,
                artworkPath = "/fake/path.jpg",
                icon = Icons.Filled.MusicNote,
            )
        }
        composeTestRule.onNodeWithContentDescription("Glenn Gould Artwork").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Glenn Gould Icon").assertIsNotDisplayed()
    }
}
