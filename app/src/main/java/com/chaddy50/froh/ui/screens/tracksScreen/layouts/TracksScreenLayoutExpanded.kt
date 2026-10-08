package com.chaddy50.froh.ui.screens.tracksScreen.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutExpanded
import com.chaddy50.froh.ui.composables.expanded.BackAffordance

private val EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING = 120.dp
private val EXPANDED_LAYOUT_CONTENT_MAX_WIDTH = 840.dp

@Composable
fun TracksScreenLayoutExpanded(
    albumArtistName: String,
    appNavigator: AppNavigator,
    entityHeaderState: EntityHeaderState,
    allPlaylists: List<Playlist>,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    onPlay: (() -> Unit)?,
    onShuffle: (() -> Unit)?,
    trackListItems: LazyListScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 28.dp)
                .widthIn(max = EXPANDED_LAYOUT_CONTENT_MAX_WIDTH)
                .fillMaxWidth(),
        ) {
            BackAffordance(label = albumArtistName, appNavigator = appNavigator)
            EntityHeaderLayoutExpanded(
                uiState = entityHeaderState,
                type = EntityType.Album,
                allPlaylists = allPlaylists,
                onAddToPlaylist = onAddToPlaylist,
                onCreateAndAdd = onCreateAndAdd,
                onPlay = onPlay,
                onShuffle = onShuffle,
            )
        }
        LazyColumn(
            modifier = Modifier
                .widthIn(max = EXPANDED_LAYOUT_CONTENT_MAX_WIDTH)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING),
            content = trackListItems,
        )
    }
}
