package com.chaddy50.froh.ui.screens.albumsScreen.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.ui.composables.common.AddToPlaylistState
import com.chaddy50.froh.ui.composables.common.EntityCard
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutCompact
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.data.entity.Playlist

@Composable
fun AlbumsScreenLayoutCompact(
    entityHeaderState: EntityHeaderState,
    allPlaylists: List<Playlist>,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    albums: List<Album>,
    isClassical: Boolean,
    onAlbumClick: (Album) -> Unit,
    addToPlaylistState: AddToPlaylistState<Album>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            EntityHeaderLayoutCompact(
                uiState = entityHeaderState,
                type = EntityType.AlbumArtist,
                allPlaylists = allPlaylists,
                onAddToPlaylist = onAddToPlaylist,
                onCreateAndAdd = onCreateAndAdd,
            )
        }

        items(albums) { album ->
            EntityCard(
                title = album.title,
                onClick = { onAlbumClick(album) },
                onLongClick = { addToPlaylistState.show(album) },
                artworkPath = if (!isClassical) album.artworkPath else null,
                subtitle = if (isClassical) album.catalogueString else album.year,
            )
        }
    }
}
