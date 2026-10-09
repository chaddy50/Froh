package com.chaddy50.froh.ui.composables.expanded

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.ArtistsRoute
import com.chaddy50.froh.navigation.HomeRoute
import com.chaddy50.froh.navigation.PlaylistTracksRoute
import com.chaddy50.froh.navigation.SettingsRoute
import com.chaddy50.froh.ui.composables.common.CreateNewPlaylistDialog
import com.chaddy50.froh.ui.composables.common.RenamePlaylistDialog
import com.chaddy50.froh.ui.screens.genresScreen.GenresScreenViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel

@Composable
fun NavigationDrawer(
    playlistViewModel: PlaylistViewModel,
    appNavigator: AppNavigator,
    screenViewModel: GenresScreenViewModel = hiltViewModel(),
) {
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
    val allPlaylists by playlistViewModel.allPlaylists.collectAsStateWithLifecycle()
    val currentKey = appNavigator.currentKey

    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistWithMenu by remember { mutableStateOf<Playlist?>(null) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 20.dp)
    ) {
        Text(
            "Froh",
            style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
        DrawerSectionLabel("GENRES")
        Spacer(modifier = Modifier.height(8.dp))

        for (genreWithStats in uiState.genres) {
            DrawerItem(
                title = genreWithStats.genre.name,
                subtitle = genreWithStats.subtitle,
                isSelected = currentKey is ArtistsRoute && currentKey.genreId == genreWithStats.genre.id,
                onClick = {
                    appNavigator.resetTo(
                        HomeRoute,
                        ArtistsRoute(genreId = genreWithStats.genre.id, title = genreWithStats.genre.name),
                    )
                },
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DrawerSectionLabel("PLAYLISTS")
            IconButton(onClick = { showCreateDialog = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Create playlist")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        for (playlist in allPlaylists) {
            Box {
                DrawerItem(
                    title = playlist.name,
                    isSelected = currentKey is PlaylistTracksRoute && currentKey.playlistId == playlist.id,
                    onClick = {
                        appNavigator.resetTo(
                            HomeRoute,
                            PlaylistTracksRoute(playlistId = playlist.id, title = playlist.name),
                        )
                    },
                    onLongClick = { playlistWithMenu = playlist },
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

        Spacer(modifier = Modifier.weight(1f))

        DrawerItem(
            title = "Settings",
            icon = Icons.Filled.Settings,
            isSelected = currentKey is SettingsRoute,
            onClick = { appNavigator.resetTo(HomeRoute, SettingsRoute) },
        )
    }

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
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text,
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val height = if (subtitle != null) 56.dp else 44.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        Column {
            Text(
                title,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    ),
                )
            }
        }
    }
}
