package com.chaddy50.froh.ui.screens.tracksScreen.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutCompact
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType

@Composable
fun TracksScreenLayoutCompact(
    entityHeaderState: EntityHeaderState,
    allPlaylists: List<Playlist>,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    trackListItems: LazyListScope.() -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            EntityHeaderLayoutCompact(
                uiState = entityHeaderState,
                type = EntityType.Album,
                allPlaylists = allPlaylists,
                onAddToPlaylist = onAddToPlaylist,
                onCreateAndAdd = onCreateAndAdd,
            )
        }

        trackListItems()
    }
}
