package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.repository.IArtistRepository

class ArtistProcessor(
    private val artistRepository: IArtistRepository,
) {
    private val processedArtists = mutableMapOf<String, Long>()

    suspend fun process(
        artistName: String
    ): Pair<Long, String> {
        processedArtists[artistName]?.let { return Pair(it, artistName) }

        val artistId = artistRepository.findOrInsertArtist(artistName)
        processedArtists[artistName] = artistId
        return Pair(artistId, artistName)
    }
}
