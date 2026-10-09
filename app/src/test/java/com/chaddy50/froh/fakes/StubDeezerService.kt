package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.api.deezer.DeezerArtistSearchResponse
import com.chaddy50.froh.data.api.deezer.DeezerService

class StubDeezerService(
    private val response: DeezerArtistSearchResponse = DeezerArtistSearchResponse(artists = null),
    private val exception: Exception? = null,
) : DeezerService {
    override suspend fun searchArtist(name: String, limit: Int): DeezerArtistSearchResponse {
        exception?.let { throw it }
        return response
    }
}
