package com.chaddy50.froh.fakes

import android.net.Uri
import com.chaddy50.froh.data.scanner.util.IMedia3MetadataReader
import com.chaddy50.froh.data.scanner.util.TrackMetadata

class FakeMedia3MetadataReader(
    var metadata: TrackMetadata? = TrackMetadata(),
) : IMedia3MetadataReader {
    var readCount = 0
    var lastReadUri: Uri? = null

    override suspend fun read(trackUri: Uri): TrackMetadata? {
        readCount++
        lastReadUri = trackUri
        return metadata
    }
}
