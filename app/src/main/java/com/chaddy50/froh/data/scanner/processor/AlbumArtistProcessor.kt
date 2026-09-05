package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.repository.IAlbumArtistRepository

class AlbumArtistProcessor(
    private val albumArtistRepository: IAlbumArtistRepository,
) {
    private val processedAlbumArtists: MutableMap<String, Pair<Long, String>> = mutableMapOf()

    suspend fun process(
        albumArtistName: String,
    ): Pair<Long, String> {
        processedAlbumArtists[albumArtistName]?.let { return it }

        val albumArtistId = albumArtistRepository.findOrInsertAlbumArtist(
            albumArtistName,
        )
        val newAlbumArtist = Pair(albumArtistId, albumArtistName)
        processedAlbumArtists[albumArtistName] = newAlbumArtist
        return newAlbumArtist
    }
}