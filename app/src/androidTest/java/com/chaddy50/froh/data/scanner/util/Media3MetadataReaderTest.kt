package com.chaddy50.froh.data.scanner.util

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chaddy50.froh.utilities.resolveId3GenreName
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

private fun copyAssetToCache(context: Context, assetName: String): Uri {
    val cachedFile = File(context.cacheDir, assetName)
    context.assets.open(assetName).use { input ->
        cachedFile.outputStream().use { output -> input.copyTo(output) }
    }
    return Uri.fromFile(cachedFile)
}

@RunWith(AndroidJUnit4::class)
class Media3MetadataReaderReadTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun readsGenreFromMp3WithNumericId3GenreCode() = runTest {
        val reader = Media3MetadataReader(context)

        val metadata = reader.read(copyAssetToCache(context, "numeric_genre_id3.mp3"))

        assertNotNull("Media3 should read the MP3", metadata)
        assertEquals("(161)", metadata?.genre)
        assertEquals("Emo", resolveId3GenreName(metadata?.genre))
        assertEquals("Dead on Arrival", metadata?.title)
        assertEquals("Fall Out Boy", metadata?.artist)
    }

    @Test
    fun readsAllTagsFromFlacVorbisComments() = runTest {
        val reader = Media3MetadataReader(context)

        val metadata = reader.read(copyAssetToCache(context, "vorbis_comments.flac"))

        assertNotNull("Media3 should read the FLAC", metadata)
        assertEquals("Surviving Exile", metadata?.title)
        assertEquals("Darren Korb", metadata?.artist)
        assertEquals("Pyre", metadata?.albumArtist)
        assertEquals("Pyre - Original Soundtrack", metadata?.album)
        assertEquals("Video Game", metadata?.genre)
        assertEquals("2017", metadata?.year)
        assertEquals("06", metadata?.trackNumber)
    }

    @Test
    fun returnsNullForUnreadableFile() = runTest {
        val reader = Media3MetadataReader(context)

        val metadata = reader.read(copyAssetToCache(context, "unreadable.bin"))

        assertNull(metadata)
    }
}
