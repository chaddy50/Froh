package com.chaddy50.froh.ui.screens.albumsScreen.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.ui.composables.common.AddToPlaylistState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutExpanded
import com.chaddy50.froh.ui.composables.expanded.BackAffordance

private val EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING = 120.dp

@Composable
fun AlbumsScreenLayoutExpanded(
    genreName: String,
    appNavigator: AppNavigator,
    entityHeaderState: EntityHeaderState,
    allPlaylists: List<Playlist>,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    onPlay: (() -> Unit)?,
    onShuffle: (() -> Unit)?,
    subGenres: List<Genre>,
    selectedSubGenreId: Long?,
    onSubGenreSelected: (Long?) -> Unit,
    isClassical: Boolean,
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    addToPlaylistState: AddToPlaylistState<Album>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp)) {
            BackAffordance(label = genreName, appNavigator = appNavigator)
            EntityHeaderLayoutExpanded(
                uiState = entityHeaderState,
                type = EntityType.AlbumArtist,
                allPlaylists = allPlaylists,
                onAddToPlaylist = onAddToPlaylist,
                onCreateAndAdd = onCreateAndAdd,
                onPlay = onPlay,
                onShuffle = onShuffle,
            )
            if (subGenres.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    FilterChip(
                        selected = selectedSubGenreId == null,
                        onClick = { onSubGenreSelected(null) },
                        label = { Text("All") },
                    )
                    subGenres.forEach { subGenre ->
                        FilterChip(
                            selected = selectedSubGenreId == subGenre.id,
                            onClick = { onSubGenreSelected(subGenre.id) },
                            label = { Text(subGenre.name) },
                        )
                    }
                }
            }
        }
        HorizontalDivider()
        if (isClassical) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 32.dp,
                    end = 32.dp,
                    top = 8.dp,
                    bottom = EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING,
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(albums) { album ->
                    WorkRow(
                        album = album,
                        onClick = { onAlbumClick(album) },
                        onLongClick = { addToPlaylistState.show(album) },
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(
                    start = 32.dp,
                    end = 32.dp,
                    top = 24.dp,
                    bottom = EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING,
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(albums) { album ->
                    AlbumArtGridCell(
                        album = album,
                        onClick = { onAlbumClick(album) },
                        onLongClick = { addToPlaylistState.show(album) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkRow(
    album: Album,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                album.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            )
            if (!album.catalogueString.isNullOrEmpty()) {
                Text(
                    album.catalogueString,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumArtGridCell(
    album: Album,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp),
    ) {
        if (album.artworkPath != null) {
            AsyncImage(
                model = album.artworkPath,
                contentDescription = "${album.title} Artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
        }
        Text(
            album.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            album.year,
            style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}
