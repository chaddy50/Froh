package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import com.chaddy50.froh.ui.composables.common.ArtworkImage

@Composable
fun AlbumArtwork(currentTrack: MediaItem?) {
    ArtworkImage(
        model = currentTrack?.mediaMetadata?.artworkUri?.let { "file://$it".toUri() },
        contentDescription = "Album artwork",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(320.dp),
    )
}