package com.chaddy50.froh.ui.composables.nowPlayingBar

import androidx.core.net.toUri
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAudioDbRepository
import com.chaddy50.froh.fakes.testTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BuildMediaItemsTest {

    private fun createAlbumArtistRepository(vararg albumArtists: AlbumArtist): AlbumArtistRepository {
        val dao = FakeAlbumArtistDao(MutableStateFlow(albumArtists.toList()))
        return AlbumArtistRepository(dao, FakeAudioDbRepository())
    }

    @Test
    fun classicalTrackWithNoArtworkFallsBackToAlbumArtistPortrait() = runTest {
        val albumArtistRepository = createAlbumArtistRepository(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach", portraitPath = "/portraits/bach.jpg"),
        )
        val track = testTrack(albumArtistId = 1, parentGenreId = 10, artworkPath = null)

        val mediaItems = buildMediaItems(listOf(track), classicalGenreId = 10, albumArtistRepository)

        assertEquals("/portraits/bach.jpg".toUri(), mediaItems[0].mediaMetadata.artworkUri)
    }

    @Test
    fun classicalTrackWithOwnArtworkKeepsItOverPortrait() = runTest {
        val albumArtistRepository = createAlbumArtistRepository(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach", portraitPath = "/portraits/bach.jpg"),
        )
        val track = testTrack(albumArtistId = 1, parentGenreId = 10, artworkPath = "/art/goldberg.jpg")

        val mediaItems = buildMediaItems(listOf(track), classicalGenreId = 10, albumArtistRepository)

        assertEquals("/art/goldberg.jpg".toUri(), mediaItems[0].mediaMetadata.artworkUri)
    }

    @Test
    fun nonClassicalTrackWithNoArtworkDoesNotFallBackToPortrait() = runTest {
        val albumArtistRepository = createAlbumArtistRepository(
            AlbumArtist(id = 1, name = "Pink Floyd", sortName = "Pink Floyd", portraitPath = "/portraits/pf.jpg"),
        )
        val track = testTrack(albumArtistId = 1, parentGenreId = null, artworkPath = null)

        val mediaItems = buildMediaItems(listOf(track), classicalGenreId = 10, albumArtistRepository)

        assertNull(mediaItems[0].mediaMetadata.artworkUri)
    }
}
