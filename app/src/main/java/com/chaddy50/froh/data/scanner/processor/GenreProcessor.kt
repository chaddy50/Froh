package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.repository.GenreMappingRepository
import com.chaddy50.froh.data.repository.GenreRepository

private val GENRES_WITHOUT_ARTIST_ARTWORK = listOf("Anime", "Movie", "Video Game")

class GenreProcessor(
    private val genreRepository: GenreRepository,
    private val genreMappingRepository: GenreMappingRepository,
) {
    private var parentGenreIdCache: MutableMap<String, Long> = mutableMapOf()
    private val genreIdCache: MutableMap<String, Long> = mutableMapOf()
    private var classicalGenreMappings: Map<String, String> = emptyMap()
    private val GENRE_CLASSICAL = "Classical"

    suspend fun process(
        genreName: String
    ): GenreProcessorResponse {
        val parentGenreId = getParentGenreId(genreName)
        val isClassical = classicalGenreMappings[genreName] == GENRE_CLASSICAL

        val genreId = genreIdCache.getOrPut(genreName) {
            genreRepository.findOrInsertGenreByName(genreName, parentGenreId)
        }

        return GenreProcessorResponse(
            genreId,
            genreName,
            parentGenreId,
            isClassical,
        )
    }

    private fun getParentGenreId(genreName: String): Long? {
        val parentGenreName = classicalGenreMappings[genreName] ?: return null
        return parentGenreIdCache[parentGenreName]
    }

    suspend fun setUpClassicalMappings() {
        classicalGenreMappings = genreMappingRepository.getAllMappingsAsMap()

        val parentGenreNames = classicalGenreMappings.values.distinct()
        for (parentGenreName in parentGenreNames) {
            val parentId = genreRepository.findOrInsertGenreByName(parentGenreName)
            parentGenreIdCache[parentGenreName] = parentId
        }
    }
}

data class GenreProcessorResponse(
    val genreId: Long,
    val genreName: String,
    val parentGenreId: Long?,
    val isClassical: Boolean,
)

fun shouldFetchArtistArtworkForGenre(
    genreName: String?,
    genresWithoutArtistArtwork: List<String> = GENRES_WITHOUT_ARTIST_ARTWORK,
): Boolean {
    if (genreName == null) return false
    return genreName !in genresWithoutArtistArtwork
}