package com.chaddy50.froh.data.scanner.util

import android.content.ContentUris
import android.provider.MediaStore
import com.chaddy50.froh.utilities.normalizeYear
import com.chaddy50.froh.utilities.parseTrackNumber
import com.chaddy50.froh.utilities.resolveId3GenreName

const val UNKNOWN_TITLE = "Unknown Title"
const val UNKNOWN_ARTIST = "Unknown Artist"
const val UNKNOWN_ALBUM = "Unknown Album"
const val UNKNOWN_GENRE = "Unknown Genre"

data class ResolvedTrackMetadata(
    val title: String,
    val artist: String,
    val albumArtist: String,
    val album: String,
    val genre: String,
    val year: String,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMilliseconds: Long,
)

class MetadataResolver(private val metadataReader: IMedia3MetadataReader) {

    suspend fun resolve(cursorData: CursorData): ResolvedTrackMetadata {
        val trackUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            cursorData.trackId
        )
        val metadata = metadataReader.read(trackUri)

        return ResolvedTrackMetadata(
            title = metadata?.title ?: UNKNOWN_TITLE,
            artist = metadata?.artist ?: UNKNOWN_ARTIST,
            albumArtist = metadata?.albumArtist ?: UNKNOWN_ARTIST,
            album = metadata?.album ?: UNKNOWN_ALBUM,
            genre = resolveId3GenreName(metadata?.genre) ?: UNKNOWN_GENRE,
            year = normalizeYear(metadata?.year),
            trackNumber = parseTrackNumber(metadata?.trackNumber),
            discNumber = parseTrackNumber(metadata?.discNumber).coerceAtLeast(0),
            durationMilliseconds = metadata?.durationMilliseconds ?: cursorData.trackDuration ?: 0,
        )
    }
}
