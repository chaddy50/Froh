package com.chaddy50.froh.data.api.deezer

import com.chaddy50.froh.data.util.IArtworkDownloader
import kotlinx.coroutines.delay

private const val API_RATE_LIMIT_DELAY = 200L

interface IDeezerRepository {
    suspend fun fetchArtistPortraitUrl(artistName: String, albumArtistId: Long): String?
}

class DeezerRepository(
    private val service: DeezerService,
    private val artworkDownloader: IArtworkDownloader
) : IDeezerRepository {
    override suspend fun fetchArtistPortraitUrl(artistName: String, albumArtistId: Long): String? {
        return try {
            val response = service.searchArtist(artistName)
            delay(API_RATE_LIMIT_DELAY)
            val pictureUrl = response.artists?.firstOrNull()?.pictureUrl
            artworkDownloader.downloadArtwork(pictureUrl, "artist_portraits", albumArtistId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
