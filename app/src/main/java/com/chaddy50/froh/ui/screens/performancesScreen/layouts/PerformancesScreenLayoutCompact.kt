package com.chaddy50.froh.ui.screens.performancesScreen.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.data.entity.Playlist
import com.chaddy50.froh.ui.composables.common.EntityCard
import com.chaddy50.froh.ui.composables.common.entityHeader.layouts.EntityHeaderLayoutCompact
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityHeaderState
import com.chaddy50.froh.ui.composables.common.entityHeader.EntityType

@Composable
fun PerformancesScreenLayoutCompact(
    entityHeaderState: EntityHeaderState,
    allPlaylists: List<Playlist>,
    onAddToPlaylist: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    performances: List<Performance>,
    onPerformanceClick: (Performance) -> Unit,
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

        items(performances) { performance ->
            EntityCard(
                title = performance.artistName,
                onClick = { onPerformanceClick(performance) },
                subtitle = performance.year,
            )
        }
    }
}
