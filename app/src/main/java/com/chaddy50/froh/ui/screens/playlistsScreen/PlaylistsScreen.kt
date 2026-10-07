package com.chaddy50.froh.ui.screens.playlistsScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.PlaylistTracksRoute
import com.chaddy50.froh.ui.composables.CreateNewPlaylistDialog
import com.chaddy50.froh.ui.composables.EmptyStateContent
import com.chaddy50.froh.ui.composables.EntityCard
import com.chaddy50.froh.ui.composables.EntityScreen
import com.chaddy50.froh.ui.composables.RenamePlaylistDialog

@Composable
fun PlaylistsScreen(
    playlistViewModel: PlaylistViewModel,
    appNavigator: AppNavigator,
    screenViewModel: PlaylistsScreenViewModel = hiltViewModel(),
) {
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
    val hasMusic by screenViewModel.hasMusic.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var playlistWithMenu by remember { mutableStateOf<Playlist?>(null) }

    EntityScreen(
        isLoading = uiState.isLoading,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.playlists.isEmpty()) {
                    EmptyStateContent(
                        icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                        title = "No playlists yet",
                        subtitle = "Tap + to create your first playlist",
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.playlists) { playlist ->
                            Box {
                                EntityCard(
                                    title = playlist.name,
                                    onClick = {
                                        appNavigator.push(PlaylistTracksRoute(playlistId = playlist.id, title = playlist.name))
                                    },
                                    onLongClick = {
                                        playlistWithMenu = playlist
                                    },
                                )
                                DropdownMenu(
                                    expanded = playlistWithMenu?.id == playlist.id,
                                    onDismissRequest = { playlistWithMenu = null },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rename") },
                                        onClick = {
                                            playlistToRename = playlist
                                            playlistWithMenu = null
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            playlistToDelete = playlist
                                            playlistWithMenu = null
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                if (hasMusic) {
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Create playlist")
                    }
                }
            }
        },
    )

    if (showCreateDialog) {
        CreateNewPlaylistDialog(
            onConfirm = { name ->
                if (name.isNotBlank()) {
                    playlistViewModel.createPlaylist(name)
                }
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    playlistToDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete playlist") },
            text = { Text("Delete \"${playlist.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    playlistViewModel.deletePlaylist(playlist)
                    playlistToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) { Text("Cancel") }
            },
        )
    }

    playlistToRename?.let { playlist ->
        RenamePlaylistDialog(
            currentName = playlist.name,
            onConfirm = { newName ->
                if (newName.isNotBlank()) {
                    playlistViewModel.renamePlaylist(playlist, newName)
                }
                playlistToRename = null
            },
            onDismiss = { playlistToRename = null },
        )
    }
}
