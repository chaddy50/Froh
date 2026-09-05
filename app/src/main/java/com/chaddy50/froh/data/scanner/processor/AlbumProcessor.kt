package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.repository.IAlbumRepository
import com.chaddy50.froh.data.scanner.util.IArtworkSaver

class AlbumProcessor(
    private val albumRepository: IAlbumRepository,
    private val artworkSaver: IArtworkSaver,
) {
    private val processedAlbums = mutableMapOf<Long, AlbumProcessorResult>()

    suspend fun process(
        albumId: Long,
        trackId: Long,
        albumArtistId: Long,
        albumName: String,
        albumYear: String,
    ) : AlbumProcessorResult {
        processedAlbums[albumId]?.let { return it }

        val catalogueSortIndex = extractCatalogueSortIndex(albumName)
        val catalogueString = extractCatalogueString(albumName)
        val cleanTitle = stripCatalogueFromTitle(albumName)

        val albumArtworkPath = artworkSaver.loadAndSaveArtwork(trackId, albumId)
        albumRepository.insert(
            Album(
                id = albumId,
                title = cleanTitle,
                catalogueSortIndex = catalogueSortIndex,
                catalogueString = catalogueString,
                artistId = albumArtistId,
                year = albumYear,
                artworkPath = albumArtworkPath,
            )
        )
        val result = AlbumProcessorResult(albumId, cleanTitle, albumArtworkPath, albumYear)
        processedAlbums[albumId] = result
        return result
    }
}

private val cataloguePattern = Regex("""(?i)(Op\.?|K\.?|BWV|WoO|Hob\.?|RV|D\.?|S\.?|M\.?|L\.?)\s*(\d+)([a-z])?(?:[,\s]+No\.?\s*(\d+))?""")
internal fun extractCatalogueSortIndex(albumName: String): Int {
    val match = cataloguePattern.find(albumName) ?: return 99_999_999

    val prefix = match.groupValues[1].uppercase().trimEnd('.')
    val mainNumber = match.groupValues[2].toIntOrNull() ?: return 99_999_999
    val letterSuffix = match.groupValues[3].firstOrNull()?.let { it - 'a' + 1 } ?: 0
    val subNumber = match.groupValues[4].toIntOrNull() ?: 0

    // WoO sorts after all other catalogue types
    val prefixPriority = if (prefix == "WOO") 1 else 0

    // Compound sort key: prefix priority, main number, letter suffix, sub-piece number
    return prefixPriority * 10_000_000 + mainNumber * 10_000 + letterSuffix * 100 + subNumber
}

internal fun extractCatalogueString(albumName: String): String? {
    return cataloguePattern.find(albumName)?.value
}

internal fun stripCatalogueFromTitle(albumName: String): String {
    val match = cataloguePattern.find(albumName) ?: return albumName
    // Remove the catalogue match and any adjacent separator ( - , or , )
    val start = match.range.first
    val end = match.range.last + 1
    val before = albumName.substring(0, start).trimEnd()
    val after = albumName.substring(end).trimStart()

    val cleanBefore = before.removeSuffix("-").removeSuffix(",").trimEnd()
    val cleanAfter = after.removePrefix("-").removePrefix(",").trimStart()

    return when {
        cleanBefore.isEmpty() -> cleanAfter
        cleanAfter.isEmpty() -> cleanBefore
        else -> "$cleanBefore $cleanAfter"
    }.trim()
}

data class AlbumProcessorResult(
    val albumId: Long,
    val albumName: String,
    val artworkPath: String?,
    val year: String
)
