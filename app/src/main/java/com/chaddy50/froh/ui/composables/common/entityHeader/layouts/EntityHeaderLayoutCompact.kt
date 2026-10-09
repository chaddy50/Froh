package com.chaddy50.froh.ui.composables.common.entityHeader.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.ui.composables.common.AddToPlaylistSheet
import com.chaddy50.froh.ui.composables.common.RenamePlaylistDialog
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.common.entityHeader.addToPlaylistTypes

@Composable
fun EntityHeaderLayoutCompact(
    uiState: EntityHeaderState,
    type: EntityType,
    allPlaylists: List<Playlist> = emptyList(),
    onAddToPlaylist: (playlistId: Long) -> Unit = {},
    onCreateAndAdd: (name: String) -> Unit = {},
    onRename: (String) -> Unit = {},
) {
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        CircularProgressIndicator()
    } else {

        Column(
            modifier = Modifier
                .padding(0.dp, 10.dp, 0.dp, 0.dp)
                .fillMaxSize()
        ) {
            if (uiState.artworkPath != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(300.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = uiState.artworkPath,
                        contentDescription = "${uiState.title} Artwork",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxHeight()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.padding(10.dp, 3.dp)) {
                        Text(uiState.title, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                    }

                    if (uiState.subtitle.isNotEmpty()) {
                        Row(modifier = Modifier.padding(10.dp, 3.dp)) {
                            Text(uiState.subtitle, style = TextStyle(fontSize = 14.sp))
                        }
                    }

                    if (!uiState.details.isNullOrEmpty()) {
                        Row(modifier = Modifier.padding(10.dp, 3.dp)) {
                            Text(uiState.details, style = TextStyle(fontSize = 14.sp))
                        }
                    }
                }

                if (type in addToPlaylistTypes) {
                    IconButton(onClick = { showAddToPlaylistSheet = true }) {
                        Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to playlist")
                    }
                }

                if (type == EntityType.Playlist) {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Rename playlist")
                    }
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        if (showAddToPlaylistSheet) {
            AddToPlaylistSheet(
                allPlaylists = allPlaylists,
                onAddToPlaylist = { playlistId -> onAddToPlaylist(playlistId) },
                onCreateAndAdd = { name -> onCreateAndAdd(name) },
                onDismiss = { showAddToPlaylistSheet = false },
                playlistsThatEntityIsAlreadyIn = uiState.playlistsThatEntityIsAlreadyIn
            )
        }

        if (showRenameDialog) {
            RenamePlaylistDialog(
                currentName = uiState.title,
                onConfirm = { newName ->
                    if (newName.isNotBlank()) {
                        onRename(newName)
                    }
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false },
            )
        }
    }
}
