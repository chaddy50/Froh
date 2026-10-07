package com.chaddy50.froh.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data class ArtistsRoute(val genreId: Long, val title: String) : NavKey

@Serializable
data class AlbumsRoute(val genreId: Long, val albumArtistId: Long, val title: String) : NavKey

@Serializable
data class PerformancesRoute(val genreId: Long, val albumId: Long, val title: String) : NavKey

@Serializable
data class TracksRoute(val genreId: Long, val albumId: Long, val performanceId: Long = -1L, val title: String) : NavKey

@Serializable
data class PlaylistTracksRoute(val playlistId: Long, val title: String) : NavKey

@Serializable
data object SettingsRoute : NavKey

@Serializable
data object ClassicalGenreSettingsRoute : NavKey
