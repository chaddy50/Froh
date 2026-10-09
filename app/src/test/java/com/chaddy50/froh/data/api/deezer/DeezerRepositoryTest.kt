package com.chaddy50.froh.data.api.deezer

import com.chaddy50.froh.fakes.FakeArtworkDownloader
import com.chaddy50.froh.fakes.StubDeezerService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class DeezerRepositoryTest {

    @Test
    fun returnsDownloadedPortraitPathWhenApiReturnsResults() = runTest {
        val service = StubDeezerService(
            response = DeezerArtistSearchResponse(
                artists = listOf(
                    DeezerArtist(pictureUrl = "https://example.com/portrait.jpg")
                )
            )
        )
        val downloader = FakeArtworkDownloader(resultPath = "/portraits/led_zeppelin.jpg")
        val repo = DeezerRepository(service, downloader)

        val result = repo.fetchArtistPortraitUrl("Led Zeppelin", albumArtistId = 42L)

        assertEquals("/portraits/led_zeppelin.jpg", result)
        assertEquals("https://example.com/portrait.jpg", downloader.lastUrl)
        assertEquals(42L, downloader.lastFileId)
    }

    @Test
    fun returnsNullWhenApiReturnsNoResults() = runTest {
        val service = StubDeezerService(
            response = DeezerArtistSearchResponse(artists = null)
        )
        val downloader = FakeArtworkDownloader(resultPath = null)
        val repo = DeezerRepository(service, downloader)

        val result = repo.fetchArtistPortraitUrl("Unknown Artist", albumArtistId = 1L)

        assertNull(result)
    }

    @Test
    fun returnsNullWhenApiThrowsException() = runTest {
        val service = StubDeezerService(exception = IOException("Network error"))
        val downloader = FakeArtworkDownloader()
        val repo = DeezerRepository(service, downloader)

        val result = repo.fetchArtistPortraitUrl("Led Zeppelin", albumArtistId = 1L)

        assertNull(result)
    }
}
