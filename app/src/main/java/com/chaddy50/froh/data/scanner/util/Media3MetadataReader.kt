package com.chaddy50.froh.data.scanner.util

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.inspector.MetadataRetriever
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

private const val METADATA_READ_TIMEOUT_MILLISECONDS = 15_000L

data class TrackMetadata(
    val title: String? = null,
    val artist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val year: String? = null,
    val trackNumber: String? = null,
    val discNumber: String? = null,
    val durationMilliseconds: Long? = null,
)

interface IMedia3MetadataReader {
    suspend fun read(trackUri: Uri): TrackMetadata?
}

/**
 * Media3 caps concurrent retrievals globally at 5 by default, which would throttle the scanner's per-core workers
 */
@OptIn(UnstableApi::class)
fun setMaximumParallelMetadataReads(maximumParallelReads: Int) {
    MetadataRetriever.setMaximumParallelRetrievals(maximumParallelReads)
}

@OptIn(UnstableApi::class)
class Media3MetadataReader(private val context: Context) : IMedia3MetadataReader {

    override suspend fun read(trackUri: Uri): TrackMetadata? =
        withTimeoutOrNull(METADATA_READ_TIMEOUT_MILLISECONDS) {
            try {
                MetadataRetriever.Builder(context, MediaItem.fromUri(trackUri))
                    .build()
                    .use { retriever ->
                        val durationMicroseconds = retriever.retrieveDurationUs().await()
                        val entries = collectMetadataEntries(retriever.retrieveTrackGroups().await())
                        toTrackMetadata(entries, durationMicroseconds)
                    }
            } catch (cancellation: CancellationException) {
                // Must not be swallowed, or cancelling a scan would look like an unreadable file
                throw cancellation
            } catch (malformedMedia: IOException) {
                malformedMedia.printStackTrace()
                null
            } catch (unexpectedState: IllegalStateException) {
                unexpectedState.printStackTrace()
                null
            } catch (unsupportedSource: IllegalArgumentException) {
                unsupportedSource.printStackTrace()
                null
            }
        }

    private fun collectMetadataEntries(trackGroups: TrackGroupArray): List<Metadata.Entry> {
        val entries = mutableListOf<Metadata.Entry>()
        for (groupIndex in 0 until trackGroups.length) {
            val trackGroup = trackGroups.get(groupIndex)
            for (formatIndex in 0 until trackGroup.length) {
                val metadata = trackGroup.getFormat(formatIndex).metadata ?: continue
                for (entryIndex in 0 until metadata.length()) {
                    entries.add(metadata.get(entryIndex))
                }
            }
        }
        return entries
    }
}

@OptIn(UnstableApi::class)
internal fun toTrackMetadata(
    entries: List<Metadata.Entry>,
    durationMicroseconds: Long?,
): TrackMetadata {
    val tagValues = collectTagValues(entries)

    return TrackMetadata(
        title = firstTagValue(tagValues, "TIT2", "TT2", "TITLE"),
        artist = firstTagValue(tagValues, "TPE1", "TP1", "ARTIST"),
        albumArtist = firstTagValue(tagValues, "TPE2", "TP2", "ALBUMARTIST"),
        album = firstTagValue(tagValues, "TALB", "TAL", "ALBUM"),
        genre = firstTagValue(tagValues, "TCON", "TCO", "GENRE"),
        // TDRC is the ID3v2.4 replacement for TYER and wins when a file carries both
        year = firstTagValue(tagValues, "TDRC", "TYER", "TYE", "DATE"),
        trackNumber = firstTagValue(tagValues, "TRCK", "TRK", "TRACKNUMBER"),
        discNumber = firstTagValue(tagValues, "TPOS", "TPA", "DISCNUMBER"),
        durationMilliseconds = durationMicroseconds?.takeIf { it > 0 }?.div(1000),
    )
}

@OptIn(UnstableApi::class)
private fun collectTagValues(entries: List<Metadata.Entry>): Map<String, String> {
    val tagValues = mutableMapOf<String, String>()
    for (entry in entries) {
        when (entry) {
            is TextInformationFrame -> entry.values.firstOrNull()?.let { tagValues.putIfAbsent(entry.id, it) }
            is VorbisComment -> tagValues.putIfAbsent(entry.key.uppercase(Locale.US), entry.value)
        }
    }
    return tagValues
}

private fun firstTagValue(tagValues: Map<String, String>, vararg tagKeys: String): String? =
    tagKeys.firstNotNullOfOrNull { key ->
        tagValues[key]?.trim()?.takeIf { it.isNotEmpty() }
    }
