package com.chaddy50.froh.ui.screens.performancesScreen.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.ui.composables.common.EntityCard
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutExpanded
import com.chaddy50.froh.ui.composables.expanded.BackAffordance

private val EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING = 120.dp
private val EXPANDED_LAYOUT_CONTENT_MAX_WIDTH = 840.dp

@Composable
fun PerformancesScreenLayoutExpanded(
    albumArtistName: String,
    appNavigator: AppNavigator,
    entityHeaderState: EntityHeaderState,
    allPlaylists: List<Playlist>,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    onPlay: (() -> Unit)?,
    onShuffle: (() -> Unit)?,
    performances: List<Performance>,
    onPerformanceClick: (Performance) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.widthIn(max = EXPANDED_LAYOUT_CONTENT_MAX_WIDTH).fillMaxWidth()) {
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
            modifier = Modifier.widthIn(max = EXPANDED_LAYOUT_CONTENT_MAX_WIDTH).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = EXPANDED_LAYOUT_BOTTOM_CONTENT_PADDING),
        ) {
            items(performances) { performance ->
                EntityCard(
                    title = performance.artistName,
                    onClick = { onPerformanceClick(performance) },
                    subtitle = performance.year,
                )
            }
        }
    }
}
