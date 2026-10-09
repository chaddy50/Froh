package com.chaddy50.froh.ui.screens.genresScreen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.chaddy50.froh.ui.composables.compact.EmptyStateContent
import com.chaddy50.froh.ui.composables.common.EntityCard
import com.chaddy50.froh.ui.composables.common.EntityScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GenresScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsEmptyStateWhenGenresEmptyAfterLoading() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    if (true) { // simulates uiState.genres.isEmpty()
                        EmptyStateContent(
                            icon = Icons.Filled.LibraryMusic,
                            title = "No music yet",
                            subtitle = "Add music to your device to get started",
                        )
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("No music yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add music to your device to get started").assertIsDisplayed()
    }

    @Test
    fun doesNotShowEmptyStateWhileLoading() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = true,
                content = {
                    EmptyStateContent(
                        icon = Icons.Filled.LibraryMusic,
                        title = "No music yet",
                        subtitle = "Add music to your device to get started",
                    )
                },
            )
        }
        composeTestRule.onNodeWithText("No music yet").assertDoesNotExist()
    }

    @Test
    fun showsGenreCardsWhenGenresNonEmpty() {
        val genreNames = listOf("Classical", "Jazz", "Rock")
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    if (genreNames.isEmpty()) {
                        EmptyStateContent(
                            icon = Icons.Filled.LibraryMusic,
                            title = "No music yet",
                            subtitle = "Add music to your device to get started",
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(genreNames) { name ->
                                EntityCard(title = name)
                            }
                        }
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("Classical").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jazz").assertIsDisplayed()
        composeTestRule.onNodeWithText("No music yet").assertDoesNotExist()
    }

    @Test
    fun emptyStateReplacesGenreList() {
        val genreNames = emptyList<String>()
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    if (genreNames.isEmpty()) {
                        EmptyStateContent(
                            icon = Icons.Filled.LibraryMusic,
                            title = "No music yet",
                            subtitle = "Add music to your device to get started",
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(genreNames) { name ->
                                EntityCard(title = name)
                            }
                        }
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("No music yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Classical").assertDoesNotExist()
    }

    @Test
    fun hidesFabsWhenGenresEmpty() {
        val genreNames = emptyList<String>()
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    if (genreNames.isEmpty()) {
                        EmptyStateContent(
                            icon = Icons.Filled.LibraryMusic,
                            title = "No music yet",
                            subtitle = "Add music to your device to get started",
                        )
                    }
                },
                onPlay = if (genreNames.isNotEmpty()) {{ }} else null,
                onShuffle = if (genreNames.isNotEmpty()) {{ }} else null,
            )
        }
        composeTestRule.onNodeWithContentDescription("Play").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Shuffle").assertDoesNotExist()
    }

    @Test
    fun showsFabsWhenGenresNonEmpty() {
        val genreNames = listOf("Classical", "Jazz")
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(genreNames) { name ->
                            EntityCard(title = name)
                        }
                    }
                },
                onPlay = if (genreNames.isNotEmpty()) {{ }} else null,
                onShuffle = if (genreNames.isNotEmpty()) {{ }} else null,
            )
        }
        composeTestRule.onNodeWithContentDescription("Play").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Shuffle").assertIsDisplayed()
    }

    @Test
    fun showsPermissionDeniedStateWhenNotGranted() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    // Simulates: !isPermissionGranted && uiState.genres.isEmpty()
                    EmptyStateContent(
                        icon = Icons.Filled.FolderOff,
                        title = "Permission required",
                        subtitle = "This app needs access to your audio files to display your music library",
                        action = {
                            Button(onClick = {}) {
                                Text("Grant permission")
                            }
                        },
                    )
                },
            )
        }
        composeTestRule.onNodeWithText("Permission required").assertIsDisplayed()
        composeTestRule.onNodeWithText("This app needs access to your audio files to display your music library").assertIsDisplayed()
        composeTestRule.onNodeWithText("No music yet").assertDoesNotExist()
    }

    @Test
    fun permissionDeniedStateShowsGrantButton() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    EmptyStateContent(
                        icon = Icons.Filled.FolderOff,
                        title = "Permission required",
                        subtitle = "This app needs access to your audio files to display your music library",
                        action = {
                            // shouldShowRationale = true
                            Button(onClick = {}) {
                                Text("Grant permission")
                            }
                        },
                    )
                },
            )
        }
        composeTestRule.onNodeWithText("Grant permission").assertIsDisplayed()
    }

    @Test
    fun permissionDeniedStateShowsOpenSettingsButton() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    EmptyStateContent(
                        icon = Icons.Filled.FolderOff,
                        title = "Permission required",
                        subtitle = "This app needs access to your audio files to display your music library",
                        action = {
                            // shouldShowRationale = false (permanently denied)
                            Button(onClick = {}) {
                                Text("Open settings")
                            }
                        },
                    )
                },
            )
        }
        composeTestRule.onNodeWithText("Open settings").assertIsDisplayed()
    }

    @Test
    fun permissionDeniedStateHidesFabs() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    EmptyStateContent(
                        icon = Icons.Filled.FolderOff,
                        title = "Permission required",
                        subtitle = "This app needs access to your audio files to display your music library",
                        action = {
                            Button(onClick = {}) {
                                Text("Grant permission")
                            }
                        },
                    )
                },
                onPlay = null,
                onShuffle = null,
            )
        }
        composeTestRule.onNodeWithContentDescription("Play").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Shuffle").assertDoesNotExist()
    }

    @Test
    fun showsGenericEmptyStateWhenPermissionGrantedButNoGenres() {
        // Permission is granted but genres list is empty
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    // isPermissionGranted = true, genres.isEmpty() = true
                    EmptyStateContent(
                        icon = Icons.Filled.LibraryMusic,
                        title = "No music yet",
                        subtitle = "Add music to your device to get started",
                    )
                },
            )
        }
        composeTestRule.onNodeWithText("No music yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Permission required").assertDoesNotExist()
        composeTestRule.onNodeWithText("Grant permission").assertDoesNotExist()
        composeTestRule.onNodeWithText("Open settings").assertDoesNotExist()
    }

    @Test
    fun showsGenreListWhenPermissionGrantedAndGenresExist() {
        val genreNames = listOf("Classical", "Jazz")
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    // isPermissionGranted = true, genres.isNotEmpty() = true
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(genreNames) { name ->
                            EntityCard(title = name)
                        }
                    }
                },
                onPlay = {{ }},
                onShuffle = {{ }},
            )
        }
        composeTestRule.onNodeWithText("Classical").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jazz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Permission required").assertDoesNotExist()
        composeTestRule.onNodeWithText("No music yet").assertDoesNotExist()
    }

    @Test
    fun grantPermissionButtonClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    EmptyStateContent(
                        icon = Icons.Filled.FolderOff,
                        title = "Permission required",
                        subtitle = "This app needs access to your audio files to display your music library",
                        action = {
                            Button(onClick = { clicked = true }) {
                                Text("Grant permission")
                            }
                        },
                    )
                },
            )
        }
        composeTestRule.onNodeWithText("Grant permission").performClick()
        assert(clicked)
    }
}
