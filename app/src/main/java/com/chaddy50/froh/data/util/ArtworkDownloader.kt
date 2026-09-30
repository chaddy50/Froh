package com.chaddy50.froh.data.util

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

interface IArtworkDownloader {
    fun downloadArtwork(url: String?, directoryName: String, fileId: Long): String?
}

class ArtworkDownloader(private val context: Context) : IArtworkDownloader {
    private val client = OkHttpClient()

    override fun downloadArtwork(url: String?, directoryName: String, fileId: Long): String? {
        if (url == null) return null
        return try {
            val directory = File(context.filesDir, directoryName)
            if (!directory.exists()) directory.mkdirs()
            val file = File(directory, "$fileId.jpg")

            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null) return null
                body.byteStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
