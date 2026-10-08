package com.chaddy50.froh.ui.screens.artistsScreen.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.ui.composables.common.AddToPlaylistState
import com.chaddy50.froh.ui.composables.common.EntityCard
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutCompact
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.ui.screens.artistsScreen.ArtistWithSubtitle

@Composable
fun ArtistsScreenLayoutCompact(
    entityHeaderState: EntityHeaderState,
    allPlaylists: List<Playlist>,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    artists: List<ArtistWithSubtitle>,
    onArtistClick: (AlbumArtist) -> Unit,
    addToPlaylistState: AddToPlaylistState<AlbumArtist>,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            EntityHeaderLayoutCompact(
                uiState = entityHeaderState,
                type = EntityType.Genre,
                allPlaylists = allPlaylists,
                onAddToPlaylist = onAddToPlaylist,
                onCreateAndAdd = onCreateAndAdd,
            )
        }

        items(artists) { artistWithSubtitle ->
            EntityCard(
                artistWithSubtitle.artist.name,
                onClick = { onArtistClick(artistWithSubtitle.artist) },
                onLongClick = { addToPlaylistState.show(artistWithSubtitle.artist) },
                artworkPath = artistWithSubtitle.artist.portraitPath,
                subtitle = artistWithSubtitle.subtitle,
            )
        }
    }
}
