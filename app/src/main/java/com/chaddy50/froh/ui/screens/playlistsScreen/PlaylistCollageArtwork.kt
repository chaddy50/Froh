package com.chaddy50.froh.ui.screens.playlistsScreen

import com.chaddy50.froh.data.entity.Track

private const val MINIMUM_ALBUMS_FOR_COLLAGE = 2
private const val COLLAGE_CELL_COUNT = 4

fun collageArtworkPaths(tracks: List<Track>): List<String?>? {
    val distinctAlbumArtworkPaths = tracks.asSequence()
        .distinctBy { it.albumId }
        .take(COLLAGE_CELL_COUNT)
        .map { it.artworkPath }
        .toList()
    if (distinctAlbumArtworkPaths.size < MINIMUM_ALBUMS_FOR_COLLAGE) return null
    return (0 until COLLAGE_CELL_COUNT).map { index ->
        distinctAlbumArtworkPaths[index % distinctAlbumArtworkPaths.size]
    }
}
