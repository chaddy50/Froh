package com.chaddy50.froh.ui.composables.common.entityHeader.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.common.entityHeader.addToPlaylistTypes

@Composable
fun EntityHeaderLayoutExpanded(
    uiState: EntityHeaderState,
    type: EntityType,
    allPlaylists: List<Playlist> = emptyList(),
    onAddToPlaylist: (playlistId: Long) -> Unit = {},
    onCreateAndAdd: (name: String) -> Unit = {},
    onPlay: (() -> Unit)? = null,
    onShuffle: (() -> Unit)? = null,
) {
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        CircularProgressIndicator()
        return
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (uiState.artworkPath != null) {
            AsyncImage(
                model = uiState.artworkPath,
                contentDescription = "${uiState.title} Artwork",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(112.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(uiState.title, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold))
            if (uiState.subtitle.isNotEmpty()) {
                Text(
                    uiState.subtitle,
                    style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            if (!uiState.details.isNullOrEmpty()) {
                Text(
                    uiState.details,
                    style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        }

        if (type in addToPlaylistTypes) {
            IconButton(onClick = { showAddToPlaylistSheet = true }) {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to playlist")
            }
        }

        if (onShuffle != null) {
            OutlinedButton(onClick = onShuffle, modifier = Modifier.heightIn(min = 44.dp)) {
                Icon(Icons.Filled.Shuffle, contentDescription = null)
                Text("Shuffle", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (onPlay != null) {
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .heightIn(min = 44.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("Play", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    if (showAddToPlaylistSheet) {
        AddToPlaylistSheet(
            allPlaylists = allPlaylists,
            onAddToPlaylist = { playlistId -> onAddToPlaylist(playlistId) },
            onCreateAndAdd = { name -> onCreateAndAdd(name) },
            onDismiss = { showAddToPlaylistSheet = false },
            playlistsThatEntityIsAlreadyIn = uiState.playlistsThatEntityIsAlreadyIn,
        )
    }
}
