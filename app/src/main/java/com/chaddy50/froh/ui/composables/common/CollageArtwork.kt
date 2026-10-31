package com.chaddy50.froh.ui.composables.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage

@Composable
fun CollageArtwork(artworkPaths: List<String?>, title: String, modifier: Modifier = Modifier) {
    require(artworkPaths.size == 4) { "CollageArtwork requires exactly 4 artwork paths" }
    Column(modifier = modifier.aspectRatio(1f)) {
        Row(modifier = Modifier.weight(1f)) {
            CollageCell(artworkPaths[0], title, index = 1)
            CollageCell(artworkPaths[1], title, index = 2)
        }
        Row(modifier = Modifier.weight(1f)) {
            CollageCell(artworkPaths[2], title, index = 3)
            CollageCell(artworkPaths[3], title, index = 4)
        }
    }
}

@Composable
private fun RowScope.CollageCell(artworkPath: String?, title: String, index: Int) {
    AsyncImage(
        model = artworkPath,
        contentDescription = "$title Artwork $index",
        modifier = Modifier.weight(1f).fillMaxHeight(),
    )
}
