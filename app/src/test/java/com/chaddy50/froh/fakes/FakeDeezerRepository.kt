package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.api.deezer.IDeezerRepository

class FakeDeezerRepository(
    private val portraitUrl: String? = null,
) : IDeezerRepository {
    override suspend fun fetchArtistPortraitUrl(artistName: String, albumArtistId: Long): String? = portraitUrl
}
