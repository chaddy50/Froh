package com.chaddy50.froh.ui.composables.compact

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EmptyStateContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysTitle() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "No music yet",
                subtitle = "Add music to your device to get started",
            )
        }
        composeTestRule.onNodeWithText("No music yet").assertIsDisplayed()
    }

    @Test
    fun displaysSubtitle() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "No music yet",
                subtitle = "Add music to your device to get started",
            )
        }
        composeTestRule.onNodeWithText("Add music to your device to get started").assertIsDisplayed()
    }

    @Test
    fun titleAndSubtitleBothVisible() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "No music yet",
                subtitle = "Add music to your device to get started",
            )
        }
        composeTestRule.onNodeWithText("No music yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add music to your device to get started").assertIsDisplayed()
    }

    @Test
    fun rendersWithEmptyTitle() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "",
                subtitle = "Some subtitle",
            )
        }
        composeTestRule.onNodeWithText("Some subtitle").assertIsDisplayed()
    }

    @Test
    fun rendersWithEmptySubtitle() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "Some title",
                subtitle = "",
            )
        }
        composeTestRule.onNodeWithText("Some title").assertIsDisplayed()
    }

    @Test
    fun rendersWithDifferentIcons() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                title = "No playlists yet",
                subtitle = "Tap + to create your first playlist",
            )
        }
        composeTestRule.onNodeWithText("No playlists yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap + to create your first playlist").assertIsDisplayed()
    }

    @Test
    fun doesNotRenderActionWhenOmitted() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "No music yet",
                subtitle = "Add music to your device to get started",
            )
        }
        composeTestRule.onNodeWithText("Grant").assertDoesNotExist()
    }

    @Test
    fun rendersActionContentWhenProvided() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "No music yet",
                subtitle = "Some subtitle",
                action = {
                    Button(onClick = {}) {
                        Text("Grant permission")
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("Grant permission").assertIsDisplayed()
    }

    @Test
    fun actionAndSubtitleBothVisible() {
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "Title",
                subtitle = "Subtitle text",
                action = {
                    Button(onClick = {}) {
                        Text("Action button")
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("Subtitle text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Action button").assertIsDisplayed()
    }

    @Test
    fun actionButtonClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            EmptyStateContent(
                icon = Icons.Filled.MusicNote,
                title = "Title",
                subtitle = "Subtitle",
                action = {
                    Button(onClick = { clicked = true }) {
                        Text("Click me")
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("Click me").performClick()
        assert(clicked)
    }
}
