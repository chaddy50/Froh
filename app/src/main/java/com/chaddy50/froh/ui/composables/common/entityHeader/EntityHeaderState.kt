package com.chaddy50.froh.ui.composables.common.entityHeader

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class EntityType {
    Genre,
    AlbumArtist,
    Album,
    Artist,
    Performance,
    Track,
    All,
    Playlist,
}

val addToPlaylistTypes = setOf(
    EntityType.Album,
    EntityType.AlbumArtist,
    EntityType.Genre,
    EntityType.Artist,
    EntityType.Performance,
)

private val personEntityTypes = setOf(
    EntityType.AlbumArtist,
    EntityType.Artist,
)

private val musicEntityTypes = setOf(
    EntityType.Album,
    EntityType.Performance,
)

val artworkCapableTypes = personEntityTypes + musicEntityTypes

fun artworkPlaceholderIconFor(type: EntityType): ImageVector =
    if (type in personEntityTypes) Icons.Filled.Person else Icons.Filled.Album

data class EntityHeaderState(
    val title: String = "Title",
    val subtitle: String = "Subtitle",
    val details: String? = null,
    val artworkPath: String? = null,
    val isLoading: Boolean = true,
    val playlistsThatEntityIsAlreadyIn: Set<Long> = setOf(),
    val collageArtworkPaths: List<String?>? = null,
)
