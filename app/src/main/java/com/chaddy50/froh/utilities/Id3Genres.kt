package com.chaddy50.froh.utilities

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.Id3Util

private val PARENTHESISED_GENRE_CODE = Regex("""^\((\d+)\)(.*)$""")
private const val REMIX_CODE = "(RX)"
private const val COVER_CODE = "(CR)"

/**
 * Turns an ID3 genre tag into a real genre name. Taggers may write the genre as a numeric ID3v1
 * code rather than text, which Android's platform extractor drops entirely instead of passing
 * through. Media3 owns the code table, so nothing here needs maintaining as the list grows.
 */
@OptIn(UnstableApi::class)
fun resolveId3GenreName(rawGenreName: String?): String? {
    if (rawGenreName.isNullOrBlank()) return null

    val genreName = rawGenreName.trim()
    if (genreName == REMIX_CODE) return "Remix"
    if (genreName == COVER_CODE) return "Cover"

    val parenthesisedCode = PARENTHESISED_GENRE_CODE.matchEntire(genreName)
    if (parenthesisedCode != null) {
        val genreCode = parenthesisedCode.groupValues[1].toIntOrNull() ?: return null
        val refinement = parenthesisedCode.groupValues[2].trim()
        return Id3Util.resolveV1Genre(genreCode) ?: refinement.ifEmpty { null }
    }

    val bareGenreCode = genreName.toIntOrNull()
    if (bareGenreCode != null) return Id3Util.resolveV1Genre(bareGenreCode)

    return genreName
}
