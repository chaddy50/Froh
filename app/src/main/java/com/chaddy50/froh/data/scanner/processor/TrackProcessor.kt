package com.chaddy50.froh.data.scanner.processor

import android.content.ContentUris
import android.provider.MediaStore
import com.chaddy50.froh.data.entity.Track
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TrackProcessor() {
    fun process(
        trackId: Long,
        trackNumber: Int,
        genreId: Long,
        genreName: String,
        parentGenreId: Long?,
        parentGenreName: String?,
        artistId: Long,
        artistName: String,
        albumId: Long,
        albumName: String,
        albumArtworkPath: String?,
        albumArtistId: Long,
        albumArtistName: String,
        performanceId: Long?,
        year: String,
        trackTitle: String,
        discNumber: Int,
        trackDuration: Long,
    ): Track {
        val trackUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            trackId
        ).toString()
        return Track(
            trackId,
            trackUri,
            trackTitle,
            trackNumber,
            albumId,
            albumName,
            artistId,
            artistName,
            albumArtistId,
            albumArtistName,
            genreId,
            genreName,
            parentGenreId,
            parentGenreName,
            trackDuration.toDuration(DurationUnit.MILLISECONDS),
            discNumber,
            performanceId,
            albumArtworkPath,
            year
        )
    }
}