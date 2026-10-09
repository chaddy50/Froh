package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.util.IArtworkDownloader

class FakeArtworkDownloader(
    private val resultPath: String? = null,
) : IArtworkDownloader {
    var lastUrl: String? = null
    var lastFileId: Long? = null
    var downloadCount = 0

    override fun downloadArtwork(url: String?, directoryName: String, fileId: Long): String? {
        lastUrl = url
        lastFileId = fileId
        downloadCount++
        return resultPath
    }
}
