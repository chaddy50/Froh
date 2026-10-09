package com.chaddy50.froh.ui.composables.common.entityHeader

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

data class EntityHeaderState(
    val title: String = "Title",
    val subtitle: String = "Subtitle",
    val details: String? = null,
    val artworkPath: String? = null,
    val isLoading: Boolean = true,
    val playlistsThatEntityIsAlreadyIn: Set<Long> = setOf(),
)
