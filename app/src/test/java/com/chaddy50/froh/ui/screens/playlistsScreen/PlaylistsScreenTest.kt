package com.chaddy50.froh.ui.screens.playlistsScreen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.rememberAppNavigator
import com.chaddy50.froh.ui.composables.EmptyStateContent
import com.chaddy50.froh.ui.composables.EntityCard
import com.chaddy50.froh.ui.composables.EntityScreen
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlaylistsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun showsEmptyStateWhenPlaylistsEmptyAfterLoading() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EmptyStateContent(
                            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                            title = "No playlists yet",
                            subtitle = "Tap + to create your first playlist",
                        )
                        FloatingActionButton(
                            onClick = {},
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Create playlist")
                        }
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("No playlists yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap + to create your first playlist").assertIsDisplayed()
    }

    @Test
    fun fabVisibleOnEmptyState() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EmptyStateContent(
                            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                            title = "No playlists yet",
                            subtitle = "Tap + to create your first playlist",
                        )
                        FloatingActionButton(
                            onClick = {},
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Create playlist")
                        }
                    }
                },
            )
        }
        composeTestRule.onNodeWithContentDescription("Create playlist").assertIsDisplayed()
    }

    @Test
    fun doesNotShowEmptyStateWhileLoading() {
        composeTestRule.setContent {
            EntityScreen(
                isLoading = true,
                content = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EmptyStateContent(
                            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                            title = "No playlists yet",
                            subtitle = "Tap + to create your first playlist",
                        )
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("No playlists yet").assertDoesNotExist()
    }

    @Test
    fun showsPlaylistCardsWhenNonEmpty() {
        val playlistNames = listOf("Favorites", "Workout")
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (playlistNames.isEmpty()) {
                            EmptyStateContent(
                                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                                title = "No playlists yet",
                                subtitle = "Tap + to create your first playlist",
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(playlistNames) { name ->
                                    EntityCard(title = name)
                                }
                            }
                        }
                        FloatingActionButton(
                            onClick = {},
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Create playlist")
                        }
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeTestRule.onNodeWithText("Workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("No playlists yet").assertDoesNotExist()
    }

    @Test
    fun emptyStateDoesNotAppearAlongsidePlaylistCards() {
        val playlistNames = listOf("Favorites")
        composeTestRule.setContent {
            EntityScreen(
                isLoading = false,
                content = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (playlistNames.isEmpty()) {
                            EmptyStateContent(
                                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                                title = "No playlists yet",
                                subtitle = "Tap + to create your first playlist",
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(playlistNames) { name ->
                                    EntityCard(title = name)
                                }
                            }
                        }
                    }
                },
            )
        }
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeTestRule.onNodeWithText("No playlists yet").assertDoesNotExist()
    }

    private val playlistDao = FakePlaylistDao(
        allPlaylistsFlow = MutableStateFlow(listOf(Playlist(id = 1, name = "Favorites"))),
    )
    private val trackDao = FakeTrackDao()

    private fun setPlaylistsScreenContent() {
        composeTestRule.setContent {
            PlaylistsScreen(
                playlistViewModel = PlaylistViewModel(
                    TrackRepository(trackDao),
                    PlaylistRepository(playlistDao),
                ),
                appNavigator = rememberAppNavigator(HomeRoute),
                screenViewModel = PlaylistsScreenViewModel(
                    PlaylistRepository(playlistDao),
                    TrackRepository(trackDao),
                ),
            )
        }
    }

    @Test
    fun longPressOnPlaylistShowsRenameAndDeleteMenuWithoutOpeningDeleteDialog() {
        setPlaylistsScreenContent()

        composeTestRule.onNodeWithText("Favorites").performTouchInput { longClick() }

        composeTestRule.onNodeWithText("Rename").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete \"Favorites\"?").assertDoesNotExist()
    }

    @Test
    fun tappingDeleteInMenuStillOpensDeleteConfirmationDialog() {
        setPlaylistsScreenContent()

        composeTestRule.onNodeWithText("Favorites").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Delete").performClick()

        composeTestRule.onNodeWithText("Delete \"Favorites\"?").assertIsDisplayed()
    }

    @Test
    fun tappingRenameThenConfirmingNewNameRenamesThePlaylist() {
        setPlaylistsScreenContent()

        composeTestRule.onNodeWithText("Favorites").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Rename").performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("Workout")
        composeTestRule.onNodeWithText("Rename").performClick()

        assertEquals(1, playlistDao.updatedPlaylists.size)
        assertEquals("Workout", playlistDao.updatedPlaylists[0].name)
    }
}
