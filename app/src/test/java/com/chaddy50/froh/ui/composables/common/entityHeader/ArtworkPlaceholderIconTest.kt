package com.chaddy50.froh.ui.composables.common.entityHeader

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtworkPlaceholderIconTest {

    @Test
    fun albumArtistUsesPersonIcon() {
        assertEquals(Icons.Filled.Person, artworkPlaceholderIconFor(EntityType.AlbumArtist))
    }

    @Test
    fun artistUsesPersonIcon() {
        assertEquals(Icons.Filled.Person, artworkPlaceholderIconFor(EntityType.Artist))
    }

    @Test
    fun albumUsesAlbumIcon() {
        assertEquals(Icons.Filled.Album, artworkPlaceholderIconFor(EntityType.Album))
    }

    @Test
    fun performanceUsesAlbumIcon() {
        assertEquals(Icons.Filled.Album, artworkPlaceholderIconFor(EntityType.Performance))
    }
}
