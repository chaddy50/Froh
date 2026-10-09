package com.chaddy50.froh.data.api.deezer

import android.util.Log
import com.chaddy50.froh.data.util.IArtworkDownloader
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

private const val API_RATE_LIMIT_DELAY = 200L
private const val TAG = "DeezerRepository"

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
        } catch (e: IOException) {
            Log.e(TAG, "Failed to fetch artist portrait for $artistName", e)
            null
        } catch (e: HttpException) {
            Log.e(TAG, "Failed to fetch artist portrait for $artistName", e)
            null
        }
    }
}
