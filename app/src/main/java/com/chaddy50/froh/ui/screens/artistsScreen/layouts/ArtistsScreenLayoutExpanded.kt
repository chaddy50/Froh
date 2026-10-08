package com.chaddy50.froh.ui.screens.artistsScreen.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.ui.composables.common.AddToPlaylistState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutExpanded
import com.chaddy50.froh.ui.screens.artistsScreen.ArtistWithSubtitle

private val EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING = 120.dp
private val ARTIST_PORTRAIT_SIZE = 116.dp

@Composable
fun ArtistsScreenLayoutExpanded(
    entityHeaderState: EntityHeaderState,
    allPlaylists: List<Playlist>,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    onPlay: (() -> Unit)?,
    onShuffle: (() -> Unit)?,
    artists: List<ArtistWithSubtitle>,
    onArtistClick: (AlbumArtist) -> Unit,
    addToPlaylistState: AddToPlaylistState<AlbumArtist>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp)) {
            EntityHeaderLayoutExpanded(
                uiState = entityHeaderState,
                type = EntityType.Genre,
                allPlaylists = allPlaylists,
                onAddToPlaylist = onAddToPlaylist,
                onCreateAndAdd = onCreateAndAdd,
                onPlay = onPlay,
                onShuffle = onShuffle,
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(
                start = 32.dp,
                end = 32.dp,
                bottom = EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(artists) { artistWithSubtitle ->
                ArtistGridCell(
                    artistWithSubtitle = artistWithSubtitle,
                    onClick = { onArtistClick(artistWithSubtitle.artist) },
                    onLongClick = { addToPlaylistState.show(artistWithSubtitle.artist) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArtistGridCell(
    artistWithSubtitle: ArtistWithSubtitle,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArtistPortrait(
            portraitPath = artistWithSubtitle.artist.portraitPath,
            artistName = artistWithSubtitle.artist.name,
        )
        Text(
            artistWithSubtitle.artist.name,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(top = 8.dp),
        )
        if (artistWithSubtitle.subtitle.isNotEmpty()) {
            Text(
                artistWithSubtitle.subtitle,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
        }
    }
}

@Composable
private fun ArtistPortrait(portraitPath: String?, artistName: String) {
    if (portraitPath != null) {
        AsyncImage(
            model = portraitPath,
            contentDescription = "$artistName Portrait",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(ARTIST_PORTRAIT_SIZE)
                .clip(CircleShape),
        )
        return
    }

    Box(
        modifier = Modifier
            .size(ARTIST_PORTRAIT_SIZE)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "$artistName Portrait",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp),
        )
    }
}
