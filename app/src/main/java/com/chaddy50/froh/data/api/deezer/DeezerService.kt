package com.chaddy50.froh.data.api.deezer

import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerService {
    @GET("search/artist")
    suspend fun searchArtist(@Query("q") name: String, @Query("limit") limit: Int = 1): DeezerArtistSearchResponse
}
