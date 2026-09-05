package com.chaddy50.froh.data.scanner.util

import android.database.Cursor
import android.provider.MediaStore
import androidx.core.database.getLongOrNull

data class CursorData(
    val trackId: Long,
    val albumId: Long?,
    val trackDuration: Long?,
    val lastModifiedAt: Long?
)

data class ColumnIndices(
    val trackId: Int,
    val albumId: Int,
    val trackDuration: Int,
    val lastModifiedAt: Int
) {
    constructor(cursor: Cursor) : this(
        trackId = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID),
        albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID),
        trackDuration = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION),
        lastModifiedAt = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
    )
}

fun getDataFromCursor(cursor: Cursor, columns: ColumnIndices): CursorData {
    return CursorData(
        cursor.getLong(columns.trackId),
        cursor.getLongOrNull(columns.albumId),
        cursor.getLongOrNull(columns.trackDuration),
        cursor.getLongOrNull(columns.lastModifiedAt)
    )
}
