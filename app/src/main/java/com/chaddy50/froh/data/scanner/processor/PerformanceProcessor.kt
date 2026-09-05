package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.data.repository.IPerformanceRepository
import com.chaddy50.froh.data.scanner.util.IArtworkSaver

class PerformanceProcessor(
    private val performanceRepository: IPerformanceRepository,
    private val artworkSaver: IArtworkSaver,
) {
    private val performanceIdCache: MutableMap<Pair<Long, Long>, Triple<Long, String?, String>> = mutableMapOf()

    suspend fun process(
        isClassical: Boolean,
        trackId: Long,
        genreId: Long,
        albumId: Long,
        artistId: Long,
        albumName: String,
        artistName: String,
        year: String,
    ): Triple<Long, String?, String>? {
        if (!isClassical) return null

        val performance = performanceIdCache[Pair(albumId, artistId)]
        if (performance != null) {
            return Triple(performance.first, performance.second, performance.third)
        }

        var performanceId = performanceRepository.insert(
            Performance(
                0,
                albumId,
                albumName,
                artistId,
                artistName,
                year,
                genreId
            )
        )

        // If insert returned -1, another worker already inserted this performance
        if (performanceId == -1L) {
            performanceId = performanceRepository.findByAlbumAndArtist(albumId, artistId)
                ?: return null
        }

        val performanceArtworkPath = artworkSaver.loadAndSaveArtwork(trackId, performanceId)

        performanceIdCache.put(Pair(albumId, artistId), Triple(performanceId, performanceArtworkPath, year))
        return Triple(performanceId, performanceArtworkPath, year)
    }
}
